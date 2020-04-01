package club.infolab.testchart;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static int REQUEST_ENABLE_BT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckEnable();
    }

    public void CheckEnable(){
        BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (! bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent ( BluetoothAdapter . ACTION_REQUEST_ENABLE );
            startActivityForResult ( enableBtIntent, REQUEST_ENABLE_BT );
        }
    }
}
