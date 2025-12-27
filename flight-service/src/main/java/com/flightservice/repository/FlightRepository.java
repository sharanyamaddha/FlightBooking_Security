package com.flightservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.flightservice.model.Flight;

@Repository
public interface FlightRepository extends MongoRepository<Flight, String> {

    Flight findByAirlineIdAndSourceAndDestinationAndDepartureDateTime(
            String airlineId,
            String source,
            String destination,
            LocalDateTime departureDateTime
    );

    List<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDateTimeBetween(
            String source,
            String destination,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Flight> findByAirlineIdIgnoreCase(String airlineId);

    boolean existsByAirlineIdAndSourceAndDestinationAndDepartureDateTime(
            String airlineId,
            String source,
            String destination,
            LocalDateTime departureDateTime
    );
    
    @Query("select distinct f.source from Flight f")
    List<Flight> findDistinctSources();
    
    @Query("select distinct f.destination from Flight f")
    List<Flight> findDistinctDestinations();
    
    Optional<Flight> findById(String id);

}
