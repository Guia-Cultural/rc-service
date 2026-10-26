
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

    public TripResp create(TripReq tripReq) {
        validarDatas(tripReq);

        Users usuario = usersRepository.findById(tripReq.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Trips viagem = new Trips();
        viagem.setUser(usuario);
        viagem.setDestination(tripReq.getDestination());
        viagem.setStartDate(tripReq.getStartDate());
        viagem.setEndDate(tripReq.getEndDate());
        viagem.setBudget(tripReq.getBudget());

        Trips viagemSalva = tripsRepository.save(viagem);

        return toResponse(viagemSalva);
    }

    public List<TripResp> getAll() {
        return tripsRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TripResp getById(Long id) {
        Trips viagem = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        return toResponse(viagem);
    }

    public TripResp update(long id, TripReq tripReq) {
        validarDatas(tripReq);

        Trips viagem = tripsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

        Users usuario = usersRepository.findById(tripReq.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        viagem.setUser(usuario);
        viagem.setDestination(tripReq.getDestination());
        viagem.setStartDate(tripReq.getStartDate());
        viagem.setEndDate(tripReq.getEndDate());
        viagem.setBudget(tripReq.getBudget());

        Trips viagemSalva = tripsRepository.save(viagem);

        return toResponse(viagemSalva);
    }

    public void delete(long id) {
        if (!tripsRepository.existsById(id)) {
            throw new EntityNotFoundException("Viagem não encontrada");
        }

        tripsRepository.deleteById(id);
    }

    private void validarDatas(TripReq tripReq) {
        if (tripReq.getStartDate() == null || tripReq.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "As datas inicial e final são obrigatórias"
            );
        }

        if (tripReq.getEndDate().isBefore(tripReq.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data final não pode ser anterior à data inicial"
            );
        }
    }


    private TripResp toResponse(Trips viagem) {
        TripResp resposta = new TripResp();

        resposta.setId(viagem.getIdTrip());
        resposta.setUserId(viagem.getUser().getIdUser());
        resposta.setDestination(viagem.getDestination());
        resposta.setStartDate(viagem.getStartDate());
        resposta.setEndDate(viagem.getEndDate());
        resposta.setBudget(viagem.getBudget());
        resposta.setDateCreate(viagem.getDateCreate());

        return resposta;
    }

}
