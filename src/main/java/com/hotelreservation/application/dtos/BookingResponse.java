package com.hotelreservation.application.dtos;

import lombok.Data;

import java.util.List;

@Data
public class BookingResponse {
    private List<Integer> bookedRoomNumbers;
    private int totalTravelTime;
}
