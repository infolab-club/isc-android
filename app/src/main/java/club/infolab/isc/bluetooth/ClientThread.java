package club.infolab.isc.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

public  class ClientThread extends AsyncTask<Void, Void, Void> {
    BluetoothDevice device;
    TextView textView;
    public void setDevice(BluetoothDevice device, TextView textView){
        this.device = device;
        this.textView = textView;
    }

    final UUID MY_UUID = UUID.fromString("9f2c4ce3-0801-42d1-ba41-1a6bfe1ccb70");
    BluetoothSocket btSocket = null;
    BluetoothAdapter myBluetooth;

    @Override
    protected Void doInBackground(Void... voids) {
        boolean ConnectSuccess = true;
        try
        {
            if (btSocket == null)
            {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(device.getAddress());//connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
                textView.setText("Connect " + device.getName());
            }
        }
        catch (IOException e)
        {
            textView.setText("Not Connect");
            ConnectSuccess = false;//if the try failed, you can check the exception here

        }
        return null;
    }

}










