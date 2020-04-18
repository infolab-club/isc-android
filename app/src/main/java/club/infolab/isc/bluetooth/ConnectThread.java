package club.infolab.isc.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public  class ConnectThread extends Thread {
    /** MY_UUID это UUID, который используется и в сервере. */
    private final UUID MY_UUID = UUID.fromString("9f2c4ce3-0801-42d1-ba41-1a6bfe1ccb70");
    private BluetoothSocketCallback socketCallback;
    private BluetoothSocket localSocket;
    private BluetoothAdapter bluetoothAdapter;

    public ConnectThread(BluetoothSocketCallback socketCallback, BluetoothDevice device) {
        this.socketCallback = socketCallback;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket tmp = null;

        try {
            tmp= device.createRfcommSocketToServiceRecord(MY_UUID);
        }
        catch (IOException ignored) {}

        localSocket = tmp;
        Log.d("TAG", localSocket.toString());
    }

    public void run(){
        bluetoothAdapter.cancelDiscovery();

        try {
            localSocket.connect();
            Log.d("TAG", "Connect");
        }
        catch (IOException connectException) {
            try {
                localSocket.close();
            }
            catch(IOException ignored){}
        }

        socketCallback.getSocket(localSocket);
    }

    public BluetoothSocket getSocket() {
        return localSocket;
    }

    public void cancel(){
        try{
            localSocket.close();
        }
        catch(IOException e) {
            Log.e("TAG", "Could not close the connect socket", e);
        }
    }
}
