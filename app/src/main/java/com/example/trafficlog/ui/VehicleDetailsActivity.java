package com.example.trafficlog.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trafficlog.R;
import com.example.trafficlog.data.AppDatabase;
import com.example.trafficlog.data.VehicleDao;
import com.example.trafficlog.model.Vehicle;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VehicleDetailsActivity extends AppCompatActivity {

    private MaterialCardView cardVehicleImage;
    private ImageView ivVehicleImage;

    private TextView tvType;
    private TextView tvLicensePlate;

    private TextView tvRegistrationDate;
    private TextView tvRegistrationExpiry;

    private TextView tvInsuranceCompany;
    private TextView tvInsuranceExpiry;

    private TextView tvMotLastDate;
    private TextView tvMotNextDate;

    private TextView tvTaxLastDate;
    private TextView tvTaxNextDate;

    private TextView tvStatusSummary;

    private Button btnLogMaintenance;

    private int vehicleId;
    private Vehicle vehicle;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vehicle Details");
        }

        cardVehicleImage = findViewById(R.id.cardVehicleImage);
        ivVehicleImage = findViewById(R.id.ivVehicleImage);

        tvType = findViewById(R.id.tvType);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvRegistrationDate = findViewById(R.id.tvRegistrationDate);
        tvRegistrationExpiry = findViewById(R.id.tvRegistrationExpiry);
        tvInsuranceCompany = findViewById(R.id.tvInsuranceCompany);
        tvInsuranceExpiry = findViewById(R.id.tvInsuranceExpiry);
        tvMotLastDate = findViewById(R.id.tvMotLastDate);
        tvMotNextDate = findViewById(R.id.tvMotNextDate);
        tvTaxLastDate = findViewById(R.id.tvTaxLastDate);
        tvTaxNextDate = findViewById(R.id.tvTaxNextDate);
        tvStatusSummary = findViewById(R.id.tvStatusSummary);
        btnLogMaintenance = findViewById(R.id.btnLogMaintenance);

        vehicleId = getIntent().getIntExtra("vehicleId", -1);
        if (vehicleId == -1) {
            Toast.makeText(this, "Invalid vehicle", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadVehicleDetails();

        btnLogMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VehicleDetailsActivity.this, MaintenanceActivity.class);
                intent.putExtra("vehicleId", vehicleId);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadVehicleDetails() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                VehicleDao vehicleDao = db.vehicleDao();
                vehicle = vehicleDao.getVehicleById(vehicleId);

                if (vehicle == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VehicleDetailsActivity.this,
                                    "Vehicle not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayVehicleDetails();
                    }
                });
            }
        }).start();
    }

    private void displayVehicleDetails() {
        // Display vehicle image if available
        if (vehicle.imageUri != null && !vehicle.imageUri.trim().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(vehicle.imageUri);
                ivVehicleImage.setImageURI(imageUri);
                cardVehicleImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                // If image loading fails, hide the image card
                cardVehicleImage.setVisibility(View.GONE);
            }
        } else {
            cardVehicleImage.setVisibility(View.GONE);
        }

        tvType.setText(vehicle.type != null ? vehicle.type : "N/A");
        tvLicensePlate.setText(vehicle.licensePlate != null ? vehicle.licensePlate : "N/A");

        tvRegistrationDate.setText(formatDateOrNA(vehicle.registrationDate));
        tvRegistrationExpiry.setText(formatDateOrNA(vehicle.registrationExpiry));

        tvInsuranceCompany.setText(vehicle.insuranceCompany != null ? vehicle.insuranceCompany : "N/A");
        tvInsuranceExpiry.setText(formatDateOrNA(vehicle.insuranceExpiry));

        tvMotLastDate.setText(formatDateOrNA(vehicle.motLastDate));
        tvMotNextDate.setText(formatDateOrNA(vehicle.motNextDate));

        tvTaxLastDate.setText(formatDateOrNA(vehicle.taxLastDate));
        tvTaxNextDate.setText(formatDateOrNA(vehicle.taxNextDate));

        // Calculate and display status summary
        calculateStatusSummary();
    }

    private String formatDateOrNA(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "Not set";
        }
        return dateStr;
    }

    private void calculateStatusSummary() {
        String[] candidates = new String[]{
                vehicle.registrationExpiry,
                vehicle.insuranceExpiry,
                vehicle.motNextDate,
                vehicle.taxNextDate
        };

        String[] labels = new String[]{
                "Registration",
                "Insurance",
                "MOT",
                "Tax"
        };

        long todayMillis = System.currentTimeMillis();
        Long bestDateMillis = null;
        String bestLabel = null;
        String bestDateString = null;

        for (int i = 0; i < candidates.length; i++) {
            String s = candidates[i];
            if (s == null || s.trim().isEmpty()) continue;
            Long millis = parseDateMillis(s);
            if (millis == null) continue;
            if (bestDateMillis == null || millis < bestDateMillis) {
                bestDateMillis = millis;
                bestLabel = labels[i];
                bestDateString = s;
            }
        }

        if (bestDateMillis == null) {
            tvStatusSummary.setText("No upcoming due dates set");
            tvStatusSummary.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            long diffDays = (bestDateMillis - todayMillis) / (1000 * 60 * 60 * 24);
            String statusText;
            int color;

            if (diffDays < 0) {
                statusText = bestLabel + " overdue by " + Math.abs(diffDays) + " days (" + bestDateString + ")";
                color = getResources().getColor(android.R.color.holo_red_dark);
            } else if (diffDays <= 30) {
                statusText = bestLabel + " due in " + diffDays + " days (" + bestDateString + ")";
                color = getResources().getColor(android.R.color.holo_orange_dark);
            } else {
                statusText = "Next due: " + bestLabel + " in " + diffDays + " days (" + bestDateString + ")";
                color = getResources().getColor(android.R.color.holo_green_dark);
            }

            tvStatusSummary.setText(statusText);
            tvStatusSummary.setTextColor(color);
        }
    }

    private Long parseDateMillis(String s) {
        try {
            Date date = dateFormat.parse(s);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            // ignore
        }
        return null;
    }
}