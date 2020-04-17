package club.infolab.isc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<History> histories;
    private OnHistoryListener onHistoryListener;

    HistoryAdapter(Context context, List<History> histories, OnHistoryListener onHistoryListener) {
        this.histories = histories;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.history_item, parent, false);
            return new ViewHolder(view, onHistoryListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = histories.get(position);
        holder.nameView.setText(history.getName());
        holder.dateView.setText(history.getDate());
        holder.isLoadedView.setText(history.getIsLoaded());
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView nameView, dateView, isLoadedView;
        OnHistoryListener onHistoryListener;

        public ViewHolder(@NonNull View itemView, OnHistoryListener onHistoryListener) {
            super(itemView);
            nameView = itemView.findViewById(R.id.history_test_name_item);
            dateView = itemView.findViewById(R.id.date_of_test);
            isLoadedView = itemView.findViewById(R.id.is_loaded);

            this.onHistoryListener = onHistoryListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onHistoryListener.onHistoryClick(getAdapterPosition());
        }
    }
    public interface OnHistoryListener{
        void onHistoryClick(int position);
    }
}
