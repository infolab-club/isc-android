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
    private OnTestListener mOnTestListener;

    TestAdapter(Context context, ArrayList<String> tests, OnTestListener onTestListener) {
        this.tests = tests;
        this.inflater = LayoutInflater.from(context);
        this.mOnTestListener = onTestListener;
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.test_item, parent, false);
        return new ViewHolder(view, mOnTestListener);
    }

    @Override
    public void onBindViewHolder(TestAdapter.ViewHolder holder, int position) {
        holder.testNameView.setText(tests.get(position));
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView testNameView;
        OnTestListener onTestListener;

        ViewHolder(View view, OnTestListener onTestListener){
            super(view);
            testNameView = view.findViewById(R.id.test_name_item);
            this.onTestListener = onTestListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onTestListener.onTestClick(getAdapterPosition());
        }
    }

    public interface OnTestListener{
        void onTestClick(int position);
    }
}