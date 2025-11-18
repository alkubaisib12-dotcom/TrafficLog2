package com.example.trafficlog.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        // Load vehicle image if available, otherwise show icon based on type
        if (item.imageUri != null && !item.imageUri.trim().isEmpty()) {
            try {
                // Load actual vehicle image
                Uri imageUri = Uri.parse(item.imageUri);
                holder.ivIcon.setImageURI(imageUri);
                holder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                // If image loading fails, show fallback icon
                setFallbackIcon(holder.ivIcon, item.vehicleType);
            }
        } else {
            // No image uploaded, show icon based on vehicle type
            setFallbackIcon(holder.ivIcon, item.vehicleType);
        }

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

    private void setFallbackIcon(ImageView imageView, String vehicleType) {
        int iconRes;
        if ("Car".equalsIgnoreCase(vehicleType)) {
            iconRes = android.R.drawable.ic_menu_mylocation;  // Car icon
        } else if ("Motorcycle".equalsIgnoreCase(vehicleType)) {
            iconRes = android.R.drawable.ic_menu_compass;  // Motorcycle icon
        } else {
            iconRes = android.R.drawable.ic_dialog_info;  // Other/generic icon
        }
        imageView.setImageResource(iconRes);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIcon;
        TextView tvName;
        TextView tvStatus;
        TextView tvDate;

        VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivVehicleIcon);
            tvName = itemView.findViewById(R.id.tvVehicleName);
            tvStatus = itemView.findViewById(R.id.tvVehicleStatus);
            tvDate = itemView.findViewById(R.id.tvVehicleDate);
        }
    }
}
