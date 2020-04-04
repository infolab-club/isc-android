package club.infolab.testchart;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LoadingActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    private static int REQUEST_ENABLE_BLUETOOTH = 0;
    private boolean wasLogoShow;
    private boolean wasBluetoothEnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ImageView imageView = findViewById(R.id.loading_logo);
        Picasso.get().load(R.drawable.isc_logo).into(imageView);

        //Считывание файла и последующая разбивка данных на объекты
        Reader();
        //Проверка Bluetooth
        checkBluetoothEnable();
    }

    private void checkBluetoothEnable() {
        BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            wasBluetoothEnable = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    wasLogoShow = true;
                    startMainActivity();
                }
            }, SPLASH_TIME_OUT);
        }
        else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkBluetoothEnable();
    }

    private void startMainActivity() {
        if (wasLogoShow && wasBluetoothEnable) {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void Reader() {
        CurrentTest currentTest = new CurrentTest();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("sample_input/cyclic.txt")));
            String line = reader.readLine();
            currentTest.AppendMomentTest(line);
            while (line != null){
                currentTest.AppendMomentTest(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentTest.AppendMomentTest("0");
    }
}



