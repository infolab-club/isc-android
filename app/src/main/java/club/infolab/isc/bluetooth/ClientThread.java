package club.infolab.isc.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

@SuppressLint("Registered")
public class ClientThread extends BluetoothActivity {

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ClientThread(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("TAG", "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        bluetoothAdapter.cancelDiscovery();

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("TAG", "Could not close the client socket", closeException);
            }
        }

    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("TAG", "Could not close the client socket", e);
        }
    }
}



