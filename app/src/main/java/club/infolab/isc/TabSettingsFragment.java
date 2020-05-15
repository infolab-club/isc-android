package club.infolab.isc;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import static club.infolab.isc.AppLocale.getCurrentLanguage;
import static club.infolab.isc.R.color.white;

public class TabSettingsFragment extends Fragment {
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initializeFragment();
        return rootView;
    }
    private void initializeFragment(){
        Log.d("TAG", getCurrentLanguage());
        Button buttonRu = rootView.findViewById(R.id.buttonRu);
        Button buttonEn = rootView.findViewById(R.id.buttonEn);
        buttonEn.setOnClickListener(onButtonEnClick);
        buttonRu.setOnClickListener(onButtonRuClick);
        if (getCurrentLanguage().equals("ru")) {
            buttonRu.setBackground(getResources().getDrawable(R.drawable.style_button_blue));
            buttonRu.setTextColor(getResources().getColor(white));
        }
        if (getCurrentLanguage().equals("en")) {
            buttonEn.setBackground(getResources().getDrawable(R.drawable.style_button_blue));
            buttonEn.setTextColor(getResources().getColor(white));
        }
    }

    private View.OnClickListener onButtonRuClick = new View.OnClickListener() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            setLocale("ru");
        }
    };

    private View.OnClickListener onButtonEnClick = new View.OnClickListener() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            setLocale("en");

        }
    };

    private void setLocale(String lang) {
        AppLocale appLocale = new AppLocale(getContext());
        appLocale.changeAppLocale(lang);
        getActivity().onConfigurationChanged(appLocale.getLocalConfiguration());
    }
}


