package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.favorites.FavReq;
import com.rotacultural.rcservice.dtos.favorites.FavResp;
import com.rotacultural.rcservice.entities.Favorites;
import com.rotacultural.rcservice.entities.FavoritesId;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.repositories.FavoritesRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoritesService {

    private final FavoritesRepository favoritesRepository;
    private final UsersRepository usersRepository;
    private final PlacesRepository placesRepository;

    public FavoritesService(
            FavoritesRepository favoritesRepository,
            UsersRepository usersRepository,
            PlacesRepository placesRepository
    ) {
        this.favoritesRepository = favoritesRepository;
        this.usersRepository = usersRepository;
        this.placesRepository = placesRepository;
    }

    public FavResp create(FavReq req) {
        Users user = getUser(req.getUserId());
        Places place = getPlace(req.getLocId());

        FavoritesId id = new FavoritesId(
                req.getUserId(),
                req.getLocId()
        );

        if (favoritesRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este local já está nos favoritos do usuário"
            );
        }

        Favorites favorite = new Favorites();
        favorite.setUser(user);
        favorite.setPlace(place);

        return toResponse(favoritesRepository.save(favorite));
    }

    public List<FavResp> getAll() {
        return favoritesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FavResp getById(Long userId, Long locId) {
        return toResponse(getFavorite(userId, locId));
    }

    public void delete(Long userId, Long locId) {
        Favorites favorite = getFavorite(userId, locId);
        favoritesRepository.delete(favorite);
    }

    private Favorites getFavorite(Long userId, Long locId) {
        FavoritesId id = new FavoritesId(userId, locId);

        return favoritesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Favorito não encontrado"
                ));
    }

    private Users getUser(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado"
                ));
    }

    private Places getPlace(Long id) {
        return placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Local não encontrado"
                ));
    }

    private FavResp toResponse(Favorites favorite) {
        FavResp response = new FavResp();

        response.setUserId(favorite.getUser().getIdUser());
        response.setLocId(favorite.getPlace().getIdLoc());
        response.setDateCreate(favorite.getDateCreate());

        return response;
    }
}