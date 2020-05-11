package club.infolab.isc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class LoadingActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 0 ;
    private static int SPLASH_TIME_OUT = 1500;
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private boolean wasLogoShow;
    private boolean wasBluetoothEnable;
//    private String language;
//    private SharedPreferences.Editor editor;
//    public static final String APP_PREFERENCES_LANGUAGE = "ru";
//    public SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

//        settings = getSharedPreferences(APP_PREFERENCES_LANGUAGE, Context.MODE_PRIVATE);
//        language = settings.getString(APP_PREFERENCES_LANGUAGE, "ru");
//        setLocale(language);

        ImageView imageView = findViewById(R.id.loading_logo);
        Picasso.get().load(R.drawable.isc_logo).into(imageView);
        checkPermission();
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
            Intent intent = new Intent(LoadingActivity.this, BluetoothActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0,0);
        }
    }

    private void checkPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    checkPermission();
                }
                return;
        }
    }

//    public void setLocale(String language) {
//        Locale myLocale = new Locale(language);
//        Resources resources = getResources();
//        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
//        Configuration configuration = resources.getConfiguration();
//        configuration.locale = myLocale;
//        resources.updateConfiguration(configuration, displayMetrics);
//    }
}
