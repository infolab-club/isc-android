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
import club.infolab.isc.retrofit.RetrofitCallback;
import club.infolab.isc.retrofit.RetrofitController;
import club.infolab.isc.test.CurrentTest;
import es.dmoral.toasty.Toasty;

public class TabHistoryFragment extends Fragment
        implements HistoryAdapter.OnHistoryListener, RetrofitCallback {
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private ArrayList<History> histories = new ArrayList<>();
    private DBRecords db;
    private View rootView;
    private int indexHistory = -1;
    private Button buttonUpload;
    private Button viewBtn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_history, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        initializeFragment();
        super.onResume();
    }

    @Override
    public void onPause() {
        clear();
        super.onPause();
    }

    private void initializeFragment() {
        recyclerView = rootView.findViewById(R.id.tab_history_list);
        buttonUpload = rootView.findViewById(R.id.buttonUpload);
        buttonUpload.setOnClickListener(onClickUpload);
        buttonUpload.setClickable(false);
        buttonUpload.setAlpha(.5f);
        viewBtn = rootView.findViewById(R.id.view_btn);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (indexHistory > -1) {
                    Intent i = new Intent(getContext(), GraphActivity.class);
                    i.putExtra(GraphActivity.EXTRA_TEST_INDEX, indexHistory);
                    i.putExtra(GraphActivity.EXTRA_TEST_NAME, db.select(indexHistory).getName());
                    i.putExtra(GraphActivity.EXTRA_TEST_TYPE, GraphActivity.TEST_TYPE_HISTORY);
                    startActivity(i);
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        });
        viewBtn.setClickable(false);
        viewBtn.setAlpha(.5f);
        db = new DBRecords(getContext());
        setHistoryData();
        historyAdapter = new HistoryAdapter(getActivity(), histories, this);
        recyclerView.setAdapter(historyAdapter);
    }

    private void clear() {
        int size = histories.size();
        histories.clear();
        if (historyAdapter != null) {
            historyAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    private void setHistoryData() {
        histories.clear();
        for (long i = db.getCountRows(); i >= 1; i--) {
            Record r = db.select(i);
            int isLoaded = r.getIsLoaded();
            histories.add(new History(r.getName(), r.getDate(), isLoaded));
            Log.d("HISTORY", "Added " + i + " History");
        }
    }

    @Override
    public void onHistoryClick(int position) {
        indexHistory = (int) db.getCountRows() - position;
        viewBtn.setClickable(true);
        viewBtn.setAlpha(1f);
        if (histories.get(position).getIsLoaded() == 0){
            buttonUpload.setClickable(true);
            buttonUpload.setAlpha(1f);
        } else {
            buttonUpload.setClickable(false);
            buttonUpload.setAlpha(.5f);
        }
    }

    private View.OnClickListener onClickUpload = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (indexHistory > -1) {
                int isUpload = db.select(indexHistory).getIsLoaded();
                if (isUpload == 0) {
                    String type = db.select(indexHistory).getName();
                    String date = db.select(indexHistory).getDate();
                    String json = db.select(indexHistory).getJson();
                    String results = CurrentTest.convertJsonToString(json);

                    RetrofitController retrofitController =
                            new RetrofitController(TabHistoryFragment.this);
                    retrofitController.addTest(type, date, results, indexHistory);

                    buttonUpload.setClickable(false);
                }
            }
        }
    };

    @Override
    public void onSuccess(int index) {
        String type = db.select(index).getName();
        String date = db.select(index).getDate();
        String json = db.select(index).getJson();
        Record record = new Record(index, type, date, 1, json);
        db.update(record);
        Toasty.custom(getContext(), R.string.success_toast,
                null, R.color.toast, Toasty.LENGTH_SHORT,
                false, true).show();
        setHistoryData();
        historyAdapter.notifyDataSetChanged();
        if (index == indexHistory) {
            buttonUpload.setClickable(false);
            buttonUpload.setAlpha(.5f);
        }
    }
}
