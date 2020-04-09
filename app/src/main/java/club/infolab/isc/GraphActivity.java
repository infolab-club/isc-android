package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.MomentTest;
import club.infolab.isc.test.simulation.TestSimulation;
import club.infolab.isc.test.simulation.TestSimulationCallback;

public class GraphActivity extends AppCompatActivity implements TestSimulationCallback {
    public static final String EXTRA_TEST = "testName";
    public static final String EXTRA_INDEX = "testIndex";
    LineChart chart;
    TestSimulation testSimulation;
    private int currentAxes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        String testName = intent.getStringExtra(EXTRA_TEST);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);

        chart = findViewById(R.id.chart);

        RadioButton radioDefault = findViewById(R.id.potential_time_rd_btn);
        radioDefault.setChecked(true);

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.current_potential_rd_btn:
                        currentAxes = 2;
                        break;
                    case R.id.current_time_rd_btn:
                        currentAxes = 1;
                        break;
                    default:
                        currentAxes = 0;
                        break;
                }
                drawChart();
            }
        });

        int indexTest = intent.getIntExtra(EXTRA_INDEX, 0);
        testSimulation = new TestSimulation();
        testSimulation.startSimulation(this, this, indexTest);
    }

    @Override
    public void getTestData(MomentTest testData) {
        CurrentTest.results.add(testData);
        drawChart();
    }

    private void drawChart() {
        chart.clear();
        List<Entry> entries = new ArrayList<>();
        for (MomentTest moment : CurrentTest.results) {
            // turn your data into Entry objects
            switch(currentAxes){
                case 0:
                    entries.add(new Entry(moment.getTime(), moment.getVoltage()));
                    break;
                case 1:
                    entries.add(new Entry(moment.getTime(), moment.getAmperage()));
                    break;
                default:
                    entries.add(new Entry(moment.getVoltage(), moment.getAmperage()));
                    break;
            }
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    @Override
    protected void onStop() {
        testSimulation.stopSimulation();
        super.onStop();
    }
}
