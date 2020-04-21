package club.infolab.isc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private Context context;
    private OnTestListener mOnTestListener;
    private LayoutInflater inflater;
    private ArrayList<String> tests;
    private int selectedPosition = -1;

    TestAdapter(Context context, OnTestListener onTestListener, ArrayList<String> tests) {
        this.context = context;
        this.mOnTestListener = onTestListener;
        this.tests = tests;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_test, parent, false);
        return new ViewHolder(view, mOnTestListener);
    }

    @Override
    public void onBindViewHolder(TestAdapter.ViewHolder holder, int position) {
        holder.testNameView.setText(tests.get(position));
        if (selectedPosition == position) {
            holder.checkItem.setBackground(context.getResources()
                    .getDrawable(R.drawable.style_check_on));
        }
        else {
            holder.checkItem.setBackground(context.getResources()
                    .getDrawable(R.drawable.style_check_off));
        }
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView testNameView;
        View checkItem;

        ViewHolder(View view, final OnTestListener onTestListener) {
            super(view);
            testNameView = view.findViewById(R.id.test_name_item);
            checkItem = view.findViewById(R.id.checkItemTest);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onTestListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onTestListener.onTestClick(position);
                            selectedPosition = position;
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    public interface OnTestListener {
        void onTestClick(int position);
    }
}