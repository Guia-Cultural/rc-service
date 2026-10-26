
package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.DTOs.Trips.TripResp;
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

    public UserResp create(UserReq req) {

        if (usersRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado"
            );
        }

        if (req.getRole() != Role.NATIVO &&
                req.getRole() != Role.USUARIO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de usuário inválido para cadastro"
            );
        }

        Users user = new Users();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setCep(req.getCep());
        user.setRole(req.getRole());
        user.setBio(req.getBio());

        return toResponse(usersRepository.save(user));
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
        List<Users> user = usersRepository.findByCep(cep);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("CEP não encontrado");
        }

        return user.stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResp update(long id, UserUpdateReq req) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (usersRepository.existsByEmailAndIdUserNot(req.getEmail(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já pertence a outro usuário"
            );
        }


        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setCep(req.getCep());
        user.setBio(req.getBio());

        return toResponse(usersRepository.save(user));
    }

    public void delete(long id) {
        if (!usersRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }

        usersRepository.deleteById(id);
    }


    public List<UserRespTrip> getAllTrip() {
        return usersRepository.findAll().stream().map(user -> {
            UserRespTrip response = new UserRespTrip();

            response.setId(user.getIdUser());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setCep(user.getCep());
            response.setBio(user.getBio());
            response.setRole(user.getRole());
            response.setDateCreate(user.getDateCreate());

            List<TripResp> trips = user.getTrips()
                    .stream()
                    .map(trip -> {
                        TripResp tripResp = new TripResp();

                        tripResp.setId(trip.getIdTrip());
                        tripResp.setUserId(user.getIdUser());
                        tripResp.setDestination(trip.getDestination());
                        tripResp.setStartDate(trip.getStartDate());
                        tripResp.setEndDate(trip.getEndDate());
                        tripResp.setBudget(trip.getBudget());
                        tripResp.setDateCreate(trip.getDateCreate());

                        return tripResp;
                    })
                    .toList();

            response.setTrips(trips);

            return response;
        }).toList();
    }


    private UserResp toResponse(Users user) {
        UserResp response = new UserResp();
        response.setId(user.getIdUser());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCep(user.getCep());
        response.setRole(user.getRole());
        response.setBio(user.getBio());
        response.setDateCreate(user.getDateCreate());
        return response;
    }
}
