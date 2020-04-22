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

import club.infolab.isc.database.DBRecords;
import club.infolab.isc.database.Record;

public class TabHistoryFragment extends Fragment implements HistoryAdapter.OnHistoryListener {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private ArrayList<History> histories = new ArrayList<>();
    private DBRecords db;
    private View rootView;
    private int indexHistory = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, container, false);
        db = new DBRecords(getContext());
        recyclerView = rootView.findViewById(R.id.tab_history_list);
        Button viewBtn = rootView.findViewById(R.id.view_btn);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (indexHistory > -1){
                    Intent i = new Intent(getContext(), GraphActivity.class);
                    i.putExtra(GraphActivity.EXTRA_INDEX, indexHistory);
                    i.putExtra(GraphActivity.EXTRA_TEST, db.select(indexHistory).getName());
                    i.putExtra(GraphActivity.EXTRA_STATUS_GRAPH, "history");
                    startActivity(i);
                }
            }
        });
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
        for (long i = db.getCountRows(); i >= 1; i--) {
            Record r = db.select(i);
            String isLoaded = "Local";
            if (r.getIsLoaded() == 1) {
                isLoaded = "Cloud";
            }
            histories.add(new History(r.getName(), r.getDate(), isLoaded));
            Log.d("HISTORY", "Added " + i + " History");
        }
    }

    @Override
    public void onHistoryClick(int position) {
        indexHistory = (int) db.getCountRows() - position;
//        View view = recyclerView.getChildAt(position);
//        TextView textTestName = view.findViewById(R.id.textHistoryTestName);
//        textTestName.setText(db.select(position).getName());
//        Intent i = new Intent(getContext(), GraphActivity.class);
//        i.putExtra(GraphActivity.EXTRA_TEST, db.select(position).getName());
//        i.putExtra(GraphActivity.EXTRA_INDEX, 0);
//        startActivity(i);
    }
}
