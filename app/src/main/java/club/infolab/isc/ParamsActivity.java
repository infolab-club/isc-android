package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import club.infolab.isc.bluetooth.BluetoothCallback;
import club.infolab.isc.bluetooth.BluetoothController;
import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.params.ParamManager;
import club.infolab.isc.test.params.ParamTest;

public class ParamsActivity extends AppCompatActivity
    implements BluetoothCallback {
    private BluetoothController bluetoothController;
    private String testParams;
    private String testParamRange;
    private String testParamRate;
    private String testName;
    private int testIndex;

    private LinearLayout paramLines;
    private TextView testNameView;
    private ArrayList<ParamTest> thisParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);

        initializeActivity();
    }

    private void initializeActivity() {
        if (BluetoothController.isBluetoothRun) {
            bluetoothController = new BluetoothController(this);
        }
        initializeViews();
        fillWithParameters();
    }

    private void initializeViews() {
        testNameView = findViewById(R.id.name_test_params);
        Intent intent = getIntent();
        testName = intent.getStringExtra(GraphActivity.EXTRA_TEST_NAME);
        testIndex = intent.getIntExtra(GraphActivity.EXTRA_TEST_INDEX, 0);
        testNameView.setText(testName);
        Button startGraphButton = findViewById(R.id.startGraphButton);
        startGraphButton.setOnClickListener(onStartGraphListener);
        thisParams = ParamManager.getParamsOfTest(testName);
        paramLines = findViewById(R.id.params_lines);
    }

    private void fillWithParameters() {
        LayoutInflater ltInflater = getLayoutInflater();
        for (final ParamTest param : thisParams) {

            View view = ltInflater.inflate(R.layout.item_param, null, false);

            TextView paramName = view.findViewById(R.id.param_name_item);
            String end = "";
            if (!param.getUnit().equals("")) {
                end = " (" + param.getUnit() + ")";
            }
            paramName.setText(param.getName().substring(0, 1).toUpperCase() + param.getName().substring(1) + end);
            final EditText editParam;
            editParam = view.findViewById(R.id.editValues);
            editParam.setText(Float.toString(param.getDefaultValue()));
            editParam.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().equals("") && !charSequence.toString().equals(".")
                            && !charSequence.toString().equals("-")) {
                        if (Float.parseFloat(charSequence.toString()) < param.getMinValue()) {
                            editParam.setText(Float.toString(param.getMinValue()));
                        } else if (Float.parseFloat(charSequence.toString()) > param.getMaxValue()) {
                            editParam.setText(Float.toString(param.getMaxValue()));
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            paramLines.addView(view);
        }
    }

    private View.OnClickListener onStartGraphListener = new View.OnClickListener () {
        @Override
        public void onClick(View v) {
            CurrentTest.results.clear();
            collectParams();
            startGraphActivity();
            overridePendingTransition(0, 0);
            finish();
        }
    };

    private void collectParams() {
        JSONObject json = new JSONObject();
        for (int i = 0; i < thisParams.size(); i++) {
            View view = paramLines.getChildAt(i);
            EditText editText = view.findViewById(R.id.editValues);
            String param = editText.getText().toString();
            if (i == 0) {
                double exp = Math.log10(Float.parseFloat(param));
                int expInt = (int) Math.round(exp);
                int paramInt = (int) Math.pow(10, expInt);
                testParamRange = "{\"command\":\"setCurrRange\",\"currRange\":\"" + paramInt + "uA\"}";
                // Log.d("PARAM_RANGE", String.valueOf(paramInt));
            } else if (i == 1) {
                float paramFloat = Float.parseFloat(param);
                float period = paramFloat / 50 * 1000;
                testParamRate = "{\"command\":\"setSamplePeriod\",\"samplePeriod\":" + period + "}";
            } else {
                if (testName.equals("Cyclic")) {
                    thisParams.get(i).setCurrentValue(Float.parseFloat(param));
                } else {
                    try {
                        json.put(thisParams.get(i).getName(), Double.valueOf(param));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (testName.equals("Cyclic")) {
            float quietValue = thisParams.get(2).getCurrentValue();
            float quietTime = thisParams.get(3).getCurrentValue();
            float minValue = thisParams.get(4).getCurrentValue();
            float maxValue = thisParams.get(5).getCurrentValue();
            float scanRate = thisParams.get(6).getCurrentValue();
            float cycles = thisParams.get(7).getCurrentValue();

            float amplitude = (maxValue - minValue) / 2;
            float offset = (maxValue + minValue) / 2;
            int period = (int) (1000 * 4 * amplitude / scanRate);
            float shift = 0;

            Log.d("PARAM_RANGE", String.valueOf(minValue));

            try {
                json.put("quietValue", quietValue);
                json.put("quietTime", quietTime);
                json.put("amplitude", amplitude);
                json.put("offset", offset);
                json.put("period", period);
                json.put("numCycles", cycles);
                json.put("shift", shift);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        testParams = json.toString();
        //Log.d("USB_DATA", testParams);
    }

    private void startGraphActivity() {
        Intent intent = new Intent(ParamsActivity.this, GraphActivity.class);
//        if (BluetoothController.isBluetoothRun) {
//            bluetoothController.sendData(testName + " \n" + testParams);
//            intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_BLUETOOTH);
//        } else {
//            intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_SIMULATION);
//        }
        intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_USB);
        intent.putExtra(GraphActivity.EXTRA_TEST_NAME, testName);
        intent.putExtra(GraphActivity.EXTRA_TEST_PARAMS, testParams);
        intent.putExtra(GraphActivity.EXTRA_TEST_PARAM_RANGE, testParamRange);
        intent.putExtra(GraphActivity.EXTRA_TEST_PARAM_RATE, testParamRate);
        intent.putExtra(GraphActivity.EXTRA_TEST_INDEX, testIndex);
        startActivity(intent);
    }

    @Override
    public void onGetBluetoothData(String data) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}
