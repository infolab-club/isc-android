package club.infolab.isc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import club.infolab.isc.database.DBRecords;
import club.infolab.isc.database.Record;

public class TabHistoryFragment extends Fragment implements HistoryAdapter.OnHistoryListener {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private ArrayList<History> histories = new ArrayList<>();
    private DBRecords db;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, container, false);
        db = new DBRecords(getContext());
        recyclerView = rootView.findViewById(R.id.tab_history_list);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeFragment();
    }

    private void initializeFragment() {
        setHistoryData();
        historyAdapter = new HistoryAdapter(getActivity(), histories, this);
        recyclerView.setAdapter(historyAdapter);
    }

    public void setHistoryData() {
        histories.clear();
        for (int i = 1; i <= db.getCountRows(); i++) {
            Record r = db.select(i);
            String isLoaded = "Not loaded";
            if (r.getIsLoaded() == 1) {
                isLoaded = "Loaded";
            }
            histories.add(new History(r.getName(), r.getDate(), isLoaded));
            Log.d("HISTORY", "Added " + i + " History");
        }
    }

    @Override
    public void onHistoryClick(int position) {
        Intent i = new Intent(getContext(), GraphActivity.class);
        i.putExtra(GraphActivity.EXTRA_TEST, db.select(position).getName());
        i.putExtra(GraphActivity.EXTRA_INDEX, 0);
        startActivity(i);
    }
}
