
package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Users.UserReq;
import com.rotacultural.rcservice.DTOs.Users.UserResp;
import com.rotacultural.rcservice.DTOs.Users.UserRespTrip;
import com.rotacultural.rcservice.DTOs.Users.UserUpdateReq;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResp create(UserReq userReq) {
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
        usuario.setPassword(passwordEncoder.encode(userReq.getPassword()));
        usuario.setCep(userReq.getCep());
        usuario.setRole(userReq.getRole());
        usuario.setBio(userReq.getBio());

        return toResponse(usersRepository.save(usuario));
    }

    public List<UserResp> getAll() {
        return usersRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResp getById(long id) {
        Users usuario = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        return toResponse(usuario);
    }

    public List<UserResp> getByCep(String cep) {
        List<Users> usuarios = usersRepository.findByCep(cep);

        if (usuarios.isEmpty()) {
            throw new EntityNotFoundException("CEP não encontrado");
        }

        return usuarios.stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResp update(long id, UserUpdateReq userReq) {
        Users usuario = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        usuario.setName(userReq.getName());
        usuario.setEmail(userReq.getEmail());
        usuario.setCep(userReq.getCep());
        usuario.setBio(userReq.getBio());

        return toResponse(usersRepository.save(usuario));
    }

    public void delete(long id) {
        if (!usersRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }

        usersRepository.deleteById(id);
    }

    public List<UserRespTrip> getAllTrip() {
        return usersRepository.findAll().stream().map(usuario -> {
            UserRespTrip resposta = new UserRespTrip();
            resposta.setId(usuario.getIdUser());
            resposta.setName(usuario.getName());
            resposta.setEmail(usuario.getEmail());
            resposta.setCep(usuario.getCep());
            resposta.setBio(usuario.getBio());
            resposta.setRole(usuario.getRole());
            resposta.setTrips(usuario.getTrips());
            return resposta;
        }).toList();
    }

    private UserResp toResponse(Users usuario) {
        UserResp resposta = new UserResp();
        resposta.setId(usuario.getIdUser());
        resposta.setName(usuario.getName());
        resposta.setEmail(usuario.getEmail());
        resposta.setCep(usuario.getCep());
        resposta.setRole(usuario.getRole());
        resposta.setBio(usuario.getBio());
        resposta.setDateCreate(usuario.getDateCreate());
        return resposta;
    }
}
