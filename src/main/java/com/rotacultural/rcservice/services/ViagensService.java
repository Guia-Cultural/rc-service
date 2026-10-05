package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Viagens.TripReq;
import com.rotacultural.rcservice.DTOs.Viagens.TripResp;
import com.rotacultural.rcservice.entities.Usuarios;
import com.rotacultural.rcservice.entities.Viagens;
import com.rotacultural.rcservice.repositories.UsuariosRepository;
import com.rotacultural.rcservice.repositories.ViagensRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ViagensService {
    private final ViagensRepository viagensRepository;
    private final UsuariosRepository usuariosRepository;

    public ViagensService(ViagensRepository viagensRepository, UsuariosRepository usuariosRepository) {
        this.viagensRepository = viagensRepository;
        this.usuariosRepository = usuariosRepository;
    }

    public TripResp create(TripReq tripReq) {
        Viagens viagens = new Viagens();
        Usuarios usuarios = usuariosRepository.findById(tripReq.getUserId()).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        viagens.setUsuario(usuarios);
        viagens.setDestino(tripReq.getDestino());
        if(tripReq.getDataInicio() != null) {
            viagens.setDataInicio(tripReq.getDataInicio());
        }
        if(tripReq.getDataFim() != null && tripReq.getDataFim().isBefore(tripReq.getDataInicio())) {
            viagens.setDataFim(tripReq.getDataFim());
        }else {
            throw new IllegalArgumentException("A data final deve ser anterior à data inicial");
        }

        viagens.setOrcamento(tripReq.getOrcamento());
        viagensRepository.save(viagens);

        TripResp tripResp = new TripResp();
        tripResp.setUserId(usuarios.getIdUser());
        tripResp.setDestino(tripReq.getDestino());
        tripResp.setDataInicio(viagens.getDataInicio());
        tripResp.setDataFim(viagens.getDataFim());
        tripResp.setOrcamento(viagens.getOrcamento());

        return tripResp;

    }

    public List<TripResp> getAll() {
        List<Viagens> viagens = viagensRepository.findAll();
        List<TripResp> tripResp = new ArrayList<>();
        for (Viagens viagen : viagens) {
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
        Viagens viagem = viagensRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        TripResp tripResp = new TripResp();
        tripResp.setId(viagem.getIdTrip());
        tripResp.setDestino(viagem.getDestino());
        tripResp.setDataInicio(viagem.getDataInicio());
        tripResp.setDataFim(viagem.getDataFim());
        tripResp.setOrcamento(viagem.getOrcamento());
        return tripResp;
    }

    public TripResp update(long id, TripReq tripReq) {
        Viagens viagem = viagensRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
        viagem.setUsuario(usuariosRepository.findById(tripReq.getUserId()).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado")));
        viagem.setDestino(tripReq.getDestino());
        viagem.setDataInicio(tripReq.getDataInicio());
        viagem.setDataFim(tripReq.getDataFim());
        viagem.setOrcamento(tripReq.getOrcamento());
        viagensRepository.save(viagem);
        TripResp tripResp = new TripResp();
        tripResp.setId(id);
        tripResp.setDestino(viagem.getDestino());
        tripResp.setDataInicio(viagem.getDataInicio());
        tripResp.setDataFim(viagem.getDataFim());
        tripResp.setOrcamento(viagem.getOrcamento());
        return tripResp;
    }

    public void delete(long id) {
        if (viagensRepository.existsById(id)) {
            viagensRepository.deleteById(id);
        }else{
            throw new EntityNotFoundException("Viagem não encontrada");
        }
    }


}
