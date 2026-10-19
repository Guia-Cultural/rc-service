package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Trips.TripReq;
import com.rotacultural.rcservice.DTOs.Trips.TripResp;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.repositories.UsersRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TripsService {
    private final TripsRepository tripsRepository;
    private final UsersRepository usersRepository;

    public TripsService(TripsRepository tripsRepository, UsersRepository usersRepository) {
        this.tripsRepository = tripsRepository;
        this.usersRepository = usersRepository;
    }

    public TripResp create(TripReq tripReq) {
        Trips trips = new Trips();
        Users users = usersRepository.findById(tripReq.getUserId()).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        trips.setUser(users);
        trips.setDestination(tripReq.getDestination());
        if(tripReq.getStartDate() != null) {
            trips.setStartDate(tripReq.getStartDate());
        }
        if(tripReq.getEndDate() != null && tripReq.getEndDate().isBefore(tripReq.getStartDate())) {
            trips.setEndDate(tripReq.getEndDate());
        }else {
            throw new IllegalArgumentException("A data final deve ser anterior à data inicial");
        }

        trips.setBudget(tripReq.getBudget());
        tripsRepository.save(trips);

        TripResp tripResp = new TripResp();
        tripResp.setUserId(users.getIdUser());
        tripResp.setDestination(tripReq.getDestination());
        tripResp.setStartDate(trips.getStartDate());
        tripResp.setEndDate(trips.getEndDate());
        tripResp.setBudget(trips.getBudget());

        return tripResp;

    }

    public List<TripResp> getAll() {
        List<Trips> viagens = tripsRepository.findAll();
        List<TripResp> tripResp = new ArrayList<>();
        for (Trips viagen : viagens) {
            TripResp tripResp1 = new TripResp();
            tripResp1.setUserId(viagen.getUser().getIdUser());
            tripResp1.setDestination(viagen.getDestination());
            tripResp1.setStartDate(viagen.getStartDate());
            tripResp1.setEndDate(viagen.getEndDate());
            tripResp1.setBudget(viagen.getBudget());
            tripResp.add(tripResp1);
        }
        return tripResp;
    }

    public TripResp getById(Long id) {
        Trips viagem = tripsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        TripResp tripResp = new TripResp();
        tripResp.setId(viagem.getIdTrip());
        tripResp.setDestination(viagem.getDestination());
        tripResp.setStartDate(viagem.getStartDate());
        tripResp.setEndDate(viagem.getEndDate());
        tripResp.setBudget(viagem.getBudget());
        return tripResp;
    }

    public TripResp update(long id, TripReq tripReq) {
        Trips viagem = tripsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        viagem.setUser(usersRepository.findById(tripReq.getUserId()).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado")));
        viagem.setDestination(tripReq.getDestination());
        viagem.setStartDate(tripReq.getStartDate());
        viagem.setEndDate(tripReq.getEndDate());
        viagem.setBudget(tripReq.getBudget());
        tripsRepository.save(viagem);
        TripResp tripResp = new TripResp();
        tripResp.setId(id);
        tripResp.setDestination(viagem.getDestination());
        tripResp.setStartDate(viagem.getStartDate());
        tripResp.setEndDate(viagem.getEndDate());
        tripResp.setBudget(viagem.getBudget());
        return tripResp;
    }

    public void delete(long id) {
        if (tripsRepository.existsById(id)) {
            tripsRepository.deleteById(id);
        }else{
            throw new EntityNotFoundException("Viagem não encontrada");
        }
    }


}
