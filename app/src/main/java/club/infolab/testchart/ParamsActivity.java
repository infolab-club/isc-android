package club.infolab.testchart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ParamsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);

        TextView testNameView = findViewById(R.id.name_test_params);
        final Intent intent = getIntent();
        final String testName = intent.getStringExtra(GraphActivity.EXTRA_TEST);
        final int testIndex = intent.getIntExtra(GraphActivity.EXTRA_INDEX, 0);
        testNameView.setText(testName);

        Button startChartBtn = findViewById(R.id.toChart_btn);
        startChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                CurrentTest.testResult.clear();
                // CurrentTest currentTest = new CurrentTest(this);
                // currentTest.Reader(position);
                Intent graph_intent = new Intent(ParamsActivity.this, GraphActivity.class);
                graph_intent.putExtra(GraphActivity.EXTRA_TEST, testName);
                graph_intent.putExtra(GraphActivity.EXTRA_INDEX, testIndex);
                startActivity(graph_intent);
                finish();
            }
        });

        ArrayList<ParamTest> thisParams = ParamManager.getParamsOfTest(testName);
        LayoutInflater ltInflater = getLayoutInflater();
        for (final ParamTest param : thisParams) {

            View view = ltInflater.inflate(R.layout.param_item, null, false);

            TextView paramName = view.findViewById(R.id.param_name_item);
            paramName.setText(param.getName());

            final EditText editParam = view.findViewById(R.id.edit_values);
//            editParam.setMaxvalue(param.getMaxValue());
//            editParam.setMinvalue(param.getMinValue());
//            editParam.setDefaultvalue(Float.toString(param.getDefaultValue()));
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

            LinearLayout paramLines = findViewById(R.id.params_lines);
            paramLines.addView(view);
        }
    }

}
