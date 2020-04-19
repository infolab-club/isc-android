package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import club.infolab.isc.database.DBRecords;
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

        if (BluetoothController.isBluetoothRun) {
            bluetoothController = new BluetoothController(this);
            BluetoothController.isTestRun = true;
        }

        Intent intent = getIntent();
        final String testName = intent.getStringExtra(EXTRA_TEST);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);

        chart = findViewById(R.id.chart);
        stylingChart();

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.potential_time_rd_btn:
                        currentAxes = 0;
                        break;
                    case R.id.current_time_rd_btn:
                        currentAxes = 1;
                        break;
                    case R.id.current_potential_rd_btn:
                        currentAxes = 2;
                        break;
                }
                drawChart();
            }
        });

        if (!BluetoothController.isBluetoothRun) {
            int indexTest = intent.getIntExtra(EXTRA_INDEX, 0);
            testSimulation = new TestSimulation();
            testSimulation.startSimulation(this, this, indexTest);
        }
        final Date dateOfStart = Calendar.getInstance().getTime();

        Button stopBtn = findViewById(R.id.buttonSave);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BluetoothController.isBluetoothRun) {
                    BluetoothController.isTestRun = false;
                }
                else {
                    testSimulation.stopSimulation();
                }
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
        String labelX = "";
        String labelY = "";
        for (MomentTest moment : CurrentTest.results) {
            // turn your data into Entry objects
            switch(currentAxes) {
                case 0:
                    entries.add(new Entry(moment.getTime(), moment.getVoltage()));
                    break;
                case 1:
                    entries.add(new Entry(moment.getTime(), moment.getAmperage()));
                    break;
                case 2:
                    entries.add(new Entry(moment.getVoltage(), moment.getAmperage()));
                    break;
            }
        }
        switch(currentAxes) {
            case 0:
                labelY = "time, sec";
                labelX = "potential, V";
                break;
            case 1:
                labelY = "time, sec";
                labelX = "current, uA";
                break;
            case 2:
                labelY = "potential, V";
                labelX = "current, uA";
                break;
        }
        limitX.setLabel(labelX);
        limitY.setLabel(labelY);
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setCircleColor(Color.rgb(61, 165, 244));
        dataSet.setCircleHoleColor(Color.rgb(61, 165, 244));
        dataSet.setCircleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(4f);
        dataSet.setColor(Color.rgb(61, 165, 244));
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    @Override
    protected void onStop() {
        if (BluetoothController.isBluetoothRun) {
            BluetoothController.isTestRun = false;
        }
        else {
            testSimulation.stopSimulation();
        }
        super.onStop();
    }

    @Override
    public void getInputData(String data) {
        MomentTest testData = CurrentTest.getMomentFromString(data);
        CurrentTest.results.add(testData);
        drawChart();
    }
}
