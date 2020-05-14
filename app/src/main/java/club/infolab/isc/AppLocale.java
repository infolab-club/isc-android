package club.infolab.isc;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

class AppLocale {
    private Resources resources;
    private DisplayMetrics displayMetrics;
    private Configuration configuration;
    private Context context;
    private static String currentLanguage;

    AppLocale(Context context) {
        this.context = context;
    }

    Configuration getLocalConfiguration() {
        return configuration;
    }

    static String getCurrentLanguage() {
        return currentLanguage;
    }

    void changeAppLocale(String currentLanguage) {
        this.currentLanguage = currentLanguage;
        Locale locale = new Locale(currentLanguage);
        resources = context.getResources();
        displayMetrics = resources.getDisplayMetrics();
        configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, displayMetrics);
        LoadingActivity.saveConfiguration(currentLanguage);
    }

}
