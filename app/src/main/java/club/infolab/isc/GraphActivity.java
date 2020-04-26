package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
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
    public static final String EXTRA_TEST_NAME = "TEST_NAME";
    public static final String EXTRA_TEST_INDEX = "TEST_INDEX";
    public static final int TEST_TYPE_BLUETOOTH = 0;
    public static final int TEST_TYPE_SIMULATION = 1;
    public static final int TEST_TYPE_HISTORY = 2;
    public static final int TEST_TYPE_STRIPPING = 3;
    public static final String EXTRA_TEST_TYPE = "TEST_TYPE";

    private int testType;
    private String testName;
    private int testIndex;

    private LineChart chart;
    private int currentAxes = 0;

    private List<List<Entry>> entriesTimePotential = new ArrayList<>();
    private List<List<Entry>> entriesTimeCurrent = new ArrayList<>();
    private List<List<Entry>> entriesPotentialCurrent = new ArrayList<>();
    private boolean rightPotentialCurrent = true;
    private List<List<Entry>> entriesCurrent;

    private TestSimulation testSimulation;

    private static List<Entry> eStripping1 = new ArrayList<>();
    private static List<Entry> eStripping2 = new ArrayList<>();
    public static final int STRIPPING_STAGE_CLEANING = 0;
    public static final int STRIPPING_STAGE_DEPOSITION = 1;
    public static final int STRIPPING_STAGE_RUNNING = 2;
    private int strippingStage = 0;
    private StrippingTimer strippingTimer;
    public static int strippingIndex = 5;

    private RadioGroup switcherAxises;
    private Button buttonSave;
    private TextView textTestName;
    private TextView textStatusStripping;
    private TextView textTimeStripping;
    private TextView textAxisX;
    private TextView textAxisY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        initializeActivity();
        startSimulation();
    }

    @Override
    protected void onStop() {
        switch (testType) {
            case (TEST_TYPE_BLUETOOTH):
                BluetoothController.isTestRun = false;
                break;
            case (TEST_TYPE_SIMULATION):
                testSimulation.stopSimulation();
                break;
            case (TEST_TYPE_STRIPPING):
                testSimulation.stopSimulation();
                if (strippingIndex == 7) {
                    strippingIndex = 5;
                    eStripping1.clear();
                    eStripping2.clear();
                }
                break;
        }
        CurrentTest.results.clear();
        super.onStop();
    }

    private void initializeActivity() {
        getInfoTest();
        initializeViews();
        customizeActivity();
        customizeChart();
    }

    private void getInfoTest() {
        Intent intent = getIntent();
        testType = intent.getIntExtra(EXTRA_TEST_TYPE, TEST_TYPE_SIMULATION);
        testName = intent.getStringExtra(EXTRA_TEST_NAME);
        testIndex = intent.getIntExtra(EXTRA_TEST_INDEX, 0);
    }

    private void initializeViews() {
        textTestName = findViewById(R.id.name_test);
        textAxisX = findViewById(R.id.XAxisText);
        textAxisY = findViewById(R.id.YAxisText);
        switcherAxises = findViewById(R.id.radio_group);
        buttonSave = findViewById(R.id.buttonSave);
        textStatusStripping = findViewById(R.id.textStatusStripping);
        textTimeStripping = findViewById(R.id.textTimeStripping);
        chart = findViewById(R.id.chart);
    }

    private void customizeActivity() {
        textTestName.setText(testName);

        switch (testType) {
            case (TEST_TYPE_HISTORY):
                textStatusStripping.setVisibility(View.GONE);
                textTimeStripping.setVisibility(View.GONE);
                buttonSave.setVisibility(View.GONE);
                switcherAxises.setOnCheckedChangeListener(onSwitchAxises);
                break;
            case (TEST_TYPE_STRIPPING):
                switcherAxises.setVisibility(View.GONE);
                buttonSave.setVisibility(View.GONE);
                textStatusStripping.setVisibility(View.VISIBLE);
                textTimeStripping.setVisibility(View.VISIBLE);
                break;
            default:
                textStatusStripping.setVisibility(View.GONE);
                textTimeStripping.setVisibility(View.GONE);
                buttonSave.setOnClickListener(onClickSave);
                switcherAxises.setOnCheckedChangeListener(onSwitchAxises);
                break;
        }
    }

    private View.OnClickListener onClickSave = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (testType) {
                case (TEST_TYPE_BLUETOOTH):
                    BluetoothController.isTestRun = false;
                    break;
                case (TEST_TYPE_SIMULATION):
                case (TEST_TYPE_STRIPPING):
                    testSimulation.stopSimulation();
                    break;
            }
            Date date = Calendar.getInstance().getTime();
            String json = CurrentTest.convertTestsToJson(CurrentTest.results);
            DBRecords dataBase = new DBRecords(GraphActivity.this);
            dataBase.insert(testName, date.toString(), 0, json);
        }
    };

    private RadioGroup.OnCheckedChangeListener onSwitchAxises = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            setLabelAxises();
            drawChart();
        }
    };

    private void startSimulation() {
        entriesTimePotential.add(new ArrayList<Entry>());
        entriesTimeCurrent.add(new ArrayList<Entry>());

        switch (testType) {
            case TEST_TYPE_BLUETOOTH:
                BluetoothController bluetoothController = new BluetoothController(this);
                BluetoothController.isTestRun = true;
                break;
            case TEST_TYPE_SIMULATION:
                testSimulation = new TestSimulation();
                testSimulation.startSimulation(this, this, testIndex);
                break;
            case TEST_TYPE_HISTORY:
                DBRecords dataBase = new DBRecords(this);
                ArrayList<MomentTest> testData = CurrentTest.convertJsonToTests(
                        dataBase.select(testIndex).getJson());
                for (MomentTest momentTest: testData) {
                    prepareNewData(momentTest);
                }
                drawChart();
                break;
            case TEST_TYPE_STRIPPING:
                switch (strippingStage) {
                    case STRIPPING_STAGE_CLEANING:
                        strippingTimer = new StrippingTimer(5000, 1000);
                        strippingTimer.start();
                        break;
                    case STRIPPING_STAGE_DEPOSITION:
                        textStatusStripping.setText(getString(R.string.strippingDeposition));
                        strippingTimer.start();
                        break;
                    case STRIPPING_STAGE_RUNNING:
                        textStatusStripping.setVisibility(View.GONE);
                        textTimeStripping.setVisibility(View.GONE);

                        strippingIndex++;

                        testSimulation = new TestSimulation();
                        testSimulation.startSimulation(this, this, strippingIndex);
                        break;
                }
                break;
        }
    }

    @Override
    public void onGetSimulationData(MomentTest testData) {
        if (testType != TEST_TYPE_STRIPPING) {
            prepareNewData(testData);
        }
        else {
            if (strippingIndex == 6) {
                eStripping1.add(new Entry(testData.getTime(), testData.getVoltage()));
            }
            else {
                eStripping2.add(new Entry(testData.getTime(), testData.getVoltage()));
            }
        }
        drawChart();
    }

    private void prepareNewData(MomentTest testData) {
        Entry entryTimePotential = new Entry(testData.getTime(), testData.getVoltage());
        entriesTimePotential.get(0).add(entryTimePotential);

        Entry entryTimeCurrent = new Entry(testData.getTime(), testData.getAmperage());
        entriesTimeCurrent.get(0).add(entryTimeCurrent);

        Entry entryPotentialCurrent = new Entry(testData.getVoltage(), testData.getAmperage());
        int lastResultsIndex = CurrentTest.results.size() - 1;
        if (lastResultsIndex == -1) {
            entriesPotentialCurrent.add(new ArrayList<Entry>());
            entriesPotentialCurrent.get(0).add(entryPotentialCurrent);
        }
        else {
            MomentTest lastData = CurrentTest.results.get(lastResultsIndex);
            int lastEntriesIndex = entriesPotentialCurrent.size() - 1;
            if (testData.getVoltage() > lastData.getVoltage()) {
                if (rightPotentialCurrent) {
                    entriesPotentialCurrent.get(lastEntriesIndex).add(entryPotentialCurrent);
                }
                else {
                    entriesPotentialCurrent.add(new ArrayList<Entry>());
                    entriesPotentialCurrent.get(lastEntriesIndex + 1).add(entryPotentialCurrent);
                    rightPotentialCurrent = true;
                }
            }
            else {
                if (!rightPotentialCurrent) {
                    entriesPotentialCurrent.get(lastEntriesIndex).add(0, entryPotentialCurrent);
                }
                else {
                    if (entriesPotentialCurrent.get(0).size() == 1) {
                        entriesPotentialCurrent.get(lastEntriesIndex).add(0, entryPotentialCurrent);
                    }
                    else {
                        entriesPotentialCurrent.add(new ArrayList<Entry>());
                        entriesPotentialCurrent.get(lastEntriesIndex + 1).add(entryPotentialCurrent);
                    }
                    rightPotentialCurrent = false;
                }
            }
        }

        CurrentTest.results.add(testData);
    }

    @Override
    public void onGetBluetoothData(String data) {
        MomentTest testData = CurrentTest.getMomentFromString(data);
        prepareNewData(testData);
        drawChart();
    }

    private void customizeChart() {
        if (testType != TEST_TYPE_STRIPPING) {
            chart.getLegend().setEnabled(false);
        }
        chart.getDescription().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = chart.getAxisRight();

        LimitLine limitX = new LimitLine(0f);
        limitX.setLineColor(Color.BLACK);
        limitX.setLineWidth(1f);
        limitX.setTextSize(12f);
        limitX.setTextColor(Color.BLACK);

        LimitLine limitY = new LimitLine(0f);
        limitY.setLineColor(Color.BLACK);
        limitY.setLineWidth(1f);
        limitY.setTextSize(12f);
        limitY.setTextColor(Color.BLACK);

        xAxis.addLimitLine(limitX);
        yAxis.addLimitLine(limitY);

        setLabelAxises();
    }

    private void drawChart() {
        LineData lineData = new LineData();

        if (testType != TEST_TYPE_STRIPPING) {
            int color = Color.rgb(61, 165, 244);
            for (List<Entry> e: entriesCurrent) {
                LineDataSet dataSet = getStyleDataSet(e, "", color);
                lineData.addDataSet(dataSet);
            }
        }
        else {
            int color1 = Color.rgb(61, 165, 244);
            LineDataSet dataSet1 = getStyleDataSet(eStripping1, "Test №1", color1);
            lineData.addDataSet(dataSet1);

            if (strippingIndex == 7) {
                int color2 = Color.rgb(61, 244, 165);
                LineDataSet dataSet2 = getStyleDataSet(eStripping2, "Test №2", color2);
                lineData.addDataSet(dataSet2);
            }
        }

        chart.setData(lineData);
        chart.invalidate();
    }

    private void setLabelAxises() {
        String labelXAxis = "";
        String labelYAxis = "";

        if (testType == TEST_TYPE_STRIPPING) {
            entriesCurrent = entriesTimePotential;
            labelXAxis = getString(R.string.strippingXAxis);
            labelYAxis = getString(R.string.strippingYAxis);
        }
        else {
            switch (currentAxes) {
                case 0:
                    entriesCurrent = entriesTimePotential;
                    labelXAxis = getString(R.string.chartAxisTime);
                    labelYAxis = getString(R.string.chartAxisPotential);
                    break;
                case 1:
                    entriesCurrent = entriesTimeCurrent;
                    labelXAxis = getString(R.string.chartAxisTime);
                    labelYAxis = getString(R.string.chartAxisCurrent);
                    break;
                case 2:
                    entriesCurrent = entriesPotentialCurrent;
                    labelXAxis = getString(R.string.chartAxisPotential);
                    labelYAxis = getString(R.string.chartAxisCurrent);
                    break;
            }
        }

        textAxisX.setText(labelXAxis);
        textAxisY.setText(labelYAxis);
    }

    private LineDataSet getStyleDataSet(List<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setCircleColor(color);
        dataSet.setCircleHoleColor(color);
        dataSet.setCircleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(4f);
        dataSet.setColor(color);
        return dataSet;
    }

    public class StrippingTimer extends CountDownTimer {
        StrippingTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            strippingStage++;
            startSimulation();
        }

        public void onTick(long millisUntilFinished) {
            textTimeStripping.setText(String.valueOf(millisUntilFinished / 1000 + 1));
        }
    }
}
