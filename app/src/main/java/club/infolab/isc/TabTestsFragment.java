package club.infolab.isc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import club.infolab.isc.bluetooth.BluetoothController;
import club.infolab.isc.test.CurrentTest;

public class TabTestsFragment extends Fragment implements TestAdapter.OnTestListener {
    private TestAdapter testAdapter;
    private ArrayList<String> tests = new ArrayList<>();
    private int indexTest;
    private Button buttonSetParams;
    private Button buttonDemo;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tests, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        initializeFragment();
        super.onResume();
        Log.d("STATUS", "Resume_Test");
    }

    @Override
    public void onPause() {
        clear();
        Log.d("STATUS", "Pause_Test");
        super.onPause();
    }

    private void initializeFragment() {
        setInitialData();
        RecyclerView recyclerView = rootView.findViewById(R.id.tab_tests_list);
        testAdapter = new TestAdapter(getActivity(), this, tests);
        recyclerView.setAdapter(testAdapter);

        buttonSetParams = rootView.findViewById(R.id.set_params_btn);
        buttonSetParams.setOnClickListener(onClickSetParams);
        buttonSetParams.setClickable(false);
        buttonSetParams.setAlpha(0.5f);

        buttonDemo = rootView.findViewById(R.id.start_demo_btn);
        buttonDemo.setOnClickListener(onClickStartDemo);
        buttonDemo.setClickable(false);
        buttonDemo.setAlpha(0.5f);
    }

    private void clear() {
        int size = tests.size();
        tests.clear();
        if (testAdapter != null) {
            testAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    private void setInitialData() {
        tests.add("Cyclic");
        tests.add("Linear sweep");
        tests.add("Sinusoid");
        tests.add("Constant voltage");
        // tests.add("Chronoamperometry");
        // tests.add("Square wave");
        // tests.add("Stripping voltammetry");
    }

    @Override
    public void onTestClick(int position) {
        indexTest = position;
        buttonSetParams.setClickable(true);
        buttonSetParams.setAlpha(1f);
        buttonDemo.setClickable(true);
        buttonDemo.setAlpha(1f);
    }

    private View.OnClickListener onClickSetParams = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CurrentTest.results.clear();
            String testName = tests.get(indexTest);

            Intent intent;
            if (indexTest == 6) {
                intent = new Intent(TabTestsFragment.this.getActivity(), GraphActivity.class);
                intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_STRIPPING);
            } else {
                intent = new Intent(TabTestsFragment.this.getActivity(), ParamsActivity.class);
                if (BluetoothController.isBluetoothRun) {
                    intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_BLUETOOTH);
                } else {
                    intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_SIMULATION);
                }
            }
            intent.putExtra(GraphActivity.EXTRA_TEST_NAME, testName);
            intent.putExtra(GraphActivity.EXTRA_TEST_INDEX, indexTest);
            startActivity(intent);
            getActivity().overridePendingTransition(0, 0);
        }
    };

    private View.OnClickListener onClickStartDemo = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CurrentTest.results.clear();
            String testName = tests.get(indexTest);

            Intent intent = new Intent(TabTestsFragment.this.getActivity(), GraphActivity.class);
            if (indexTest == 6) {
                intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_STRIPPING);
            } else {
                intent.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_USB);
            }
            intent.putExtra(GraphActivity.EXTRA_TEST_NAME, testName);
            intent.putExtra(GraphActivity.EXTRA_TEST_INDEX, indexTest);
            startActivity(intent);
            getActivity().overridePendingTransition(0, 0);
        }
    };
}