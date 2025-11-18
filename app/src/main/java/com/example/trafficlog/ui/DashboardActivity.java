package com.example.trafficlog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficlog.R;
import com.example.trafficlog.data.AppDatabase;
import com.example.trafficlog.data.VehicleDao;
import com.example.trafficlog.model.Vehicle;
import com.example.trafficlog.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements VehicleAdapter.OnVehicleClickListener {

    private Button btnFilterAll;
    private Button btnFilterDueSoon;
    private Button btnFilterOverdue;
    private RecyclerView recyclerVehicles;
    private FloatingActionButton fabAddVehicle;

    private VehicleAdapter adapter;
    private final List<VehicleItem> allVehicles = new ArrayList<>();

    private static final int DAYS_DUE_SOON = 30;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterDueSoon = findViewById(R.id.btnFilterDueSoon);
        btnFilterOverdue = findViewById(R.id.btnFilterOverdue);
        recyclerVehicles = findViewById(R.id.recyclerVehicles);
        fabAddVehicle = findViewById(R.id.fabAddVehicle);

        recyclerVehicles.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VehicleAdapter(new ArrayList<VehicleItem>(), this);
        recyclerVehicles.setAdapter(adapter);

        btnFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllVehicles();
            }
        });

        btnFilterDueSoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByStatus(StatusType.DUE_SOON);
            }
        });

        btnFilterOverdue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByStatus(StatusType.OVERDUE);
            }
        });

        fabAddVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddVehicleActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehiclesFromDb();
    }

    private void loadVehiclesFromDb() {
        final int userId = SessionManager.getLoggedInUserId(this);
        if (userId <= 0) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                VehicleDao vehicleDao = db.vehicleDao();
                List<Vehicle> vehicles = vehicleDao.getVehiclesForUser(userId);

                final List<VehicleItem> items = new ArrayList<>();
                for (Vehicle v : vehicles) {
                    VehicleItem item = mapVehicleToItem(v);
                    items.add(item);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        allVehicles.clear();
                        allVehicles.addAll(items);
                        showAllVehicles();
                    }
                });
            }
        }).start();
    }

    private VehicleItem mapVehicleToItem(Vehicle v) {
        // Choose the earliest non-empty due date among registration, insurance, MOT, tax
        String[] candidates = new String[]{
                v.registrationExpiry,
                v.insuranceExpiry,
                v.motNextDate,
                v.taxNextDate
        };

        long todayMillis = System.currentTimeMillis();
        Long bestDateMillis = null;
        String bestDateString = null;

        for (String s : candidates) {
            if (s == null || s.trim().isEmpty()) continue;
            Long millis = parseDateMillis(s);
            if (millis == null) continue;
            if (bestDateMillis == null || millis < bestDateMillis) {
                bestDateMillis = millis;
                bestDateString = s;
            }
        }

        String statusText;
        String dateText;
        StatusType statusType;

        if (bestDateMillis == null) {
            statusText = "No due dates set";
            dateText = "";
            statusType = StatusType.UP_TO_DATE;
        } else {
            long diffDays = (bestDateMillis - todayMillis) / (1000 * 60 * 60 * 24);
            if (diffDays < 0) {
                statusType = StatusType.OVERDUE;
                statusText = "Overdue by " + Math.abs(diffDays) + " days";
            } else if (diffDays <= DAYS_DUE_SOON) {
                statusType = StatusType.DUE_SOON;
                statusText = "Due in " + diffDays + " days";
            } else {
                statusType = StatusType.UP_TO_DATE;
                statusText = "Next due in " + diffDays + " days";
            }
            dateText = "Next due: " + bestDateString;
        }

        String displayName = v.licensePlate;
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = v.type != null ? v.type : "Vehicle";
        }

        return new VehicleItem(
                v.id,
                displayName,
                statusText,
                dateText,
                statusType,
                v.type,      // Pass vehicle type for fallback icon
                v.imageUri   // Pass vehicle image URI
        );
    }

    private Long parseDateMillis(String s) {
        try {
            Date date = dateFormat.parse(s);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            // ignore, treat as no date
        }
        return null;
    }

    private void showAllVehicles() {
        adapter.updateItems(new ArrayList<VehicleItem>(allVehicles));
    }

    private void filterByStatus(StatusType statusType) {
        List<VehicleItem> filtered = new ArrayList<>();
        for (VehicleItem item : allVehicles) {
            if (item.statusType == statusType) {
                filtered.add(item);
            }
        }
        adapter.updateItems(filtered);
    }

    @Override
    public void onVehicleClick(VehicleItem item) {
        Intent intent = new Intent(this, VehicleDetailsActivity.class);
        intent.putExtra("vehicleId", item.id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear session
        SessionManager.clearSession(this);

        // Navigate to Welcome screen
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    public enum StatusType {
        UP_TO_DATE,
        DUE_SOON,
        OVERDUE
    }

    public static class VehicleItem {
        public final int id;
        public final String name;
        public final String statusText;
        public final String dateText;
        public final StatusType statusType;
        public final String vehicleType;  // Car, Motorcycle, Other
        public final String imageUri;      // Vehicle image URI

        public VehicleItem(int id, String name, String statusText, String dateText, StatusType statusType, String vehicleType, String imageUri) {
            this.id = id;
            this.name = name;
            this.statusText = statusText;
            this.dateText = dateText;
            this.statusType = statusType;
            this.vehicleType = vehicleType;
            this.imageUri = imageUri;
        }
    }
}