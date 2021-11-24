package club.infolab.isc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import club.infolab.isc.usb.CustomProber;
import club.infolab.isc.usb.UsbController;

public class UsbActivity extends AppCompatActivity {
    private TextView textUsb;
    private ProgressBar progressUsb;
    private  AppLocale appLocale;
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    static final String APP_PREFERENCES_LANGUAGE = "appLanguage";
    static final String APP_PREFERENCES = "mySettings";
    static final String APP_PREFERENCES_START = "firstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanUsbDevices();
    }

    private void init() {
        appLocale = new AppLocale(this);
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isFirstStart();

        textUsb = findViewById(R.id.textHeaderUsb);
        progressUsb = findViewById(R.id.progressUsb);
        updateVisibility();
        scanUsbDevices();
    }

    private void updateVisibility() {
        int visibility = UsbController.isConnect ? View.INVISIBLE : View.VISIBLE;
        textUsb.setVisibility(visibility);
        progressUsb.setVisibility(visibility);
    }

    private void scanUsbDevices() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            UsbController.device = device;
            UsbController.port = 0;
            UsbController.driver = driver;
            UsbController.isConnect = true;
            updateVisibility();
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    private void isFirstStart() {
        if (sharedPreferences.getBoolean(APP_PREFERENCES_START, true)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.choose_language)
                    .setPositiveButton(R.string.language_russian, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            appLocale.changeAppLocale("ru");
                        }
                    })
                    .setNegativeButton(R.string.language_english, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            appLocale.changeAppLocale("en");
                        }
                    }).setCancelable(false)
                    .show();
            sharedPreferences.edit().putBoolean(APP_PREFERENCES_START, false).apply();
        }
        else if (!sharedPreferences.getBoolean(APP_PREFERENCES_START, false)) {
            appLocale.checkLanguage();
        }
    }
}