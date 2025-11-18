package com.example.trafficlog.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficlog.R;
import com.example.trafficlog.ui.DashboardActivity.StatusType;
import com.example.trafficlog.ui.DashboardActivity.VehicleItem;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    public interface OnVehicleClickListener {
        void onVehicleClick(VehicleItem item);
    }

    private List<VehicleItem> items;
    private final OnVehicleClickListener listener;

    public VehicleAdapter(List<VehicleItem> items, OnVehicleClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<VehicleItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        final VehicleItem item = items.get(position);

        holder.tvName.setText(item.name);
        holder.tvStatus.setText(item.statusText);
        holder.tvDate.setText(item.dateText);

        // Color status text based on status type
        int colorRes;
        if (item.statusType == StatusType.UP_TO_DATE) {
            colorRes = android.R.color.holo_green_dark;
        } else if (item.statusType == StatusType.DUE_SOON) {
            colorRes = android.R.color.holo_orange_dark;
        } else { // OVERDUE
            colorRes = android.R.color.holo_red_dark;
        }
        holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes)
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onVehicleClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvStatus;
        TextView tvDate;

        VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVehicleName);
            tvStatus = itemView.findViewById(R.id.tvVehicleStatus);
            tvDate = itemView.findViewById(R.id.tvVehicleDate);
        }
    }
}
