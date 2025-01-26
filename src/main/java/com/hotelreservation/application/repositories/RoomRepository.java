package com.hotelreservation.application.repositories;

import com.hotelreservation.application.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByFloorAndIsOccupiedFalse(Integer floor);
    List<Room> findByIsOccupiedFalse();

    List<Room> findByIsOccupiedTrue();
}
