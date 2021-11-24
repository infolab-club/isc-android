package club.infolab.isc;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

import static club.infolab.isc.UsbActivity.APP_PREFERENCES_LANGUAGE;

class AppLocale {
    private Resources resources;
    private DisplayMetrics displayMetrics;
    private Configuration configuration;
    private Context context;
    private static String currentLanguage;
    AppLocale(Context context) {
        this.context = context;
    }

    void checkLanguage() {
        if (UsbActivity.sharedPreferences.contains(APP_PREFERENCES_LANGUAGE)) {
            changeAppLocale(UsbActivity.sharedPreferences
                    .getString(APP_PREFERENCES_LANGUAGE, "ru"));
        }
    }


    Configuration getLocalConfiguration() {
        return configuration;
    }

    static String getCurrentLanguage() {
        if (currentLanguage == null) {
            currentLanguage = "ru";
        }
        return currentLanguage;
    }

    void changeAppLocale(String language) {
        currentLanguage = language;
        Log.d("TAG", language);
        Log.d("TAG", currentLanguage);
        Locale locale = new Locale(language);
        resources = context.getResources();
        displayMetrics = resources.getDisplayMetrics();
        configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, displayMetrics);
        Log.d("TAG", context.getResources().getConfiguration().locale.toString());
        saveConfiguration(language);
    }

    private void saveConfiguration(String currentLanguage) {
        UsbActivity.editor.putString(APP_PREFERENCES_LANGUAGE, currentLanguage);
        UsbActivity.editor.apply();
    }

}
