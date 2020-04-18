package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import club.infolab.isc.database.DBRecords;

public class MainActivity extends AppCompatActivity {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new TabTestsFragment(), "Tests");
        adapter.addFragment(new TabHistoryFragment(), "History");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
//        long time = System.currentTimeMillis() / 1000;

        DBRecords db = new DBRecords(this);
//        Date currentTime = Calendar.getInstance().getTime();
//        db.insert("Sinusoid", currentTime.toString(), 0, "");
//        db.insert("S3oid", currentTime.toString(), 1, "");
//        db.insert("Ddd", "91229", 0, "lolol");
//        db.insert("Kek", "8888888", 1, "323");
//
//        Log.d("DB_TEST", db.select(1).getName() + " " + db.select(1).getDate());
//        Log.d("DB_TEST", db.select(2).getName() + " " + db.select(2).getDate());
//        Log.d("DB_TEST", db.select(3).getName() + " " + db.select(3).getDate());
//        Log.d("DB_TEST", db.select(4).getName() + " " + db.select(4).getDate());
    }
}
