package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.favorites.FavReq;
import com.rotacultural.rcservice.dtos.favorites.FavResp;
import com.rotacultural.rcservice.entities.Favorites;
import com.rotacultural.rcservice.entities.FavoritesId;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.FavoritesRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoritesService {

    private final FavoritesRepository favoritesRepository;
    private final PlacesRepository placesRepository;
    private final AuthService authService;

    public FavoritesService(
            FavoritesRepository favoritesRepository,
            PlacesRepository placesRepository,
            AuthService authService
    ) {
        this.favoritesRepository = favoritesRepository;
        this.placesRepository = placesRepository;
        this.authService = authService;
    }

    public FavResp create(FavReq req) {
        Users currentUser = authService.getCurrentUser();
        if (req.getUserId() != null && !req.getUserId().equals(currentUser.getIdUser()) && !authService.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Não é permitido manipular favoritos de outro usuário"
            );
        }

        Places place = getPlace(req.getLocId());
        if (place.getStatus() != Status.APROVADO && currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Apenas locais aprovados podem ser favoritados"
            );
        }

        FavoritesId id = new FavoritesId(currentUser.getIdUser(), req.getLocId());
        if (favoritesRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este local já está nos favoritos do usuário"
            );
        }

        Favorites favorite = new Favorites();
        favorite.setUser(currentUser);
        favorite.setPlace(place);

        return toResponse(favoritesRepository.save(favorite));
    }

    @Transactional(readOnly = true)
    public List<FavResp> getAll() {
        return favoritesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavResp> getMyFavorites() {
        Users currentUser = authService.getCurrentUser();
        return favoritesRepository.findByUser_IdUserOrderByDateCreateDesc(currentUser.getIdUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavResp getById(Long userId, Long locId) {
        authService.requireOwnerOrAdmin(userId);
        return toResponse(getFavorite(userId, locId));
    }

    public void delete(Long userId, Long locId) {
        authService.requireOwnerOrAdmin(userId);
        Favorites favorite = getFavorite(userId, locId);
        favoritesRepository.delete(favorite);
    }

    public void deleteByPlace(Long locId) {
        Users currentUser = authService.getCurrentUser();
        Favorites favorite = getFavorite(currentUser.getIdUser(), locId);
        favoritesRepository.delete(favorite);
    }

    private Favorites getFavorite(Long userId, Long locId) {
        FavoritesId id = new FavoritesId(userId, locId);
        return favoritesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Favorito não encontrado"));
    }

    private Places getPlace(Long id) {
        return placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local não encontrado"));
    }

    private FavResp toResponse(Favorites favorite) {
        FavResp response = new FavResp();
        Places place = favorite.getPlace();

        response.setUserId(favorite.getUser().getIdUser());
        response.setLocId(place.getIdLoc());
        response.setDateCreate(favorite.getDateCreate());
        response.setPlaceName(place.getName());
        response.setPlaceCity(place.getCity());
        response.setPlaceState(place.getState());
        response.setPrice(place.getPrice());
        response.setCoverImage(place.getCoverImage());
        response.setCategoryName(place.getCategory() != null ? place.getCategory().getName() : null);

        return response;
    }
}
