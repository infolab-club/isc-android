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
    public static final String EXTRA_STATUS_GRAPH = "statusGraph"; // test, stripping, history
//    public static final String EXTRA_JSON_INDEX = "json";
    LineChart chart;
    TestSimulation testSimulation;
    private int currentAxes = 0;
    LimitLine limitX = new LimitLine(0f);
    LimitLine limitY = new LimitLine(0f);
    Button stopBtn;
    private BluetoothController bluetoothController;
    private boolean isStripping;
    public static int countStripping = 0;
    private int stageStripping = 0;
    public static ArrayList<MomentTest> stripping1 = new ArrayList<>();
    public static ArrayList<MomentTest> stripping2 = new ArrayList<>();
    private TextView textStatusStripping;
    private TextView textTimeStripping;
    private String statusGraph;
    private TextView textAxisX;
    private TextView textAxisY;
    int indexTest;
    MyTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        final String testName = intent.getStringExtra(EXTRA_TEST);
        indexTest = intent.getIntExtra(EXTRA_INDEX, 0);
        statusGraph = intent.getStringExtra(EXTRA_STATUS_GRAPH);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);

        if (testName.equals("Stripping voltammetry")) {
            isStripping = true;
            countStripping++;
        }

        if (BluetoothController.isBluetoothRun && !isStripping && !statusGraph.equals("history")) {
            bluetoothController = new BluetoothController(this);
            BluetoothController.isTestRun = true;
        }

        chart = findViewById(R.id.chart);
        stylingChart();

        textStatusStripping = findViewById(R.id.textStatusStripping);
        textTimeStripping = findViewById(R.id.textTimeStripping);
        textAxisX = findViewById(R.id.XAxisText);
        textAxisY = findViewById(R.id.YAxisText);

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

        stopBtn = findViewById(R.id.buttonSave);

        if (isStripping) {
            radioGroup.setVisibility(View.GONE);
            stopBtn.setVisibility(View.GONE);
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
        else if (BluetoothController.isBluetoothRun && !statusGraph.equals("history")) {
            return;
        }
        else if (statusGraph.equals("history")) {
            stopBtn.setVisibility(View.GONE);
            DBRecords db = new DBRecords(this);
            CurrentTest.results = CurrentTest.convertJsonToTests(db.select(indexTest).getJson());
            drawChart();
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
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        yAxis.setEnabled(false);

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
        List<List<Entry>> megaEntries = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();
        List<Entry> entries2 = new ArrayList<>();
        String labelX = "";
        String labelY = "";

        if (!isStripping) {
            for (int i = 0; i < CurrentTest.results.size() - 1; i++) {
                List<Entry> smallEntries = new ArrayList<>();
                // turn your data into Entry objects
                switch (currentAxes) {
                    case 0:
                        smallEntries.add(new Entry(CurrentTest.results.get(i).getTime(), CurrentTest.results.get(i).getVoltage()));
                        smallEntries.add(new Entry(CurrentTest.results.get(i + 1).getTime(), CurrentTest.results.get(i + 1).getVoltage()));
                        megaEntries.add(smallEntries);
                        break;
                    case 1:
                        smallEntries.add(new Entry(CurrentTest.results.get(i).getTime(), CurrentTest.results.get(i).getAmperage()));
                        smallEntries.add(new Entry(CurrentTest.results.get(i + 1).getTime(), CurrentTest.results.get(i + 1).getAmperage()));
                        megaEntries.add(smallEntries);
                        break;
                    case 2:
                        smallEntries.add(new Entry(CurrentTest.results.get(i).getVoltage(), CurrentTest.results.get(i).getAmperage()));
                        smallEntries.add(new Entry(CurrentTest.results.get(i + 1).getVoltage(), CurrentTest.results.get(i + 1).getAmperage()));
                        megaEntries.add(smallEntries);
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
                labelX = "current, uA";
                break;
            case 2:
                labelY = "potential, V";
                labelX = "current, uA";
                break;
        }

        if (isStripping) {
            labelY = "E, V";
            labelX = "I, uA";
        }

//        limitX.setLabel(labelX);
//        limitY.setLabel(labelY);

        textAxisX.setText(labelY);
        textAxisY.setText(labelX);

        LineData lineData = new LineData();

        if (!isStripping) {
            for (List<Entry> e: megaEntries) {
                LineDataSet dataSet = getFirstStyleDataSet(e);
                lineData.addDataSet(dataSet);
            }
        }
        else {
            LineDataSet dataSet1 = getFirstStyleDataSet(entries);
            lineData.addDataSet(dataSet1);
            if (countStripping == 2) {
                LineDataSet dataSet2 = getTwoStyleDataSet(entries2);
                lineData.addDataSet(dataSet2);
            }
        }

        chart.setData(lineData);
        chart.invalidate();
    }

    private LineDataSet getFirstStyleDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Test №1");
        dataSet.setCircleColor(Color.rgb(61, 165, 244));
        dataSet.setCircleHoleColor(Color.rgb(61, 165, 244));
        dataSet.setCircleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(4f);
        dataSet.setColor(Color.rgb(61, 165, 244));
        return dataSet;
    }

    private LineDataSet getTwoStyleDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Test №2");
        dataSet.setCircleColor(Color.rgb(61, 244, 165));
        dataSet.setCircleHoleColor(Color.rgb(61, 244, 165));
        dataSet.setCircleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(4f);
        dataSet.setColor(Color.rgb(61, 244, 165));
        return dataSet;
    }

    @Override
    protected void onStop() {
        if (BluetoothController.isBluetoothRun && !isStripping) {
            BluetoothController.isTestRun = false;
        }
        else if (testSimulation != null){
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
