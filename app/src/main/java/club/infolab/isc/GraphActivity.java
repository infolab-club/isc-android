package club.infolab.isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.MomentTest;
import club.infolab.isc.test.simulation.TestSimulation;
import club.infolab.isc.test.simulation.TestSimulationCallback;

public class GraphActivity extends AppCompatActivity implements TestSimulationCallback {
    public static final String EXTRA_TEST = "testName";
    public static final String EXTRA_INDEX = "testIndex";
    LineChart chart;
    TestSimulation testSimulation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        String testName = intent.getStringExtra(EXTRA_TEST);
        TextView nameTestView = findViewById(R.id.name_test);
        nameTestView.setText(testName);

        chart = findViewById(R.id.chart);

        int indexTest = intent.getIntExtra(EXTRA_INDEX, 0);
        testSimulation = new TestSimulation();
        testSimulation.startSimulation(this, this, indexTest);
    }

    @Override
    public void getTestData(MomentTest testData) {
        CurrentTest.results.add(testData);
        drawChart();
    }

    private void drawChart() {
        chart.clear();
        List<Entry> entries = new ArrayList<>();
        for (MomentTest moment : CurrentTest.results) {
            // turn your data into Entry objects
            entries.add(new Entry(moment.getTime(), moment.getVoltage()));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    @Override
    protected void onStop() {
        testSimulation.stopSimulation();
        super.onStop();
    }
}
