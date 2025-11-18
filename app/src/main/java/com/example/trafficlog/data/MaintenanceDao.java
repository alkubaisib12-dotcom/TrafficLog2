package com.example.trafficlog.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.trafficlog.model.MaintenanceEntry;

@Dao
public interface MaintenanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMaintenance(MaintenanceEntry entry);

    @Update
    void updateMaintenance(MaintenanceEntry entry);

    @Query("SELECT * FROM maintenance_entries WHERE vehicle_id = :vehicleId LIMIT 1")
    MaintenanceEntry getMaintenanceForVehicle(int vehicleId);

    @Query("DELETE FROM maintenance_entries WHERE vehicle_id = :vehicleId")
    void deleteMaintenanceForVehicle(int vehicleId);
}