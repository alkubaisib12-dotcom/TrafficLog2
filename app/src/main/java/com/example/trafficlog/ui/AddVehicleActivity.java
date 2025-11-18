package com.example.trafficlog.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trafficlog.R;
import com.example.trafficlog.data.AppDatabase;
import com.example.trafficlog.data.VehicleDao;
import com.example.trafficlog.model.Vehicle;
import com.example.trafficlog.util.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class AddVehicleActivity extends AppCompatActivity {

    private MaterialCardView cardImagePicker;
    private ImageView ivVehicleImage;
    private LinearLayout layoutImagePlaceholder;
    private Spinner spinnerVehicleType;

    private TextInputLayout tilCustomVehicleType;
    private TextInputEditText etCustomVehicleType;

    private TextInputLayout tilLicensePlate;
    private TextInputLayout tilRegistrationDate;
    private TextInputLayout tilRegistrationExpiry;
    private TextInputLayout tilInsuranceCompany;
    private TextInputLayout tilInsuranceExpiry;
    private TextInputLayout tilMotLastDate;
    private TextInputLayout tilMotNextDate;
    private TextInputLayout tilTaxLastDate;
    private TextInputLayout tilTaxNextDate;

    private TextInputEditText etLicensePlate;
    private TextInputEditText etRegistrationDate;
    private TextInputEditText etRegistrationExpiry;
    private TextInputEditText etInsuranceCompany;
    private TextInputEditText etInsuranceExpiry;
    private TextInputEditText etMotLastDate;
    private TextInputEditText etMotNextDate;
    private TextInputEditText etTaxLastDate;
    private TextInputEditText etTaxNextDate;

    private Button btnSaveVehicle;

    // For storing selected image URI
    private Uri selectedImageUri = null;

    // Modern way to handle image selection (replaces deprecated onActivityResult)
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Show selected image
                        ivVehicleImage.setImageURI(selectedImageUri);
                        ivVehicleImage.setVisibility(View.VISIBLE);
                        layoutImagePlaceholder.setVisibility(View.GONE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Vehicle");
        }

        // Image picker views
        cardImagePicker = findViewById(R.id.cardImagePicker);
        ivVehicleImage = findViewById(R.id.ivVehicleImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);

        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);

        tilCustomVehicleType = findViewById(R.id.tilCustomVehicleType);
        etCustomVehicleType = findViewById(R.id.etCustomVehicleType);

        tilLicensePlate = findViewById(R.id.tilLicensePlate);
        tilRegistrationDate = findViewById(R.id.tilRegistrationDate);
        tilRegistrationExpiry = findViewById(R.id.tilRegistrationExpiry);
        tilInsuranceCompany = findViewById(R.id.tilInsuranceCompany);
        tilInsuranceExpiry = findViewById(R.id.tilInsuranceExpiry);
        tilMotLastDate = findViewById(R.id.tilMotLastDate);
        tilMotNextDate = findViewById(R.id.tilMotNextDate);
        tilTaxLastDate = findViewById(R.id.tilTaxLastDate);
        tilTaxNextDate = findViewById(R.id.tilTaxNextDate);

        etLicensePlate = findViewById(R.id.etLicensePlate);
        etRegistrationDate = findViewById(R.id.etRegistrationDate);
        etRegistrationExpiry = findViewById(R.id.etRegistrationExpiry);
        etInsuranceCompany = findViewById(R.id.etInsuranceCompany);
        etInsuranceExpiry = findViewById(R.id.etInsuranceExpiry);
        etMotLastDate = findViewById(R.id.etMotLastDate);
        etMotNextDate = findViewById(R.id.etMotNextDate);
        etTaxLastDate = findViewById(R.id.etTaxLastDate);
        etTaxNextDate = findViewById(R.id.etTaxNextDate);

        btnSaveVehicle = findViewById(R.id.btnSaveVehicle);

        // Setup image picker click listener
        cardImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Spinner options
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.vehicle_types_array,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(typeAdapter);

        // Show/hide custom vehicle type field when "Other" is selected
        spinnerVehicleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if ("Other".equalsIgnoreCase(selectedType)) {
                    tilCustomVehicleType.setVisibility(View.VISIBLE);
                } else {
                    tilCustomVehicleType.setVisibility(View.GONE);
                    etCustomVehicleType.setText(""); // Clear custom type when not "Other"
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tilCustomVehicleType.setVisibility(View.GONE);
            }
        });

        // Date pickers for all date fields
        setupDateField(etRegistrationDate);
        setupDateField(etRegistrationExpiry);
        setupDateField(etInsuranceExpiry);
        setupDateField(etMotLastDate);
        setupDateField(etMotNextDate);
        setupDateField(etTaxLastDate);
        setupDateField(etTaxNextDate);

        btnSaveVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVehicle();
            }
        });
    }

    // Handle back button press
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Close this activity and return to Dashboard
        return true;
    }

    // Also handle device back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);

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

    private void saveVehicle() {
        final int userId = SessionManager.getLoggedInUserId(this);
        if (userId <= 0) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedType = spinnerVehicleType.getSelectedItem().toString();
        final String customType = getTextOrEmpty(etCustomVehicleType);

        // If "Other" is selected, use custom type instead
        final String type;
        if ("Other".equalsIgnoreCase(selectedType)) {
            if (!TextUtils.isEmpty(customType)) {
                type = customType; // Use custom type (e.g., "Van", "Truck")
            } else {
                type = "Other"; // Fallback to "Other" if custom type is empty
            }
        } else {
            type = selectedType; // Use Car or Motorcycle
        }

        final String licensePlate = getTextOrEmpty(etLicensePlate);
        final String registrationDate = getTextOrEmpty(etRegistrationDate);
        final String registrationExpiry = getTextOrEmpty(etRegistrationExpiry);
        final String insuranceCompany = getTextOrEmpty(etInsuranceCompany);
        final String insuranceExpiry = getTextOrEmpty(etInsuranceExpiry);
        final String motLastDate = getTextOrEmpty(etMotLastDate);
        final String motNextDate = getTextOrEmpty(etMotNextDate);
        final String taxLastDate = getTextOrEmpty(etTaxLastDate);
        final String taxNextDate = getTextOrEmpty(etTaxNextDate);

        // Store image URI as string (or null if no image selected)
        final String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;

        boolean hasError = false;

        // Validate custom vehicle type when "Other" is selected
        if ("Other".equalsIgnoreCase(selectedType) && TextUtils.isEmpty(customType)) {
            tilCustomVehicleType.setError("Please specify the vehicle type");
            hasError = true;
        } else {
            tilCustomVehicleType.setError(null);
        }

        if (TextUtils.isEmpty(licensePlate)) {
            tilLicensePlate.setError("License plate is required");
            hasError = true;
        } else {
            tilLicensePlate.setError(null);
        }

        if (TextUtils.isEmpty(insuranceCompany)) {
            tilInsuranceCompany.setError("Insurance company is required");
            hasError = true;
        } else {
            tilInsuranceCompany.setError(null);
        }

        if (TextUtils.isEmpty(insuranceExpiry)) {
            tilInsuranceExpiry.setError("Insurance expiry is required");
            hasError = true;
        } else {
            tilInsuranceExpiry.setError(null);
        }

        if (TextUtils.isEmpty(motNextDate)) {
            tilMotNextDate.setError("MOT next date is required");
            hasError = true;
        } else {
            tilMotNextDate.setError(null);
        }

        if (TextUtils.isEmpty(taxNextDate)) {
            tilTaxNextDate.setError("Tax next date is required");
            hasError = true;
        } else {
            tilTaxNextDate.setError(null);
        }

        if (hasError) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                VehicleDao vehicleDao = db.vehicleDao();

                Vehicle vehicle = new Vehicle();
                vehicle.userId = userId;
                vehicle.type = type;
                vehicle.imageUri = imageUriString; // Save the image URI
                vehicle.licensePlate = licensePlate;
                vehicle.registrationDate = registrationDate;
                vehicle.registrationExpiry = registrationExpiry;
                vehicle.insuranceCompany = insuranceCompany;
                vehicle.insuranceExpiry = insuranceExpiry;
                vehicle.motLastDate = motLastDate;
                vehicle.motNextDate = motNextDate;
                vehicle.taxLastDate = taxLastDate;
                vehicle.taxNextDate = taxNextDate;

                vehicleDao.insertVehicle(vehicle);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddVehicleActivity.this,
                                "Vehicle saved successfully", Toast.LENGTH_SHORT).show();
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