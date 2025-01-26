package com.hotelreservation.application.services;

import com.hotelreservation.application.dtos.BookingRequest;
import com.hotelreservation.application.dtos.BookingResponse;
import com.hotelreservation.application.models.Room;
import com.hotelreservation.application.repositories.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Book;
import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomBookingService {

    private static final int TOTAL_FLOORS = 10;
    private static final int ROOMS_PER_FLOOR = 10;
    private static final int TOP_FLOOR_ROOMS = 7;
    private static final int MAX_ROOMS_PER_BOOKING = 5;
    private static final int HORIZONTAL_TRAVEL_TIME = 1;
    private static final int VERTICAL_TRAVEL_TIME = 2;
    private static final double OCCUPANCY_RATE = 0.6;
    private static final double TRANSITION_PROBABILITY = 0.2;
    private static final Logger log = LoggerFactory.getLogger(RoomBookingService.class);

    private static final Logger logger = LoggerFactory.getLogger(RoomBookingService.class);

    @Autowired
    private RoomRepository roomRepository;

    public void initializeRooms() {

        if (roomRepository.count() == 0) {
            List<Room> rooms = new ArrayList<>();
            // Create rooms for floor 1-9
            for(int floor = 1; floor <= TOTAL_FLOORS - 1; floor++) {
                for(int room = 1; room <= ROOMS_PER_FLOOR; room++) {
                    int fullRoomNumber = floor * 100 + room;
                    rooms.add(new Room(fullRoomNumber, floor));
                }
            }

            // Create rooms on top floor
            for(int room = 1; room <= TOP_FLOOR_ROOMS; room++) {
                int fullRoomNumber = TOTAL_FLOORS * 100 + room;
                rooms.add(new Room(fullRoomNumber, TOTAL_FLOORS));
            }
            roomRepository.saveAll(rooms);
        }

    }

    private int calculateTravelTime(Room room1, Room room2) {

        if (Objects.equals(room1.getFloor(), room2.getFloor())) {
            return Math.abs(room1.getRoomNumber() % 100 - room2.getRoomNumber() % 100) * HORIZONTAL_TRAVEL_TIME;
        }

        int verticalTime = Math.abs(room1.getFloor() - room2.getFloor()) * VERTICAL_TRAVEL_TIME;
        int horizontalTime = (room1.getRoomNumber() % 100 + room2.getRoomNumber() % 100) * HORIZONTAL_TRAVEL_TIME;
        return verticalTime + horizontalTime;
    }

    // Calculate total travel time for a set of rooms
    private int calculateTotalTravelTime(List<Room> rooms) {
        if (rooms.size() <= 1) return 0;

        int totalTime = 0;
        for (int i = 0; i < rooms.size() - 1; i++) {
            totalTime += calculateTravelTime(rooms.get(i), rooms.get(i + 1));
        }
        return totalTime;
    }

    // Book rooms with optimal placement
    public BookingResponse bookRooms(BookingRequest request) {
        int numberOfRooms = request.getNumberOfRooms();

//        if (numberOfRooms > MAX_ROOMS_PER_BOOKING) {
//            throw new IllegalArgumentException("Cannot book more than " + MAX_ROOMS_PER_BOOKING + " rooms");
//        }

        //Try to book rooms on the same floor first
        List<Room> bookedRooms = bookRoomsOnSameFloor(numberOfRooms);

        // If not, try to book across floors
        if (bookedRooms.isEmpty()) {
            bookedRooms = bookRoomsAcrossFloors(numberOfRooms);
        }

        // Prepare Response
        BookingResponse response = new BookingResponse();
        response.setBookedRoomNumbers(
                bookedRooms.stream()
                        .map(Room::getRoomNumber)
                        .collect(Collectors.toList())
        );
        response.setTotalTravelTime(calculateTotalTravelTime(bookedRooms));

        // Save booked rooms
        bookedRooms.forEach(room -> {
            room.setOccupied(true);
            roomRepository.save(room);
        });

        return response;
    }

    // Book rooms on the same floor
    private List<Room> bookRoomsOnSameFloor(int numberOfRooms) {
        for (int floor = 1; floor <= TOTAL_FLOORS; floor++) {
            List<Room> availableRooms = roomRepository.findByFloorAndIsOccupiedFalse(floor);

            if (availableRooms.size() >= numberOfRooms) {
                // Sort rooms by room number
                availableRooms.sort(Comparator.comparingInt(Room::getRoomNumber));
                return availableRooms.subList(0, numberOfRooms);
            }
        }
        return new ArrayList<>();
    }

    private void backtrack(List<Room> rooms, int k, int start,
                           List<Room> current, List<List<Room>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < rooms.size(); i++) {
            current.add(rooms.get(i));
            backtrack(rooms, k, i + 1, current, combinations);
            current.removeLast();
        }
    }

    // Generate room combinations (recursive approach)
    private List<List<Room>> generateRoomCombinations(List<Room> rooms, int k) {
        List<List<Room>> combinations = new ArrayList<>();
        backtrack(rooms, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    // Book rooms across floors
    private List<Room> bookRoomsAcrossFloors(int numberOfRooms) {
        List<Room> availableRooms = roomRepository.findByIsOccupiedFalse();

        for(Room room: availableRooms) {
            logger.info("available rooms: " + room);
        }
        logger.info("Available rooms count: " + availableRooms.size());

        if (availableRooms.size() < numberOfRooms) {
            return new ArrayList<>();
        }


        // Generate all possible room combinations
        List<List<Room>> combinations = generateRoomCombinations(availableRooms, numberOfRooms);

//        int minimumTotalTravelTime = Integer.MAX_VALUE;
//        List<Room> rooms = new ArrayList<>();
//        for(List<Room> list: combinations) {
//            int totalTravelTime = calculateTotalTravelTime(list);
//            if (totalTravelTime < minimumTotalTravelTime) {
//                minimumTotalTravelTime = totalTravelTime;
//                rooms = list;
//            }
//        }
//
//        return rooms;
//        logger.info("Minimum total travel time is: " + minimumTotalTravelTime);
        return combinations.stream()
                .min(Comparator.comparingInt(this::calculateTotalTravelTime)) // Ensure this method works correctly
                .orElseGet(ArrayList::new);

//        return combinations.stream()
//                .min(Comparator.comparingInt(combination -> calculateTotalTravelTime(combination)))
//                .orElse(new ArrayList<>());
    }

    public List<Integer> getBookedRooms() {
        List<Room> bookedRooms = roomRepository.findByIsOccupiedTrue();

        return bookedRooms.stream()
                .map(Room::getRoomNumber)
                .toList();
    }

    // Reset room occupancy
    @Transactional
    public void resetRoomOccupancy() {
        List<Room> occupiedRooms = roomRepository.findAll().stream()
                .filter(Room::getOccupied)
                .toList();

        occupiedRooms.forEach(room -> {
            room.setOccupied(false);
            roomRepository.save(room);
        });
    }

    // book random number of rooms
    @Transactional
    public List<Integer> bookRandomRooms() {

        List<Room> allRooms = roomRepository.findAll().stream()
                .toList();
        int total_rooms = allRooms.size();
        Random random = new Random();
        for(int i = 0; i < total_rooms; i++) {
            boolean val = random.nextDouble() < OCCUPANCY_RATE;
            allRooms.get(i).setOccupied(val);
        }

        //Simulate state changes using Markov chain
        for(int i = 0; i < 10; i++) { // Simulate 10 times steps
            allRooms = nextState(allRooms);
        }

        roomRepository.saveAll(allRooms);

        BookingResponse response = new BookingResponse();

        return allRooms.stream()
                .filter(Room::getOccupied)
                .map(Room::getRoomNumber)
                .toList();









    }

    // Calculate the next state based on transition probabilities
    private static List<Room> nextState(List<Room> currentStates) {
        List<Room> nextStates = List.copyOf(currentStates);
        Random random = new Random();
        for (int i = 0; i < currentStates.size(); i++) {
            if (random.nextDouble() < TRANSITION_PROBABILITY) {
                // Change state (flip the bit)
                nextStates.get(i).setOccupied(!nextStates.get(i).getOccupied());
            }
        }
        return nextStates;
    }








}
