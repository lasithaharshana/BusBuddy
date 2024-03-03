package com.example.BusBuddy.controllers;

import com.example.BusBuddy.dto.Trip.TripAddForDurationRequest;

import com.example.BusBuddy.dto.Trip.TripAddRequest;
import com.example.BusBuddy.dto.Trip.TripResponse;
import com.example.BusBuddy.repositories.TripRepository;
import com.example.BusBuddy.services.TripService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/trip")
public class TripController {

    private final TripService tripService;
    private final TripRepository tripRepository;

    @PostMapping("/scheduleTripsForDuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addTripsForDuration(HttpServletRequest httpServletRequest, @RequestBody TripAddForDurationRequest tripAddForDurationRequest){
        return tripService.addTripsForDuration(httpServletRequest ,tripAddForDurationRequest);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addTrip(HttpServletRequest httpServletRequest, @RequestBody TripAddRequest tripAddRequest , @RequestParam LocalDate date){
        return tripService.addTrip(httpServletRequest, tripAddRequest, date);
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> remove(@RequestParam Long tripId){
        return tripService.remove(tripId);
    }

    @GetMapping("/findByDriver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<TripResponse>> findTripByDriver(HttpServletRequest httpServletRequest, @RequestParam @NotNull LocalDate date){
        return tripService.findTripByDriver(httpServletRequest , date);
    }


}
