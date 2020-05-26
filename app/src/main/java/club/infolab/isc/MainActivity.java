package club.infolab.isc;

import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    private long pressedSpeed;
    private Fragment history;
    private Fragment tests;
    private Fragment settings;
    private ViewPager viewPager;
    private  TabAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeActivity();
    }

    private void initializeActivity() {
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager.setOffscreenPageLimit(2);
        history = new TabHistoryFragment();
        settings = new TabSettingsFragment();
        tests = new TabTestsFragment();
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(tests, " " + getString(R.string.tab_tests) + " ");
        adapter.addFragment(history, getString(R.string.tab_history));
        adapter.addFragment(settings, getString(R.string.tab_settings));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        setDividerOnTab(tabLayout);
    }

    private void setDividerOnTab(TabLayout tabLayout) {
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(getResources().getColor(R.color.separatorTab));
            drawable.setSize(2, 1);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }
    }

    @Override
    public void onBackPressed() {
        if (pressedSpeed + 200 > System.currentTimeMillis()) {
            super.onBackPressed();
        }
        else {
            Toasty.custom(this, R.string.exit_toast, null,
                    R.color.toast, Toasty.LENGTH_SHORT, false, true).show();
        }
        pressedSpeed = System.currentTimeMillis();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initializeActivity();
        viewPager.getAdapter().notifyDataSetChanged();
        viewPager.setCurrentItem(2);

    }
}
