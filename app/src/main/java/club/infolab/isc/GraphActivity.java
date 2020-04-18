package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import club.infolab.isc.bluetooth.BluetoothCallback;
import club.infolab.isc.bluetooth.BluetoothController;
import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.MomentTest;
import club.infolab.isc.test.simulation.TestSimulation;
import club.infolab.isc.test.simulation.TestSimulationCallback;

public class GraphActivity extends AppCompatActivity
        implements TestSimulationCallback, BluetoothCallback {
    public static final String EXTRA_TEST = "testName";
    public static final String EXTRA_INDEX = "testIndex";
    LineChart chart;
    TestSimulation testSimulation;
    private int currentAxes = 0;
    LimitLine limitX = new LimitLine(0f);
    LimitLine limitY = new LimitLine(0f);
    private BluetoothController bluetoothController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        bluetoothController = new BluetoothController(this);

        Intent intent = getIntent();
        final String testName = intent.getStringExtra(EXTRA_TEST);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);

        chart = findViewById(R.id.chart);
        stylingChart();

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
//        testSimulation = new TestSimulation();
//        testSimulation.startSimulation(this, this, indexTest);
        final Date dateOfStart = Calendar.getInstance().getTime();

        Button stopBtn = findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testSimulation.stopSimulation();
                String jsonResults = CurrentTest.convertTestsToJson(CurrentTest.results);
                DBRecords db = new DBRecords(GraphActivity.this);
                db.insert(testName, dateOfStart.toString(), 0, jsonResults);
            }
        });
    }

    @Override
    public void getTestData(MomentTest testData) {
        CurrentTest.results.add(testData);
        drawChart();
    }

    private void stylingChart(){
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        Description d = chart.getDescription();
        d.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisRight();

        limitX.setLineColor(Color.BLACK);
        limitX.setLineWidth(2f);

        limitY.setLineColor(Color.BLACK);
        limitY.setLineWidth(1.5f);

        limitX.setTextSize(12f);
        limitY.setTextSize(12f);
        limitX.setTextColor(Color.BLACK);
        limitY.setTextColor(Color.BLACK);

        xAxis.addLimitLine(limitX);
        yAxis.addLimitLine(limitY);
    }

    private void drawChart() {
        chart.clear();
        List<Entry> entries = new ArrayList<>();
        for (MomentTest moment : CurrentTest.results) {
            // turn your data into Entry objects
            String labelX, labelY;
            switch(currentAxes){
                case 0:
                    entries.add(new Entry(moment.getTime(), moment.getVoltage()));
                    labelY = "time, sec";
                    labelX = "potential, V";
                    break;
                case 1:
                    entries.add(new Entry(moment.getTime(), moment.getAmperage()));
                    labelY = "time, sec";
                    labelX = "current, uA";
                    break;
                default:
                    entries.add(new Entry(moment.getVoltage(), moment.getAmperage()));
                    labelY = "potential, V";
                    labelX = "current, uA";
                    break;
            }
            limitX.setLabel(labelX);
            limitY.setLabel(labelY);
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(6f);
        dataSet.setColor(Color.rgb(61, 165, 244));
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    @Override
    protected void onStop() {
//        testSimulation.stopSimulation();
        super.onStop();
    }

    @Override
    public void getInputData(String data) {
        Log.d("TEST_DATA", data +"\n");
        MomentTest testData = CurrentTest.getMomentFromString(data);
        CurrentTest.results.add(testData);
        drawChart();
    }
}
