package club.infolab.isc.test.simulation;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

import club.infolab.isc.test.CurrentTest;
import club.infolab.isc.test.MomentTest;

public class TestSimulation {
    private final int PERIOD = 10;
    private TestSimulationCallback callback;
    private ArrayList<MomentTest> testSimulationResult = new ArrayList<>();
    private int indexCurrentTest = 0;
    private Handler handler = new Handler();

    public void startSimulation(Context context, TestSimulationCallback callback, int testIndex) {
        testSimulationResult = CurrentTest.GetTestsFromFiles(context, testIndex);
        this.callback = callback;
        handler.postDelayed(timeUpdaterRunnable, PERIOD);
    }

    public void stopSimulation() {
        callback = null;
        indexCurrentTest = 0;
        testSimulationResult.clear();
        handler.removeCallbacks(timeUpdaterRunnable);
    }

    private Runnable timeUpdaterRunnable = new Runnable() {
        public void run() {
            callback.getTestData(testSimulationResult.get(indexCurrentTest));
            if (indexCurrentTest < testSimulationResult.size() - 1) {
                indexCurrentTest++;
                handler.postDelayed(this, PERIOD);
            }
            else {
                stopSimulation();
            }
        }
    };
}
