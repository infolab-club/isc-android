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

import com.vk59.graphviewlibrary.GraphData;
import com.vk59.graphviewlibrary.GraphView;
import com.vk59.graphviewlibrary.Moment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import club.infolab.isc.bluetooth.BluetoothCallback;
import club.infolab.isc.bluetooth.BluetoothController;
import club.infolab.isc.database.DBRecords;
import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.MomentTest;
import club.infolab.isc.test.simulation.TestSimulation;
import club.infolab.isc.test.simulation.TestSimulationCallback;
import es.dmoral.toasty.Toasty;

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

    private GraphView graphView;
    private int currentAxes = 0;

    private boolean isClickedSave;

    private TestSimulation testSimulation;

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
    private GraphData graphData;

    private GraphData graphTP;
    private GraphData graphTC;
    private GraphData graphPC;

    private static GraphData graphStripping1;
    private static GraphData graphStripping2;

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
                    graphView.clear();
                }
                break;
        }
        CurrentTest.results.clear();
        super.onStop();
    }

    private void initializeActivity() {
        getInfoTest();
        initializeViews();
        initializeGraphData();
        customizeActivity();
        customizeGraphView();
    }

    private void initializeGraphData() {
        graphView = findViewById(R.id.graph_view);
        int color = Color.rgb(61, 165, 244);
        if (testType == TEST_TYPE_STRIPPING && strippingIndex == 5) {
            int color1 = Color.rgb(61, 165, 244);
            graphStripping1 = new GraphData(new ArrayList<Moment>(), color1, "Test №1");
            int color2 = Color.rgb(61, 244, 165);
            graphStripping2 = new GraphData(new ArrayList<Moment>(), color2, "Test №2");
        } else {
            graphPC = new GraphData(new ArrayList<Moment>(), color, " ");
            graphTC = new GraphData(new ArrayList<Moment>(), color, " ");
            graphTP = new GraphData(new ArrayList<Moment>(), color, " ");
            graphData = graphTP;
        }
    }

    private void getInfoTest() {
        Intent intent = getIntent();
        testType = intent.getIntExtra(EXTRA_TEST_TYPE, TEST_TYPE_SIMULATION);
        testName = intent.getStringExtra(EXTRA_TEST_NAME);
        testIndex = intent.getIntExtra(EXTRA_TEST_INDEX, 0);
    }

    private void initializeViews() {
        textTestName = findViewById(R.id.name_test);
        switcherAxises = findViewById(R.id.radio_group);
        buttonSave = findViewById(R.id.buttonSave);
        textStatusStripping = findViewById(R.id.textStatusStripping);
        textTimeStripping = findViewById(R.id.textTimeStripping);
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
            if (!isClickedSave) {
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
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                String dateString = dateFormat.format(date);
                String json = CurrentTest.convertTestsToJson(CurrentTest.results);
                DBRecords dataBase = new DBRecords(GraphActivity.this);
                dataBase.insert(testName, dateString, 0, json);
                isClickedSave = true;
                buttonSave.setAlpha(0.8f);
                buttonSave.setClickable(false);
                Toasty.custom(GraphActivity.this, R.string.toast_saved,
                        null, R.color.toast, Toasty.LENGTH_SHORT,
                        false, true).show();
            }
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
                graphStripping1.addData(testData.getTime(), testData.getVoltage());
            }
            else {
                graphStripping2.addData(testData.getTime(), testData.getVoltage());
            }
        }
        drawChart();
    }

    private void prepareNewData(MomentTest testData) {
        graphTP.addData(testData.getTime(), testData.getVoltage());
        graphTC.addData(testData.getTime(), testData.getAmperage());
        graphPC.addData(testData.getVoltage(), testData.getAmperage());
        CurrentTest.results.add(testData);
    }

    @Override
    public void onGetBluetoothData(String data) {
        MomentTest testData = CurrentTest.getMomentFromString(data);
        prepareNewData(testData);
        drawChart();
    }

    private void customizeGraphView() {
        if (testType != TEST_TYPE_STRIPPING) {
            graphView.setLegendEnable(false);
            graphView.addGraphData(graphData);
        }
        setLabelAxises();
    }

    private void drawChart() {
        if (testType == TEST_TYPE_STRIPPING) {
            graphView.clear();
            graphView.addGraphData(graphStripping1);
            if (strippingIndex == 7) {
                graphView.addGraphData(graphStripping2);
            }
        }
        graphView.drawGraph();
    }

    private void setLabelAxises() {
        String labelXAxis = "";
        String labelYAxis = "";

        if (testType == TEST_TYPE_STRIPPING) {
            labelXAxis = getString(R.string.strippingXAxis);
            labelYAxis = getString(R.string.strippingYAxis);
        }
        else {
            switch (currentAxes) {
                case 0:
                    graphData = graphTP;
                    labelXAxis = getString(R.string.chartAxisTime);
                    labelYAxis = getString(R.string.chartAxisPotential);
                    break;
                case 1:
                    graphData = graphTC;
                    labelXAxis = getString(R.string.chartAxisTime);
                    labelYAxis = getString(R.string.chartAxisCurrent);
                    break;
                case 2:
                    graphData = graphPC;
                    labelXAxis = getString(R.string.chartAxisPotential);
                    labelYAxis = getString(R.string.chartAxisCurrent);
                    break;
            }
            graphView.clear();
            graphView.addGraphData(graphData);
        }
        graphView.setAxisName(labelXAxis, labelYAxis);
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
