package com.hotelreservation.application.controllers;

import com.hotelreservation.application.dtos.BookingRequest;
import com.hotelreservation.application.dtos.BookingResponse;
import com.hotelreservation.application.services.RoomBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomBookingController {

    @Autowired
    private RoomBookingService roomBookingService;

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookRooms(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(roomBookingService.bookRooms(request));
    }

    @GetMapping("/getBookedRooms")
    public ResponseEntity<List<Integer>> getBookedRooms() {
        return ResponseEntity.ok(roomBookingService.getBookedRooms());
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetRooms() {
        roomBookingService.resetRoomOccupancy();
        return ResponseEntity.ok("All rooms reset successfully");
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeRooms() {
        roomBookingService.initializeRooms();
        return ResponseEntity.ok("Rooms initialized successfully");
    }

    @PostMapping("/bookRandom")
    public ResponseEntity<List<Integer>> bookRandomRooms() {
        roomBookingService.bookRandomRooms();
        return ResponseEntity.ok(roomBookingService.bookRandomRooms());
    }
}

