package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Users.UserReq;
import com.rotacultural.rcservice.DTOs.Users.UserResp;
import com.rotacultural.rcservice.DTOs.Users.UserRespTrip;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersService(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResp create(UserReq userReq) {

        // Impede cadastro público de administradores
        if (userReq.getRole() != Role.NATIVO &&
                userReq.getRole() != Role.USUARIO) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de usuário inválido para cadastro"
            );
        }

        Users usuario = new Users();

        usuario.setName(userReq.getName());
        usuario.setEmail(userReq.getEmail());

        // Armazena a senha com hash BCrypt
        usuario.setPassword(
                passwordEncoder.encode(userReq.getPassword())
        );

        if (userReq.getCep() != null &&
                !userReq.getCep().isEmpty()) {
            usuario.setCep(userReq.getCep());
        }

        usuario.setRole(userReq.getRole());

        if (userReq.getBio() != null &&
                !userReq.getBio().isEmpty()) {
            usuario.setBio(userReq.getBio());
        }

        usuario = usersRepository.save(usuario);

        UserResp userResp = new UserResp();

        userResp.setId(usuario.getIdUser());
        userResp.setName(usuario.getName());
        userResp.setEmail(usuario.getEmail());
        userResp.setCep(usuario.getCep());
        userResp.setBio(usuario.getBio());
        userResp.setRole(usuario.getRole());
        userResp.setDateCreate(usuario.getDateCreate());

        return userResp;
    }

    public List<UserResp> getAll() {
        List<Users> usuarios = usersRepository.findAll();
        List<UserResp> userResps = new ArrayList<>();
        for (Users usuario : usuarios) {
            UserResp userResp = new UserResp();
            userResp.setId(usuario.getIdUser());
            userResp.setName(usuario.getName());
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
        userResp.setName(usuario.getName());
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
            userResp.setName(usuario.getName());
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
        usuario.setName(userReq.getName());
        usuario.setEmail(userReq.getEmail());
        usuario.setCep(userReq.getCep());
        usuario.setBio(userReq.getBio());
        usersRepository.save(usuario);
        UserResp userResp = new UserResp();
        userResp.setId(usuario.getIdUser());
        userResp.setName(usuario.getName());
        userResp.setEmail(usuario.getEmail());
        userResp.setCep(usuario.getCep());
        userResp.setBio(usuario.getBio());
        userResp.setRole(usuario.getRole());
        userResp.setDateCreate(usuario.getDateCreate());
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
            userRespTrip.setName(usuario.getName());
            userRespTrip.setEmail(usuario.getEmail());
            userRespTrip.setCep(usuario.getCep());
            userRespTrip.setBio(usuario.getBio());
            userRespTrip.setRole(usuario.getRole());
            userRespTrip.setTrips(usuario.getTrips());
            userRespTrips.add(userRespTrip);
        }
        return userRespTrips;
    }
}
