package com.example.trafficlog.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "maintenance_entries",
        foreignKeys = @ForeignKey(
                entity = Vehicle.class,
                parentColumns = "id",
                childColumns = "vehicle_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("vehicle_id")}
)
public class MaintenanceEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "vehicle_id")
    public int vehicleId;

    @ColumnInfo(name = "engine_oil_last_changed")
    public String engineOilLastChanged;

    @ColumnInfo(name = "engine_oil_interval")
    public String engineOilInterval;

    @ColumnInfo(name = "tires_last_changed")
    public String tiresLastChanged;

    @ColumnInfo(name = "tires_mileage")
    public String tiresMileage;

    @ColumnInfo(name = "maintenance_last_service")
    public String maintenanceLastService;

    @ColumnInfo(name = "spark_plugs_last_replaced")
    public String sparkPlugsLastReplaced;

    @ColumnInfo(name = "spark_plugs_interval")
    public String sparkPlugsInterval;
}