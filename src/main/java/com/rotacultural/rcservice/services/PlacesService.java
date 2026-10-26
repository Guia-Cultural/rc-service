
package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.places.LocReq;
import com.rotacultural.rcservice.dtos.places.LocResp;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlacesService {

    private final PlacesRepository placesRepository;
    private final CategoriesRepository categoriesRepository;
    private final UsersRepository usersRepository;

    public PlacesService(
            PlacesRepository placesRepository,
            CategoriesRepository categoriesRepository,
            UsersRepository usersRepository
    ) {
        this.placesRepository = placesRepository;
        this.categoriesRepository = categoriesRepository;
        this.usersRepository = usersRepository;
    }

    public LocResp create(LocReq req) {
        validateCoordinates(req);

        Categories category = getCategory(req.getCatId());
        Users user = getUser(req.getUserId());

        Places place = new Places();

        // TODO: Obtain the creator from the authenticated user / obtem o criador de user autenticado
        // instead of trusting the userId provided in the request. /ve a confiabilidade do userID

        // TODO: Automatically set the initial status to EM_AVALIACAO. / status padrão é EM_AVALIACAO
        // Only authorized users should approve or reject places. / apenas admin pode aprovar or rejeitar algo

        setFields(place, req, category, user);

        return toResponse(placesRepository.save(place));
    }

    public List<LocResp> getAll() {
        // TODO: Add pagination and filtering when the number / paginação e filtro no get
        // of registered cultural places increases. / registra lugares culturaria

        // TODO: Consider allowing public queries to return / permitir public querries
        // only places with APROVADO status. / apenas de APROVDOS

        return placesRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LocResp getById(Long id) {
        return toResponse(getPlace(id));
    }

    public LocResp update(Long id, LocReq req) {
        Places place = getPlace(id);

        validateCoordinates(req);

        Categories category = getCategory(req.getCatId());
        Users user = getUser(req.getUserId());

        // TODO: Check whether the authenticated user
        // has permission to modify this place.

        // TODO: Prevent unauthorized changes to the creator
        // and approval status.

        setFields(place, req, category, user);

        return toResponse(placesRepository.save(place));
    }

    public void delete(Long id) {
        Places place = getPlace(id);

        // TODO: Validate whether the authenticated user
        // has permission to delete this place.

        // TODO: Define how to handle related feedbacks,
        // favorites and itinerary records before deletion.
        // Deleting a referenced place may cause a database
        // foreign key constraint error.

        placesRepository.delete(place);
    }

    private Places getPlace(Long id) {
        return placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Local não encontrado"
                ));
    }

    private Categories getCategory(Long id) {
        return categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoria não encontrada"
                ));
    }

    private Users getUser(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado"
                ));
    }

    private void validateCoordinates(LocReq req) {
        BigDecimal latitude = req.getLatitude();
        BigDecimal longitude = req.getLongitude();

        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude e longitude são obrigatórias"
            );
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude deve estar entre -90 e 90"
            );
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Longitude deve estar entre -180 e 180"
            );
        }

        // TODO: Confirm that coordinate precision matches the / confirma se a coordena é preciso
        // MySQL schema and define whether coordinates must
        // always be mandatory.
    }

    private void setFields(
            Places place,
            LocReq req,
            Categories category,
            Users user
    ) {
        place.setName(req.getName());
        place.setDescription(req.getDescription());
        place.setCategory(category);
        place.setAddress(req.getAddress());
        place.setCity(req.getCity());
        place.setState(req.getState());
        place.setCountry(req.getCountry());
        place.setLatitude(req.getLatitude());
        place.setLongitude(req.getLongitude());
        place.setBusinessHours(req.getBusinessHours());
        place.setPrice(req.getPrice());
        place.setCreateBy(user);
        place.setStatus(req.getStatus());
    }

    private LocResp toResponse(Places place) {
        LocResp response = new LocResp();

        response.setId(place.getIdLoc());
        response.setName(place.getName());
        response.setDescription(place.getDescription());

        response.setCatId(
                place.getCategory() != null
                        ? place.getCategory().getIdCat()
                        : null
        );

        response.setAddress(place.getAddress());
        response.setCity(place.getCity());
        response.setState(place.getState());
        response.setCountry(place.getCountry());
        response.setLatitude(place.getLatitude());
        response.setLongitude(place.getLongitude());
        response.setBusinessHours(place.getBusinessHours());
        response.setPrice(place.getPrice());
        response.setUserId(place.getCreateBy().getIdUser());
        response.setStatus(place.getStatus());
        response.setDateCreate(place.getDateCreate());

        return response;
    }
}
