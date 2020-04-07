package club.infolab.testchart.test_simulation;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

import club.infolab.testchart.CurrentTest;
import club.infolab.testchart.GraphActivity;
import club.infolab.testchart.MomentTest;

public class TestSimulation {
    private final int PERIOD = 10;
    private TestSimulationCallback callback;
    private ArrayList<MomentTest> testSimulationResult = new ArrayList<>();
    private int indexCurrentTest = 0;
    private Handler handler = new Handler();

    public void startSimulation(Context context, TestSimulationCallback callback, int testIndex) {
        CurrentTest currentTest = new CurrentTest(context);
        currentTest.Reader(testIndex);
        testSimulationResult = (ArrayList) CurrentTest.testResult.clone();
        CurrentTest.testResult.clear();
        this.callback = callback;
        handler.postDelayed(timeUpdaterRunnable, PERIOD);
    }

    public void stopSimulation() {
        testSimulationResult.clear();
        handler.removeCallbacks(timeUpdaterRunnable);
    }

    private Runnable timeUpdaterRunnable = new Runnable() {
        public void run() {
            indexCurrentTest = indexCurrentTest % testSimulationResult.size();
            callback.getTestData(testSimulationResult.get(indexCurrentTest));
            indexCurrentTest++;
            handler.postDelayed(this, PERIOD);
        }
    };
}
