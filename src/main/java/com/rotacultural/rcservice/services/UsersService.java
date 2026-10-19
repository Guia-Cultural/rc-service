package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Users.UserReq;
import com.rotacultural.rcservice.DTOs.Users.UserResp;
import com.rotacultural.rcservice.DTOs.Users.UserRespTrip;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public UserResp create(UserReq userReq) {
        Users usuario = new Users();
        usuario.setNome(userReq.getNome());
        usuario.setEmail(userReq.getEmail());
        usuario.setSenha(userReq.getSenha());
        if(userReq.getCep() != null && !userReq.getCep().isEmpty()){
            usuario.setCep(userReq.getCep());
        }
        usuario.setRole(userReq.getRole());
        if (userReq.getBio() != null && !userReq.getBio().isEmpty()){
            usuario.setBio(userReq.getBio());
        }
        usersRepository.save(usuario);

        UserResp userResp = new UserResp();
        userResp.setId(userResp.getId());
        userResp.setNome(userReq.getNome());
        userResp.setEmail(userReq.getEmail());
        userResp.setCep(userReq.getCep());
        userResp.setBio(userReq.getBio());
        userResp.setRole(userReq.getRole());
        userResp.setDataCriacao(usuario.getDataCriacao());
        return userResp;
    }

    public List<UserResp> getAll() {
        List<Users> usuarios = usersRepository.findAll();
        List<UserResp> userResps = new ArrayList<>();
        for (Users usuario : usuarios) {
            UserResp userResp = new UserResp();
            userResp.setId(usuario.getIdUser());
            userResp.setNome(usuario.getNome());
            userResp.setEmail(usuario.getEmail());
            userResp.setCep(usuario.getCep());
            userResp.setBio(usuario.getBio());
            userResp.setRole(usuario.getRole());
            userResps.add(userResp);
        }

        return userResps;
    }

    public UserResp getById(long id) {
        Users usuario = usersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        UserResp userResp = new UserResp();
        userResp.setId(usuario.getIdUser());
        userResp.setNome(usuario.getNome());
        userResp.setEmail(usuario.getEmail());
        userResp.setCep(usuario.getCep());
        userResp.setBio(usuario.getBio());
        userResp.setRole(usuario.getRole());
        return userResp;
    }

    public List<UserResp> getByCep(String cep) {
        List<Users> usuarios = usersRepository.findByCep(cep);
        if(usuarios.isEmpty()){
            throw new EntityNotFoundException("Cep não encontrado");
        }
        List<UserResp> userResps = new ArrayList<>();
        for (Users usuario : usuarios) {
            UserResp userResp = new UserResp();
            userResp.setId(usuario.getIdUser());
            userResp.setNome(usuario.getNome());
            userResp.setEmail(usuario.getEmail());
            userResp.setCep(usuario.getCep());
            userResp.setBio(usuario.getBio());
            userResp.setRole(usuario.getRole());
            userResps.add(userResp);
        }
        return userResps;
    }

    public UserResp update(long id, UserReq userReq) {
        Users usuario = usersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        usuario.setNome(userReq.getNome());
        usuario.setEmail(userReq.getEmail());
        usuario.setCep(userReq.getCep());
        usuario.setBio(userReq.getBio());
        usuario.setRole(userReq.getRole());
        usersRepository.save(usuario);
        UserResp userResp = new UserResp();
        userResp.setId(usuario.getIdUser());
        userResp.setNome(usuario.getNome());
        userResp.setEmail(usuario.getEmail());
        userResp.setCep(usuario.getCep());
        userResp.setBio(usuario.getBio());
        userResp.setRole(usuario.getRole());
        userResp.setDataCriacao(usuario.getDataCriacao());
        return userResp;
    }

    public void delete(long id) {
        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
        }else {
            throw new EntityNotFoundException("Usuario não encontrado");
        }
    }

    public List<UserRespTrip> getAllTrip() {
        List<Users> usuarios = usersRepository.findAll();
        List<UserRespTrip> userRespTrips = new ArrayList<>();
        for (Users usuario : usuarios) {
            UserRespTrip userRespTrip = new UserRespTrip();
            userRespTrip.setId(usuario.getIdUser());
            userRespTrip.setNome(usuario.getNome());
            userRespTrip.setEmail(usuario.getEmail());
            userRespTrip.setCep(usuario.getCep());
            userRespTrip.setBio(usuario.getBio());
            userRespTrip.setRole(usuario.getRole());
            userRespTrip.setViagens(usuario.getViagens());
            userRespTrips.add(userRespTrip);
        }
        return userRespTrips;
    }
}
