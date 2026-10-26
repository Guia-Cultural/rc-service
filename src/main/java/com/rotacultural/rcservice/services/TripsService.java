
package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Trips.TripReq;
import com.rotacultural.rcservice.DTOs.Trips.TripResp;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.repositories.TripsRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TripsService {

    private final TripsRepository tripsRepository;
    private final UsersRepository usersRepository;

    public TripsService(TripsRepository tripsRepository, UsersRepository usersRepository) {
        this.tripsRepository = tripsRepository;
        this.usersRepository = usersRepository;
    }

    public TripResp create(TripReq req) {
        dateValidation(req);

        Users usuario = usersRepository.findById(req.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Trips trip = new Trips();
        trip.setUser(usuario);
        trip.setDestination(req.getDestination());
        trip.setStartDate(req.getStartDate());
        trip.setEndDate(req.getEndDate());
        trip.setBudget(req.getBudget());

        Trips viagemSalva = tripsRepository.save(trip);

        return toResponse(viagemSalva);
    }

    public List<TripResp> getAll() {
        return tripsRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TripResp getById(Long id) {
        Trips trip = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        return toResponse(trip);
    }

    public TripResp update(long id, TripReq req) {
        dateValidation(req);

        Trips trip = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        Users user = usersRepository.findById(req.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        trip.setUser(user);
        trip.setDestination(req.getDestination());
        trip.setStartDate(req.getStartDate());
        trip.setEndDate(req.getEndDate());
        trip.setBudget(req.getBudget());

        Trips saveTrip = tripsRepository.save(trip);

        return toResponse(saveTrip);
    }

    public void delete(long id) {
        if (!tripsRepository.existsById(id)) {
            throw new EntityNotFoundException("Viagem não encontrada");
        }

        tripsRepository.deleteById(id);
    }

    private void dateValidation(TripReq req) {
        if (req.getStartDate() == null || req.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "As datas inicial e final são obrigatórias"
            );
        }

        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data final não pode ser anterior à data inicial"
            );
        }
    }


    private TripResp toResponse(Trips trip) {
        TripResp response = new TripResp();

        response.setId(trip.getIdTrip());
        response.setUserId(trip.getUser().getIdUser());
        response.setDestination(trip.getDestination());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setBudget(trip.getBudget());
        response.setDateCreate(trip.getDateCreate());

        return response;
    }

}
