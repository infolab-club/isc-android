package club.infolab.testchart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<String> tests;

    TestAdapter(Context context, ArrayList<String> tests) {
        this.tests = tests;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.test_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TestAdapter.ViewHolder holder, int position) {
        holder.testNameView.setText(tests.get(position));
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView testNameView;
        ViewHolder(View view){
            super(view);
            testNameView = view.findViewById(R.id.test_name);
        }
    }
}