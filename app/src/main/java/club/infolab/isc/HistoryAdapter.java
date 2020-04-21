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
    private Context context;
    private OnHistoryListener onHistoryClickListener;
    private LayoutInflater inflater;
    private List<History> histories;
    private int selectedPosition = -1;

    HistoryAdapter(Context context, List<History> histories, OnHistoryListener onHistoryListener) {
        this.context = context;
        this.onHistoryClickListener = onHistoryListener;
        this.histories = histories;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view, onHistoryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = histories.get(position);
        holder.nameView.setText(history.getName());
        holder.dateView.setText(history.getDate());
        holder.isLoadedView.setText(history.getIsLoaded());
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
        return histories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView nameView, dateView, isLoadedView;
        View checkItem;
        public ViewHolder(@NonNull View view, final OnHistoryListener onHistoryListener) {
            super(view);
            nameView = itemView.findViewById(R.id.history_test_name_item);
            dateView = itemView.findViewById(R.id.date_of_test);
            isLoadedView = itemView.findViewById(R.id.is_loaded);
            checkItem = view.findViewById(R.id.checkItemHistory);
            onHistoryClickListener = onHistoryListener;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onHistoryListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onHistoryListener.onHistoryClick(position);
                            selectedPosition = position;
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    public interface OnHistoryListener{
        void onHistoryClick(int position);
    }
}
