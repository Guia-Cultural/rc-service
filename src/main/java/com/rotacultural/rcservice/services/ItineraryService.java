package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.itineraries.ItineraryReq;
import com.rotacultural.rcservice.dtos.itineraries.ItineraryResp;
import com.rotacultural.rcservice.entities.Itinerary;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.repositories.ItineraryRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripsRepository tripsRepository;
    private final PlacesRepository placesRepository;

    public ItineraryService(
            ItineraryRepository itineraryRepository,
            TripsRepository tripsRepository,
            PlacesRepository placesRepository
    ) {
        this.itineraryRepository = itineraryRepository;
        this.tripsRepository = tripsRepository;
        this.placesRepository = placesRepository;
    }

    public ItineraryResp create(ItineraryReq req) {
        Trips trip = getTrip(req.getTripId());
        Places place = getPlace(req.getLocId());

        validateDay(trip, req.getDay());

        Itinerary itinerary = new Itinerary();
        itinerary.setTrip(trip);
        itinerary.setLocale(place);
        itinerary.setDay(req.getDay());
        itinerary.setDescription(req.getDescription());

        return toResponse(itineraryRepository.save(itinerary));
    }

    public List<ItineraryResp> getAll() {
        return itineraryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ItineraryResp getById(Long id) {
        return toResponse(getItinerary(id));
    }

    public ItineraryResp update(Long id, ItineraryReq req) {
        Itinerary itinerary = getItinerary(id);

        Trips trip = getTrip(req.getTripId());
        Places place = getPlace(req.getLocId());

        validateDay(trip, req.getDay());

        itinerary.setTrip(trip);
        itinerary.setLocale(place);
        itinerary.setDay(req.getDay());
        itinerary.setDescription(req.getDescription());

        return toResponse(itineraryRepository.save(itinerary));
    }

    public void delete(Long id) {
        Itinerary itinerary = getItinerary(id);
        itineraryRepository.delete(itinerary);
    }

    private Itinerary getItinerary(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Itinerário não encontrado"
                ));
    }

    private Trips getTrip(Long id) {
        return tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Viagem não encontrada"
                ));
    }

    private Places getPlace(Long id) {
        return placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Local não encontrado"
                ));
    }

    private void validateDay(Trips trip, LocalDate day) {
        if (trip.getStartDate() != null
                && day.isBefore(trip.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O dia do itinerário não pode ser anterior ao início da viagem"
            );
        }

        if (trip.getEndDate() != null
                && day.isAfter(trip.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O dia do itinerário não pode ser posterior ao fim da viagem"
            );
        }
    }

    private ItineraryResp toResponse(Itinerary itinerary) {
        ItineraryResp response = new ItineraryResp();

        response.setId(itinerary.getIdItinerario());
        response.setTripId(itinerary.getTrip().getIdTrip());
        response.setDay(itinerary.getDay());
        response.setLocId(itinerary.getLocale().getIdLoc());
        response.setDescription(itinerary.getDescription());

        return response;
    }
}