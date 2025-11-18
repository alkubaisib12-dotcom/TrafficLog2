package com.example.trafficlog.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class Vehicle {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "type")
    public String type;   // "Car", "Motorcycle", "Other"

    @ColumnInfo(name = "image_uri")
    public String imageUri;

    @ColumnInfo(name = "license_plate")
    public String licensePlate;

    @ColumnInfo(name = "registration_date")
    public String registrationDate;

    @ColumnInfo(name = "registration_expiry")
    public String registrationExpiry;

    @ColumnInfo(name = "insurance_company")
    public String insuranceCompany;

    @ColumnInfo(name = "insurance_expiry")
    public String insuranceExpiry;

    @ColumnInfo(name = "mot_last_date")
    public String motLastDate;

    @ColumnInfo(name = "mot_next_date")
    public String motNextDate;

    @ColumnInfo(name = "tax_last_date")
    public String taxLastDate;

    @ColumnInfo(name = "tax_next_date")
    public String taxNextDate;
}
