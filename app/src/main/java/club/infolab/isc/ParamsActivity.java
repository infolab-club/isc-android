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

import java.util.ArrayList;

import club.infolab.isc.bluetooth.BluetoothCallback;
import club.infolab.isc.bluetooth.BluetoothController;
import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.params.ParamManager;
import club.infolab.isc.test.params.ParamTest;

public class ParamsActivity extends AppCompatActivity
    implements BluetoothCallback {
    private BluetoothController bluetoothController;
    private StringBuilder testParams;
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
        testParams = new StringBuilder();
    }

    private void fillWithParameters() {
        LayoutInflater ltInflater = getLayoutInflater();
        for (final ParamTest param : thisParams) {

            View view = ltInflater.inflate(R.layout.item_param, null, false);

            TextView paramName = view.findViewById(R.id.param_name_item);
            paramName.setText(param.getName().substring(0, 1).toUpperCase() + param.getName().substring(1));
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
        for (int i = 0; i < thisParams.size(); i++) {
            View view = paramLines.getChildAt(i);
            EditText editText = view.findViewById(R.id.editValues);
            String param = editText.getText().toString();
            testParams.append(param).append(" ");
        }
        testParams.deleteCharAt(testParams.length() - 1);
    }

    private void startGraphActivity() {
        Intent intent = new Intent(ParamsActivity.this, GraphActivity.class);
        if (BluetoothController.isBluetoothRun) {
//            bluetoothController.sendData(testName + " \n" + testParams);
            bluetoothController.sendData(testName + "\n");
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bluetoothController.sendData(testParams.toString());
            intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_BLUETOOTH);
        } else {
            intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_SIMULATION);
        }
        intent.putExtra(GraphActivity.EXTRA_TEST_NAME, testName);
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
