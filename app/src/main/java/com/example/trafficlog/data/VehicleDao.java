package com.example.trafficlog.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.trafficlog.model.Vehicle;

import java.util.List;

@Dao
public interface VehicleDao {

    @Insert
    long insertVehicle(Vehicle vehicle);

    @Update
    void updateVehicle(Vehicle vehicle);

    @Delete
    void deleteVehicle(Vehicle vehicle);

    @Query("SELECT * FROM vehicles WHERE user_id = :userId")
    List<Vehicle> getVehiclesForUser(int userId);

    @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
    Vehicle getVehicleById(int vehicleId);

    @Query("DELETE FROM vehicles WHERE id = :vehicleId")
    void deleteVehicleById(int vehicleId);
}