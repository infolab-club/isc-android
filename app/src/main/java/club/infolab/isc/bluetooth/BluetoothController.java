package club.infolab.isc.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class BluetoothController implements BluetoothSocketCallback {
    public static final int STATE_MESSAGE_RECEIVED = 0;
    private static BluetoothCallback callback;
    private static BluetoothSocket socket;
    private static ConnectThread connection;
    private static ClientThread client;

    public BluetoothController(BluetoothCallback callback) {
        this.callback = callback;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public void startClient() {
        client = new ClientThread(handler, socket);
        client.start();
    }

    public void connectToDevice(BluetoothDevice device) {
        connection = new ConnectThread(this, device);
        connection.start();
    }

    public void sendData(String data) {
        client.write(data.getBytes());
    }

    @Override
    public void getSocket(BluetoothSocket socket) {
        setSocket(socket);
        startClient();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == STATE_MESSAGE_RECEIVED) {
                byte[] readBuffer = (byte[]) msg.obj;
                String tempMsg = new String(readBuffer, 0, msg.arg1);
                Log.d("DATA", tempMsg);
                callback.getInputData(tempMsg);
            }
            return true;
        }
    });
}
