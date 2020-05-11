package club.infolab.isc;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import club.infolab.isc.database.DBRecords;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static long pressedSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new TabTestsFragment(), " " + getString(R.string.tab_tests) + " ");
        adapter.addFragment(new TabHistoryFragment(), getString(R.string.tab_history));
//        adapter.addFragment(new TabSettingsFragment(), getString(R.string.tab_settings));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        if (pressedSpeed + 200 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toasty.custom(this, R.string.exit_toast,
                    null, R.color.toast, Toasty.LENGTH_SHORT,
                    false, true).show();
        pressedSpeed = System.currentTimeMillis();
    }
}
