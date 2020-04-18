package club.infolab.isc.bluetooth;

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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import club.infolab.isc.R;

public class BluetoothActivity extends AppCompatActivity
        implements AdapterDev.OnTestListener, BluetoothCallback {
    private BluetoothController bluetoothController;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

    private ArrayList<String> devicesName = new ArrayList<>();
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Button buttonSearch;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        initializeActivity();
        searchPairedDevices();
    }

    private void initializeActivity() {
        bluetoothController = new BluetoothController(this);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterDev(this, devicesName, this);
        recyclerView.setAdapter(adapter);

        buttonSearch = findViewById(R.id.scanBut);
        buttonSearch.setOnClickListener(onClickSearch);

        buttonSend = findViewById(R.id.button);
        buttonSend.setOnClickListener(onClickSend);
    }

    final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device;

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() == null) {
                    setData(device.getAddress());
                }
                else {
                    setData(device.getName());
                }
                bluetoothDevices.add(device);
            }
        }
    };

    private void searchPairedDevices() {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                setData(device.getName());
                bluetoothDevices.add(device);
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);
    }

    public void setData(String deviceName) {
        devicesName.add(deviceName);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceClick(int position) {
        BluetoothDevice device = bluetoothDevices.get(position);
        device.createBond();
        bluetoothController.connectToDevice(device);

//        Intent mainIntent = new Intent(this, MainActivity.class);
//        startActivity(mainIntent);
    }

    private View.OnClickListener onClickSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            devicesName.clear();
            searchPairedDevices();
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }
    };

    private View.OnClickListener onClickSend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String string = "TEst mess";
            bluetoothController.sendData(string);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    public void getInputData(String data) {
        Toast.makeText(BluetoothActivity.this, data, Toast.LENGTH_SHORT).show();
    }
}
