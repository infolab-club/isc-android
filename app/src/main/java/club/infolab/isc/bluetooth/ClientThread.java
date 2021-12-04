package club.infolab.isc.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientThread extends Thread {
    private BluetoothSocket globalSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler handler;

    public ClientThread(Handler handler, BluetoothSocket socket) {
        this.handler = handler;
        globalSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = globalSocket.getInputStream();
        }
        catch (IOException e) {
            Log.d("BLUETOOTH_DATA", "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = globalSocket.getOutputStream();
        }
        catch (IOException e) {
            Log.d("BLUETOOTH_DATA", "Error occurred when creating output stream", e);
        }
        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int numBytes;

        while (true) {
            try {
                numBytes = inputStream.read(buffer);
                handler.obtainMessage(BluetoothController.STATE_MESSAGE_RECEIVED, numBytes,
                        -1, buffer).sendToTarget();
                Log.d("BLUETOOTH_DATA", "read: " + Arrays.toString(buffer));
            }
            catch (IOException e) {
                Log.d("BLUETOOTH_DATA", "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
            Log.d("BLUETOOTH_DATA", new String(bytes, StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            Log.d("BLUETOOTH_DATA", "Error occurred when sending data", e);
        }
    }

    public void cancel() {
        try {
            globalSocket.close();
        }
        catch (IOException e) {
            Log.d("BLUETOOTH_DATA", "Could not close the connect socket", e);
        }
    }
}
