package club.infolab.isc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.SerialTimeoutException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.vk59.graphviewlibrary.GraphData;
import com.vk59.graphviewlibrary.GraphView;
import com.vk59.graphviewlibrary.Moment;

import org.json.JSONException;
import org.json.JSONObject;

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
import club.infolab.isc.usb.Constants;
import club.infolab.isc.usb.CustomProber;
import club.infolab.isc.usb.SerialListener;
import club.infolab.isc.usb.SerialService;
import club.infolab.isc.usb.SerialSocket;
import club.infolab.isc.usb.TextUtil;
import club.infolab.isc.usb.UsbController;
import es.dmoral.toasty.Toasty;

public class GraphActivity extends AppCompatActivity
        implements TestSimulationCallback, BluetoothCallback, ServiceConnection, SerialListener {
    public static final String EXTRA_TEST_NAME = "TEST_NAME";
    public static final String EXTRA_TEST_INDEX = "TEST_INDEX";
    public static final String EXTRA_TEST_PARAMS = "TEST_PARAMS";
    public static final String EXTRA_TEST_PARAM_RANGE = "TEST_PARAM_RANGE";
    public static final String EXTRA_TEST_PARAM_RATE = "TEST_PARAM_RATE";
    public static final int TEST_TYPE_BLUETOOTH = 0;
    public static final int TEST_TYPE_SIMULATION = 1;
    public static final int TEST_TYPE_HISTORY = 2;
    public static final int TEST_TYPE_STRIPPING = 3;
    public static final int TEST_TYPE_USB = 4;
    public static final String EXTRA_TEST_TYPE = "TEST_TYPE";

    private int testType;
    private String testName;
    private int testIndex;
    private String testParams;
    private String testParamRange;
    private String testParamRate;

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
    private BluetoothController bluetoothController;

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

    // USB
    private enum Connected { False, Pending, True }
    private BroadcastReceiver broadcastReceiver;
    private int deviceId, portNum;
    private int baudRate = 19200;
    private UsbSerialPort usbSerialPort;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean controlLinesEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;
    private String TAG_USB = "USB_DATA";

    public GraphActivity() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        deviceId = UsbController.device.getDeviceId();
        portNum = UsbController.port;
        initializeActivity();
        // startSimulation();
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        stopService(new Intent(this, SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            startService(new Intent(this, SerialService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_GRANT_USB));
        Log.d(TAG_USB, "BRODCAST");
        if (initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    private void startUsbConnect() {
        bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG_USB, "PRESTART");
                if(Constants.INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        Log.d(TAG_USB, "START");
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if(device == null) {
            status("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            status("connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            status("connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                status("connection failed: permission denied");
            else
                status("connection failed: open failed");
            return;
        }

        connected = Connected.Pending;
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialSocket socket = new SerialSocket(getApplicationContext(), usbConnection, usbSerialPort);
            service.connect(socket);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
        usbSerialPort = null;
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            Log.d(TAG_USB, msg);
            service.write(data);
        } catch (SerialTimeoutException e) {
            status("write timeout: " + e.getMessage());
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        if(hexEnabled) {
            Log.d(TAG_USB, "h + " + TextUtil.toHexString(data));
        } else {
            String msg = new String(data);
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
//                if (pendingNewline && msg.charAt(0) == '\n') {
//                    Editable edt = receiveText.getEditableText();
//                    if (edt != null && edt.length() > 1)
//                        edt.replace(edt.length() - 2, edt.length(), "");
//                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            String message = String.valueOf(TextUtil.toCaretString(msg, newline.length() != 0));
            try {
                JSONObject json = new JSONObject(message);
                if (json.has("t") && json.has("v") && json.has("i")) {
                    onGetUsbData(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG_USB, message);
        }
    }

    void status(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSerialConnect() {
        // status("connected");
        connected = Connected.True;
        startSimulation();
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
        Log.d(TAG_USB, "ERROR");
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    public void onGetUsbData(String data) {
        MomentTest testData = CurrentTest.getMomentFromJson(data);
        prepareNewData(testData);
        drawChart();
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
            case (TEST_TYPE_USB):
                if (service != null && !isChangingConfigurations()) {
                    service.detach();
                }
                // unbindService(this);
                super.onStop();
                break;
        }
        CurrentTest.results.clear();
        super.onStop();
    }

    private void initializeActivity() {
        getInfoTest();
        startUsbConnect();
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
        testParams = intent.getStringExtra(EXTRA_TEST_PARAMS);
        testParamRange = intent.getStringExtra(EXTRA_TEST_PARAM_RANGE);
        testParamRate = intent.getStringExtra(EXTRA_TEST_PARAM_RATE);
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
                        bluetoothController.sendData("stop");
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
        Log.d(TAG_USB, String.valueOf(testType));
        switch (testType) {
            case TEST_TYPE_BLUETOOTH:
                bluetoothController = new BluetoothController(this);
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
            case TEST_TYPE_USB:
                if (testName.equals("Cyclic")) testName = "cyclic";
                else if (testName.equals("Linear sweep")) testName = "linearSweep";
                else if (testName.equals("Sinusoid")) testName = "sinusoid";
                else if (testName.equals("Constant voltage")) testName = "constant";
                else if (testName.equals("Chronoamperometry")) testName = "chronoamp";
                else if (testName.equals("Square wave")) testName = "multiStep";
                send(testParamRange);
                send(testParamRate);
                send("{\"command\":\"setParam\",\"test\":\"" + testName + "\",\"param\":" + testParams + "}");
                send("{\"command\": \"runTest\", \"test\": \"" + testName + "\"}");
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
