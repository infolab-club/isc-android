package club.infolab.isc.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import club.infolab.isc.MainActivity;
import club.infolab.isc.ParamsActivity;
import club.infolab.isc.R;

public class BluetoothActivity extends AppCompatActivity implements AdapterDev.OnTestListener {

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    private ArrayList<String> devicesName = new ArrayList<>();
    ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    Set<BluetoothDevice> bluetoothDeviceSet;


    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    TextView textView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        textView = findViewById(R.id.textView2);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterDev(this, devicesName, this);
        recyclerView.setAdapter(adapter);

        searchPairedDevices();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);
        button = findViewById(R.id.scanBut);
        onButtonClick();
    }

    final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            BluetoothDevice device;

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() == null)
                    setData(device.getAddress());
                else
                    setData(device.getName());
                bluetoothDevices.add(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
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
    }

    public void setData(String deviceN){
        devicesName.add(deviceN);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceClick(int position) {

        BluetoothDevice device = bluetoothDevices.get(position);
        device.createBond();
        Log.d("CLICK", "Click");
        ClientThread clientThread = new ClientThread();
        clientThread.setDevice(device, textView);
        clientThread.execute();
        textView.setText("Click");
//        Intent mainIntent = new Intent(this, MainActivity.class);
//        startActivity(mainIntent);


    }


    public void onButtonClick() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devicesName.clear();
                searchPairedDevices();
                if (bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();
                bluetoothAdapter.startDiscovery();
            }
        });
    }

}
