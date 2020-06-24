package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import club.infolab.isc.bluetooth.AdapterDev;
import club.infolab.isc.bluetooth.BluetoothCallback;
import club.infolab.isc.bluetooth.BluetoothController;
import es.dmoral.toasty.Toasty;

public class BluetoothActivity extends AppCompatActivity
        implements AdapterDev.onDeviceListener, BluetoothCallback {
    private BluetoothController bluetoothController;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

    private ArrayList<String> devicesName = new ArrayList<>();
    private ArrayList<String> devicesStatus = new ArrayList<>();
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private int indexBluetoothDevice = -1;
    private boolean isDeviceBounded;
    private String currentStatus;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button buttonConnect;
    // private Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initializeActivity();
        searchPairedDevices();
    }

    private void initializeActivity() {
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterDev(this, devicesName, devicesStatus, this);
        recyclerView.setAdapter(adapter);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(onClickConnect);
        buttonConnect.setClickable(false);
        buttonConnect.setAlpha(0.5f);
    }



    private void searchPairedDevices() {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("Potentiostat")) {
                     currentStatus = "Bonded";
                    setData(device.getName(), currentStatus);
                    bluetoothDevices.add(device);
                    isDeviceBounded = true;
                }
            }
            if (!isDeviceBounded) {
                currentStatus = "Non bonded";
                setData("Potentiostat", currentStatus);
            }
        }
        setData("Test station", "Bonded");
    }

    public void setData(String deviceName, String status) {
        devicesName.add(deviceName);
        devicesStatus.add(status);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceClick(int position) {
        indexBluetoothDevice = position;
        buttonConnect.setClickable(true);
        buttonConnect.setAlpha(1f);
    }

    private void runBluetoothTest() {
        if (devicesName.get(indexBluetoothDevice).equals("Test station") ) {
            goToMainActivity();
        }
        else {
            bluetoothController = new BluetoothController(this);
            BluetoothDevice device = bluetoothDevices.get(indexBluetoothDevice);
            device.createBond();
            bluetoothController.connectToDevice(device);
            BluetoothController.isBluetoothRun = true;
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    private View.OnClickListener onClickConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (indexBluetoothDevice != -1 && devicesStatus.get(indexBluetoothDevice)
                    .equals("Bonded") || devicesName.get(indexBluetoothDevice).equals("Test station")) {
                runBluetoothTest();
                goToMainActivity();
            }
            else {
                Toasty.custom(getApplicationContext(), R.string.non_bonded_error,
                        null, R.color.toast, Toasty.LENGTH_SHORT,
                        false, true).show();            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGetBluetoothData(String data) {
//        Toast.makeText(BluetoothActivity.this, data, Toast.LENGTH_SHORT).show();
    }
}
