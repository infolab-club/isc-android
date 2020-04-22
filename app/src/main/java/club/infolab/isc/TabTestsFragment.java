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

import club.infolab.isc.test.CurrentTest;

public class TabTestsFragment extends Fragment implements TestAdapter.OnTestListener{
    private RecyclerView recyclerView;
    private TestAdapter testAdapter;
    private ArrayList<String> tests = new ArrayList<>();
    private int indexTest;
    private Button buttonSetParams;
    private Button buttonDemo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        setInitialData();
        recyclerView = view.findViewById(R.id.tab_tests_list);

        testAdapter = new TestAdapter(getActivity(), this, tests);
        recyclerView.setAdapter(testAdapter);

        buttonSetParams = view.findViewById(R.id.set_params_btn);
        buttonSetParams.setOnClickListener(onClickSetParams);

        buttonDemo = view.findViewById(R.id.start_demo_btn);
        buttonDemo.setOnClickListener(onClickStartDemo);

        return view;
    }

    private void setInitialData(){
        tests.add("Cyclic");
        tests.add("Linear sweep");
        tests.add("Sinusoid");
        tests.add("Constant voltage");
        tests.add("Chronoamperometry");
        tests.add("Square wave");
        tests.add("Stripping voltammetry");
    }

    @Override
    public void onTestClick(int position) {
        indexTest = position;
    }

    private View.OnClickListener onClickSetParams = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CurrentTest.results.clear();

            Intent intent;
            String testName = tests.get(indexTest);
            if (testName.equals("Stripping voltammetry")) {
                intent = new Intent(TabTestsFragment.this.getActivity(), GraphActivity.class);
            }
            else {
                intent = new Intent(TabTestsFragment.this.getActivity(), ParamsActivity.class);
            }
            intent.putExtra(GraphActivity.EXTRA_TEST, testName);
            intent.putExtra(GraphActivity.EXTRA_INDEX, indexTest);
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickStartDemo = new View.OnClickListener() {

        @Override
        public void onClick(View view){
            CurrentTest.results.clear();

            Intent intent;
            String testName = tests.get(indexTest);
            intent = new Intent(TabTestsFragment.this.getActivity(), GraphActivity.class);
            intent.putExtra(GraphActivity.EXTRA_TEST, testName);
            intent.putExtra(GraphActivity.EXTRA_INDEX, indexTest);
            intent.putExtra(GraphActivity.EXTRA_STATUS_GRAPH, "test");
            startActivity(intent);
        }
    };
}
