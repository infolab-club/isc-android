package club.infolab.isc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import club.infolab.isc.test.CurrentTest;

public class TabTestsFragment extends Fragment implements TestAdapter.OnTestListener{
    private RecyclerView recyclerView;
    private TestAdapter testAdapter;
    private ArrayList<String> tests = new ArrayList<>();;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        setInitialData();
        recyclerView = view.findViewById(R.id.tab_tests_list);

        testAdapter = new TestAdapter(getActivity(), tests, this);
        recyclerView.setAdapter(testAdapter);
        return view;
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
        CurrentTest.results.clear();
//        CurrentTest currentTest = new CurrentTest(this);
//        currentTest.Reader(position);
        Intent test_intent = new Intent(TabTestsFragment.this.getActivity(), ParamsActivity.class);
        String testName = tests.get(position);
        test_intent.putExtra(GraphActivity.EXTRA_TEST, testName);
        test_intent.putExtra(GraphActivity.EXTRA_INDEX, position);
        startActivity(test_intent);
    }
}
