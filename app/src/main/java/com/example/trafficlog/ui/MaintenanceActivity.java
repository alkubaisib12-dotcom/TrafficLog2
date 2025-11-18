package com.example.trafficlog.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trafficlog.R;
import com.example.trafficlog.data.AppDatabase;
import com.example.trafficlog.data.MaintenanceDao;
import com.example.trafficlog.model.MaintenanceEntry;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class MaintenanceActivity extends AppCompatActivity {

    private TextInputEditText etEngineOilLastChanged;
    private TextInputEditText etEngineOilInterval;
    private TextInputEditText etTiresLastChanged;
    private TextInputEditText etTiresMileage;
    private TextInputEditText etMaintenanceLastService;
    private TextInputEditText etSparkPlugsLastReplaced;
    private TextInputEditText etSparkPlugsInterval;

    private Button btnSaveMaintenance;

    private int vehicleId;
    private MaintenanceEntry existingEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Maintenance Log");
        }

        etEngineOilLastChanged = findViewById(R.id.etEngineOilLastChanged);
        etEngineOilInterval = findViewById(R.id.etEngineOilInterval);
        etTiresLastChanged = findViewById(R.id.etTiresLastChanged);
        etTiresMileage = findViewById(R.id.etTiresMileage);
        etMaintenanceLastService = findViewById(R.id.etMaintenanceLastService);
        etSparkPlugsLastReplaced = findViewById(R.id.etSparkPlugsLastReplaced);
        etSparkPlugsInterval = findViewById(R.id.etSparkPlugsInterval);
        btnSaveMaintenance = findViewById(R.id.btnSaveMaintenance);

        vehicleId = getIntent().getIntExtra("vehicleId", -1);
        if (vehicleId == -1) {
            Toast.makeText(this, "Invalid vehicle", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup date pickers for date fields
        setupDateField(etEngineOilLastChanged);
        setupDateField(etTiresLastChanged);
        setupDateField(etMaintenanceLastService);
        setupDateField(etSparkPlugsLastReplaced);

        loadExistingMaintenance();

        btnSaveMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMaintenance();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupDateField(final TextInputEditText editText) {
        editText.setFocusable(false);
        editText.setKeyListener(null);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(editText);
            }
        });
    }

    private void showDatePicker(final TextInputEditText target) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int m, int d) {
                        String formatted = String.format(Locale.UK, "%04d-%02d-%02d", y, m + 1, d);
                        target.setText(formatted);
                    }
                },
                year, month, day
        );
        dialog.show();
    }

    private void loadExistingMaintenance() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                MaintenanceDao maintenanceDao = db.maintenanceDao();
                existingEntry = maintenanceDao.getMaintenanceForVehicle(vehicleId);

                if (existingEntry != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateFields();
                        }
                    });
                }
            }
        }).start();
    }

    private void populateFields() {
        if (existingEntry == null) return;

        setText(etEngineOilLastChanged, existingEntry.engineOilLastChanged);
        setText(etEngineOilInterval, existingEntry.engineOilInterval);
        setText(etTiresLastChanged, existingEntry.tiresLastChanged);
        setText(etTiresMileage, existingEntry.tiresMileage);
        setText(etMaintenanceLastService, existingEntry.maintenanceLastService);
        setText(etSparkPlugsLastReplaced, existingEntry.sparkPlugsLastReplaced);
        setText(etSparkPlugsInterval, existingEntry.sparkPlugsInterval);
    }

    private void setText(TextInputEditText editText, String value) {
        if (value != null && !value.trim().isEmpty()) {
            editText.setText(value);
        }
    }

    private void saveMaintenance() {
        final String engineOilLastChanged = getTextOrEmpty(etEngineOilLastChanged);
        final String engineOilInterval = getTextOrEmpty(etEngineOilInterval);
        final String tiresLastChanged = getTextOrEmpty(etTiresLastChanged);
        final String tiresMileage = getTextOrEmpty(etTiresMileage);
        final String maintenanceLastService = getTextOrEmpty(etMaintenanceLastService);
        final String sparkPlugsLastReplaced = getTextOrEmpty(etSparkPlugsLastReplaced);
        final String sparkPlugsInterval = getTextOrEmpty(etSparkPlugsInterval);

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                MaintenanceDao maintenanceDao = db.maintenanceDao();

                MaintenanceEntry entry;
                if (existingEntry != null) {
                    entry = existingEntry;
                } else {
                    entry = new MaintenanceEntry();
                    entry.vehicleId = vehicleId;
                }

                entry.engineOilLastChanged = engineOilLastChanged;
                entry.engineOilInterval = engineOilInterval;
                entry.tiresLastChanged = tiresLastChanged;
                entry.tiresMileage = tiresMileage;
                entry.maintenanceLastService = maintenanceLastService;
                entry.sparkPlugsLastReplaced = sparkPlugsLastReplaced;
                entry.sparkPlugsInterval = sparkPlugsInterval;

                if (existingEntry != null) {
                    maintenanceDao.updateMaintenance(entry);
                } else {
                    maintenanceDao.insertMaintenance(entry);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MaintenanceActivity.this,
                                "Maintenance saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }).start();
    }

    private String getTextOrEmpty(TextInputEditText editText) {
        if (editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }
}