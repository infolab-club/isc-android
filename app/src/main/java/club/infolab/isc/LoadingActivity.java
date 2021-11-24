package club.infolab.isc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public  class LoadingActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 1500;
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private boolean wasLogoShow;
    private boolean wasBluetoothEnable;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ImageView imageView = findViewById(R.id.loading_logo);
        Picasso.get().load(R.drawable.isc_logo).into(imageView);
        startSplashScrin();
    }

    private void startSplashScrin() {
        new Handler().postDelayed(() -> {
            wasLogoShow = true;
            startMainActivity();
        }, SPLASH_TIME_OUT);
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
        // checkBluetoothEnable();
    }


    private void startMainActivity() {
        if (wasLogoShow) { // && wasBluetoothEnable
            Intent intent = new Intent(LoadingActivity.this, UsbActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0,0);
        }
    }
}
