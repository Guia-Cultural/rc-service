package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.trips.TripResp;
import com.rotacultural.rcservice.dtos.users.UserReq;
import com.rotacultural.rcservice.dtos.users.UserResp;
import com.rotacultural.rcservice.dtos.users.UserRespTrip;
import com.rotacultural.rcservice.dtos.users.UserUpdateReq;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Trips;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.repositories.FavoritesRepository;
import com.rotacultural.rcservice.repositories.FeedbacksRepository;
import com.rotacultural.rcservice.repositories.ItineraryRepository;
import com.rotacultural.rcservice.repositories.PlaceImageRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.PreferencesRepository;
import com.rotacultural.rcservice.repositories.TripsRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final FavoritesRepository favoritesRepository;
    private final FeedbacksRepository feedbacksRepository;
    private final PreferencesRepository preferencesRepository;
    private final ItineraryRepository itineraryRepository;
    private final TripsRepository tripsRepository;
    private final PlacesRepository placesRepository;
    private final PlaceImageRepository placeImageRepository;

    public UsersService(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder,
            AuthService authService,
            FavoritesRepository favoritesRepository,
            FeedbacksRepository feedbacksRepository,
            PreferencesRepository preferencesRepository,
            ItineraryRepository itineraryRepository,
            TripsRepository tripsRepository,
            PlacesRepository placesRepository,
            PlaceImageRepository placeImageRepository
    ) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.favoritesRepository = favoritesRepository;
        this.feedbacksRepository = feedbacksRepository;
        this.preferencesRepository = preferencesRepository;
        this.itineraryRepository = itineraryRepository;
        this.tripsRepository = tripsRepository;
        this.placesRepository = placesRepository;
        this.placeImageRepository = placeImageRepository;
    }

    public UserResp create(UserReq req) {
        String email = req.getEmail().trim();

        if (usersRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado"
            );
        }

        if (req.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido criar conta com perfil ADMIN no cadastro público"
            );
        }

        boolean requestedNative = req.getRole() == Role.NATIVO
                || Boolean.TRUE.equals(req.getRequestNative());

        Users user = new Users();
        user.setName(req.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setCep(req.getCep());
        // Fluxo seguro: toda conta pública inicia como USUARIO; solicitação de NATIVO aguarda aprovação do ADMIN
        user.setRole(Role.USUARIO);
        user.setNativeRequested(requestedNative);
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
        authService.requireOwnerOrAdmin(id);

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
        authService.requireOwnerOrAdmin(id);

        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        String email = req.getEmail().trim();
        if (usersRepository.existsByEmailIgnoreCaseAndIdUserNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já pertence a outro usuário"
            );
        }

        user.setName(req.getName().trim());
        user.setEmail(email);
        user.setCep(req.getCep());
        user.setBio(req.getBio());

        return toResponse(usersRepository.save(user));
    }

    public UserResp updateRole(long id, Role newRole) {
        if (!authService.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente administradores podem alterar perfis de usuário"
            );
        }

        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        user.setRole(newRole);
        user.setNativeRequested(false);

        return toResponse(usersRepository.save(user));
    }

    @Transactional
    public void delete(long id) {
        authService.requireOwnerOrAdmin(id);

        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        favoritesRepository.deleteByUser_IdUser(id);
        feedbacksRepository.deleteByUser_IdUser(id);

        List<Trips> trips = tripsRepository.findByUser_IdUserOrderByDateCreateDesc(id);
        for (Trips trip : trips) {
            preferencesRepository.deleteByTrips_IdTrip(trip.getIdTrip());
            itineraryRepository.deleteByTrip_IdTrip(trip.getIdTrip());
        }
        tripsRepository.deleteByUser_IdUser(id);

        List<Places> createdPlaces = placesRepository.findByCreateBy_IdUserOrderByDateCreateDesc(id);
        for (Places place : createdPlaces) {
            Long locId = place.getIdLoc();
            favoritesRepository.deleteByPlace_IdLoc(locId);
            feedbacksRepository.deleteByPlaces_IdLoc(locId);
            itineraryRepository.deleteByLocale_IdLoc(locId);
            placeImageRepository.deleteByPlace_IdLoc(locId);
            placesRepository.delete(place);
        }

        usersRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public List<UserRespTrip> getAllTrip() {
        return usersRepository.findAll().stream().map(user -> {
            UserRespTrip response = new UserRespTrip();

            response.setId(user.getIdUser());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setCep(user.getCep());
            response.setBio(user.getBio());
            response.setRole(user.getRole());
            response.setNativeRequested(Boolean.TRUE.equals(user.getNativeRequested()));
            response.setDateCreate(user.getDateCreate());

            List<TripResp> trips = tripsRepository.findByUser_IdUserOrderByDateCreateDesc(user.getIdUser())
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
        return authService.toUserResponse(user);
    }
}
