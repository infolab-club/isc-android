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
    private  AppLocale appLocale;
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    static final String APP_PREFERENCES_LANGUAGE = "appLanguage";
    static final String APP_PREFERENCES = "mySettings";
    static final String APP_PREFERENCES_START = "firstRun";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        appLocale = new AppLocale(this);
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ImageView imageView = findViewById(R.id.loading_logo);
        Picasso.get().load(R.drawable.isc_logo).into(imageView);
        isFirstStart();

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

    private void isFirstStart() {
        if (sharedPreferences.getBoolean(APP_PREFERENCES_START, true)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.choose_language)
                    .setPositiveButton(R.string.language_russian, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            appLocale.changeAppLocale("ru");
                            checkBluetoothEnable();
                        }
                    })
                    .setNegativeButton(R.string.language_english, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            appLocale.changeAppLocale("en");
                            checkBluetoothEnable();
                        }
                    }).setCancelable(false)
                    .show();
            sharedPreferences.edit().putBoolean(APP_PREFERENCES_START, false).apply();
        }
        else if (!sharedPreferences.getBoolean(APP_PREFERENCES_START, false)) {
            appLocale.checkLanguage();
            checkBluetoothEnable();
        }
    }
}
