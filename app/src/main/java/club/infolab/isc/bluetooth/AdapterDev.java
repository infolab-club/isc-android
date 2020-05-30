package club.infolab.isc.bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import club.infolab.isc.R;


public class AdapterDev extends RecyclerView.Adapter<AdapterDev.ViewHolder> {
    private Context context;
    private onDeviceListener onDeviceListener;
    private LayoutInflater inflater;
    private ArrayList<String> dataBase;
    private ArrayList<String> status;
    private int selectedPosition = -1;

    public AdapterDev(Context context, ArrayList data, ArrayList status, onDeviceListener onDeviceListener){
        this.context = context;
        this.onDeviceListener = onDeviceListener;
        this.dataBase = data;
        this.status = status;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view, onDeviceListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textDeviceName.setText(dataBase.get(position));
        holder.textDeviceStatus.setText(status.get(position));
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
        return dataBase.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textDeviceName;
        final TextView textDeviceStatus;
        onDeviceListener onDeviceListener;
        View checkItem;

        ViewHolder(View view, final onDeviceListener onDeviceListener){
            super(view);
            textDeviceName = view.findViewById(R.id.textDeviceName);
            textDeviceStatus = view.findViewById(R.id.textDeviceStatus);
            this.onDeviceListener = onDeviceListener;
            checkItem = view.findViewById(R.id.checkItemDevice);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onDeviceListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onDeviceListener.onDeviceClick(position);
                            selectedPosition = position;
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    public interface onDeviceListener {
        void onDeviceClick(int position);
    }
}
