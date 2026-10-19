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
        trips.setUsuario(users);
        trips.setDestino(tripReq.getDestino());
        if(tripReq.getDataInicio() != null) {
            trips.setDataInicio(tripReq.getDataInicio());
        }
        if(tripReq.getDataFim() != null && tripReq.getDataFim().isBefore(tripReq.getDataInicio())) {
            trips.setDataFim(tripReq.getDataFim());
        }else {
            throw new IllegalArgumentException("A data final deve ser anterior à data inicial");
        }

        trips.setOrcamento(tripReq.getOrcamento());
        tripsRepository.save(trips);

        TripResp tripResp = new TripResp();
        tripResp.setUserId(users.getIdUser());
        tripResp.setDestino(tripReq.getDestino());
        tripResp.setDataInicio(trips.getDataInicio());
        tripResp.setDataFim(trips.getDataFim());
        tripResp.setOrcamento(trips.getOrcamento());

        return tripResp;

    }

    public List<TripResp> getAll() {
        List<Trips> viagens = tripsRepository.findAll();
        List<TripResp> tripResp = new ArrayList<>();
        for (Trips viagen : viagens) {
            TripResp tripResp1 = new TripResp();
            tripResp1.setUserId(viagen.getUsuario().getIdUser());
            tripResp1.setDestino(viagen.getDestino());
            tripResp1.setDataInicio(viagen.getDataInicio());
            tripResp1.setDataFim(viagen.getDataFim());
            tripResp1.setOrcamento(viagen.getOrcamento());
            tripResp.add(tripResp1);
        }
        return tripResp;
    }

    public TripResp getById(Long id) {
        Trips viagem = tripsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        TripResp tripResp = new TripResp();
        tripResp.setId(viagem.getIdTrip());
        tripResp.setDestino(viagem.getDestino());
        tripResp.setDataInicio(viagem.getDataInicio());
        tripResp.setDataFim(viagem.getDataFim());
        tripResp.setOrcamento(viagem.getOrcamento());
        return tripResp;
    }

    public TripResp update(long id, TripReq tripReq) {
        Trips viagem = tripsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        viagem.setUsuario(usersRepository.findById(tripReq.getUserId()).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado")));
        viagem.setDestino(tripReq.getDestino());
        viagem.setDataInicio(tripReq.getDataInicio());
        viagem.setDataFim(tripReq.getDataFim());
        viagem.setOrcamento(tripReq.getOrcamento());
        tripsRepository.save(viagem);
        TripResp tripResp = new TripResp();
        tripResp.setId(id);
        tripResp.setDestino(viagem.getDestino());
        tripResp.setDataInicio(viagem.getDataInicio());
        tripResp.setDataFim(viagem.getDataFim());
        tripResp.setOrcamento(viagem.getOrcamento());
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
