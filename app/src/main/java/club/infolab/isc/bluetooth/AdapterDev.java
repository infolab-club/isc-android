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
    private LayoutInflater inflater;
    private ArrayList<String> dataBase;
    private OnTestListener mOnTestListener;

    public AdapterDev(Context context, ArrayList data, OnTestListener onTestListener){
        this.dataBase = data;
        this.inflater = LayoutInflater.from(context);
        this.mOnTestListener = onTestListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view, mOnTestListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textDeviceName.setText(dataBase.get(position));
    }

    @Override
    public int getItemCount() {
        return dataBase.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView textDeviceName;
        OnTestListener onTestListener;

        ViewHolder(View view, OnTestListener onTestListener){
            super(view);
            textDeviceName = view.findViewById(R.id.textDeviceName);
            this.onTestListener = onTestListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onTestListener.onDeviceClick(getAdapterPosition());
        }
    }

    public interface OnTestListener{
        void onDeviceClick(int position);
    }
}
