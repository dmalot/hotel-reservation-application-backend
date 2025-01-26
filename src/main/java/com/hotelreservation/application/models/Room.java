package com.hotelreservation.application.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Room {

    @Id
    private Integer roomNumber;
    private Integer floor;
    private boolean isOccupied;

    public Room(Integer roomNumber, Integer floor) {
        this.roomNumber = roomNumber;
        this.floor = floor;
    }

    public boolean getOccupied() {
        return this.isOccupied;
    }
}
