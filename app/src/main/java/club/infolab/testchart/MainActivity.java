package club.infolab.testchart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TestAdapter.OnTestListener {
    private RecyclerView recyclerView;
    private TestAdapter testAdapter;
    private ArrayList<String> tests = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setInitialData();
        recyclerView = findViewById(R.id.tests_list);

        testAdapter = new TestAdapter(this, tests, this);
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

    @Override
    public void onTestClick(int position) {
        CurrentTest.testResult.clear();
//        CurrentTest currentTest = new CurrentTest(this);
//        currentTest.Reader(position);
        Intent test_intent = new Intent(this, GraphActivity.class);
        String testName = tests.get(position);
        test_intent.putExtra(GraphActivity.EXTRA_TEST, testName);
        test_intent.putExtra(GraphActivity.EXTRA_INDEX, position);
        startActivity(test_intent);
    }
}
