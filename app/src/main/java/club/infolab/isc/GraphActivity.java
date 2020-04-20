package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private boolean isStripping;
    public static int countStripping = 0;
    private int stageStripping = 0;
    public static ArrayList<MomentTest> stripping1 = new ArrayList<>();
    public static ArrayList<MomentTest> stripping2 = new ArrayList<>();
    private TextView textStatusStripping;
    private TextView textTimeStripping;
    int indexTest;
    MyTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        final String testName = intent.getStringExtra(EXTRA_TEST);
        indexTest = intent.getIntExtra(EXTRA_INDEX, 0);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);

        if (testName.equals("Stripping voltammetry")) {
            isStripping = true;
            countStripping++;
        }

        if (BluetoothController.isBluetoothRun && !isStripping) {
            bluetoothController = new BluetoothController(this);
            BluetoothController.isTestRun = true;
        }

        chart = findViewById(R.id.chart);
        stylingChart();

        textStatusStripping = findViewById(R.id.textStatusStripping);
        textTimeStripping = findViewById(R.id.textTimeStripping);

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

        if (isStripping) {
            radioGroup.setVisibility(View.GONE);
            textStatusStripping.setVisibility(View.VISIBLE);
            textTimeStripping.setVisibility(View.VISIBLE);

            indexTest = countStripping == 1 ? 6 : 7;
        }
        else {
            radioGroup.setVisibility(View.VISIBLE);
            textStatusStripping.setVisibility(View.GONE);
            textTimeStripping.setVisibility(View.GONE);
        }

        final Date dateOfStart = Calendar.getInstance().getTime();

        Button stopBtn = findViewById(R.id.buttonSave);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BluetoothController.isBluetoothRun && !isStripping) {
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

        startSimulation();
    }

    private void startSimulation() {
        if (isStripping) {
            if (stageStripping == 0) {
                timer = new MyTimer(5000, 1000);
                timer.start();
                return;
            }
            else if (stageStripping == 1) {
                textStatusStripping.setText("Deposition");
                timer.start();
                return;
            }
            else {
                textStatusStripping.setVisibility(View.GONE);
                textTimeStripping.setVisibility(View.GONE);
            }
        }
        else if (BluetoothController.isBluetoothRun) {
            return;
        }
        testSimulation = new TestSimulation();
        testSimulation.startSimulation(this, this, indexTest);
    }

    @Override
    public void getTestData(MomentTest testData) {
        if (!isStripping) {
            CurrentTest.results.add(testData);
        }
        else {
            if (countStripping == 1) {
                stripping1.add(testData);
            }
            else {
                stripping2.add(testData);
            }
        }
        drawChart();
    }

    private void stylingChart(){
        Legend legend = chart.getLegend();
        if (!isStripping) {
            legend.setEnabled(false);
        }

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
        List<Entry> entries2 = new ArrayList<>();
        String labelX = "";
        String labelY = "";
        if (!isStripping) {
            for (MomentTest moment : CurrentTest.results) {
                // turn your data into Entry objects
                switch (currentAxes) {
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
        }
        else {
            if (countStripping == 1) {
                for (MomentTest moment : stripping1) {
                    entries.add(new Entry(moment.getTime(), moment.getVoltage()));
                }
            }
            else {
                for (MomentTest moment : stripping1) {
                    entries.add(new Entry(moment.getTime(), moment.getVoltage()));
                }
                for (MomentTest moment : stripping2) {
                    entries2.add(new Entry(moment.getTime(), moment.getVoltage()));
                }
            }
        }

        switch(currentAxes) {
            case 0:
                labelY = "time, sec";
                labelX = "potential, V";
                break;
            case 1:
                labelY = "time, sec";
                labelX = "current, мкA";
                break;
            case 2:
                labelY = "potential, V";
                labelX = "current, мкA";
                break;
        }

        if (isStripping) {
            labelY = "E, V";
            labelX = "I, мкA";
        }

        limitX.setLabel(labelX);
        limitY.setLabel(labelY);

        LineDataSet dataSet = new LineDataSet(entries, "Test №1");
        dataSet.setCircleColor(Color.rgb(61, 165, 244));
        dataSet.setCircleHoleColor(Color.rgb(61, 165, 244));
        dataSet.setCircleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(4f);
        dataSet.setColor(Color.rgb(61, 165, 244));

        LineDataSet dataSet2 = new LineDataSet(entries2, "Test №2");
        if (countStripping == 2) {
            dataSet2.setCircleColor(Color.rgb(61, 244, 165));
            dataSet2.setCircleHoleColor(Color.rgb(61, 244, 165));
            dataSet2.setCircleRadius(2f);
            dataSet2.setDrawValues(false);
            dataSet2.setLineWidth(4f);
            dataSet2.setColor(Color.rgb(61, 244, 165));
        }

        LineData lineData = new LineData(dataSet);
        if (countStripping == 2) {
            lineData.addDataSet(dataSet2);
        }
        chart.setData(lineData);
        chart.invalidate();
    }

    @Override
    protected void onStop() {
        if (BluetoothController.isBluetoothRun && !isStripping) {
            BluetoothController.isTestRun = false;
        }
        else {
            testSimulation.stopSimulation();
        }
        if (countStripping == 2) {
            countStripping = 0;
            stripping1.clear();
            stripping2.clear();
        }
        super.onStop();
    }

    @Override
    public void getInputData(String data) {
        MomentTest testData = CurrentTest.getMomentFromString(data);
        CurrentTest.results.add(testData);
        drawChart();
    }

    public class MyTimer extends CountDownTimer
    {

        public MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish()
        {
            stageStripping++;
            startSimulation();
        }

        public void onTick(long millisUntilFinished)
        {
            textTimeStripping.setText(String.valueOf(millisUntilFinished / 1000 + 1));
        }

    }
}
