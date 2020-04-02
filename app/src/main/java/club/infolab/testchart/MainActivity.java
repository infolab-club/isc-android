package club.infolab.testchart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TestAdapter testAdapter;
    private ArrayList<String> tests = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setInitialData();
        recyclerView = findViewById(R.id.tests_list);

        testAdapter = new TestAdapter(this, tests);
        recyclerView.setAdapter(testAdapter);
    }

    private void setInitialData(){
        tests.add("Cyclic");
        tests.add("Linear sweep");
        tests.add("Sinusoid");
        tests.add("Constant voltage");
        tests.add("Chronoamperometry");
        tests.add("Square wave");
    }
}
