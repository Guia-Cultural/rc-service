package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.places.LocReq;
import com.rotacultural.rcservice.dtos.places.LocResp;
import com.rotacultural.rcservice.dtos.places.PlaceImageResp;
import com.rotacultural.rcservice.dtos.places.PlaceStatusReq;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Feedbacks;
import com.rotacultural.rcservice.entities.PlaceImage;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.FavoritesRepository;
import com.rotacultural.rcservice.repositories.FeedbacksRepository;
import com.rotacultural.rcservice.repositories.ItineraryRepository;
import com.rotacultural.rcservice.repositories.PlaceImageRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.services.storage.ImageFileValidator;
import com.rotacultural.rcservice.services.storage.MediaAccessService;
import com.rotacultural.rcservice.services.storage.StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PlacesService {

    private static final int MAX_IMAGES_PER_PLACE = 10;

    private final PlacesRepository placesRepository;
    private final CategoriesRepository categoriesRepository;
    private final PlaceImageRepository placeImageRepository;
    private final FeedbacksRepository feedbacksRepository;
    private final FavoritesRepository favoritesRepository;
    private final ItineraryRepository itineraryRepository;
    private final AuthService authService;
    private final StorageService storageService;
    private final MediaAccessService mediaAccessService;

    public PlacesService(
            PlacesRepository placesRepository,
            CategoriesRepository categoriesRepository,
            PlaceImageRepository placeImageRepository,
            FeedbacksRepository feedbacksRepository,
            FavoritesRepository favoritesRepository,
            ItineraryRepository itineraryRepository,
            AuthService authService,
            StorageService storageService,
            MediaAccessService mediaAccessService
    ) {
        this.placesRepository = placesRepository;
        this.categoriesRepository = categoriesRepository;
        this.placeImageRepository = placeImageRepository;
        this.feedbacksRepository = feedbacksRepository;
        this.favoritesRepository = favoritesRepository;
        this.itineraryRepository = itineraryRepository;
        this.authService = authService;
        this.storageService = storageService;
        this.mediaAccessService = mediaAccessService;
    }

    public LocResp create(LocReq req) {
        validateCoordinates(req);

        Users creator = authService.getCurrentUser();
        Categories category = getCategory(req.getCatId());

        Places place = new Places();
        setCommonFields(place, req, category);
        place.setCreateBy(creator);

        // Regra de segurança: locais criados por NATIVO sempre iniciam EM_AVALIACAO
        if (creator.getRole() == Role.ADMIN && req.getStatus() != null) {
            place.setStatus(req.getStatus());
        } else {
            place.setStatus(Status.EM_AVALIACAO);
        }
        place.setRejectionReason(null);

        return toResponse(placesRepository.save(place));
    }

    @Transactional(readOnly = true)
    public List<LocResp> getPublicPlaces(String q, String city, String state, Long catId, Price price) {
        String normalizedQ = normalizeText(q);
        String normalizedCity = normalizeText(city);
        String normalizedState = normalizeText(state);

        return placesRepository.findByStatusOrderByDateCreateDesc(Status.APROVADO)
                .stream()
                .filter(place -> matchesFilters(place, normalizedQ, normalizedCity, normalizedState, catId, price))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LocResp> getPublicPlacesPaged(
            String q,
            String city,
            String state,
            Long catId,
            Price price,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        List<LocResp> all = getPublicPlaces(q, city, state, catId, price);
        int start = Math.min(safePage * safeSize, all.size());
        int end = Math.min(start + safeSize, all.size());

        return new PageImpl<>(all.subList(start, end), PageRequest.of(safePage, safeSize), all.size());
    }

    @Transactional(readOnly = true)
    public List<LocResp> getMyPlaces() {
        Users currentUser = authService.getCurrentUser();
        return placesRepository.findByCreateBy_IdUserOrderByDateCreateDesc(currentUser.getIdUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LocResp> getAdminPlaces(Status status) {
        List<Places> places = status != null
                ? placesRepository.findByStatusOrderByDateCreateDesc(status)
                : placesRepository.findAllByOrderByDateCreateDesc();

        return places.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocResp getById(Long id) {
        Places place = getPlace(id);
        ensureCanViewPlace(place);
        return toResponse(place);
    }

    @Transactional
    public LocResp update(Long id, LocReq req) {
        Places place = getPlace(id);
        authService.requireOwnerOrAdmin(place.getCreateBy().getIdUser());

        validateCoordinates(req);
        Categories category = getCategory(req.getCatId());

        setCommonFields(place, req, category);

        // Não permite alteração indevida de criador ou autoprovação pelo NATIVO
        Users currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            if (place.getStatus() == Status.REJEITADO) {
                place.setStatus(Status.EM_AVALIACAO);
                place.setRejectionReason(null);
            }
        } else if (req.getStatus() != null) {
            place.setStatus(req.getStatus());
            if (req.getStatus() == Status.APROVADO) {
                place.setRejectionReason(null);
            }
        }

        return toResponse(placesRepository.save(place));
    }

    @Transactional
    public LocResp updateStatus(Long id, PlaceStatusReq req) {
        if (!authService.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente administradores podem aprovar ou rejeitar locais"
            );
        }

        Places place = getPlace(id);
        place.setStatus(req.getStatus());

        if (req.getStatus() == Status.REJEITADO) {
            String reason = req.getRejectionReason() != null && !req.getRejectionReason().isBlank()
                    ? req.getRejectionReason().trim()
                    : "Local não atendeu aos critérios de moderação.";
            place.setRejectionReason(reason);
        } else {
            place.setRejectionReason(null);
        }

        return toResponse(placesRepository.save(place));
    }

    @Transactional
    public void delete(Long id) {
        Places place = getPlace(id);
        authService.requireOwnerOrAdmin(place.getCreateBy().getIdUser());

        List<PlaceImage> images = placeImageRepository.findByPlace_IdLocOrderByCoverDescDateCreateAsc(id);
        for (PlaceImage image : images) {
            storageService.delete(image.getUrl());
        }

        favoritesRepository.deleteByPlace_IdLoc(id);
        feedbacksRepository.deleteByPlaces_IdLoc(id);
        itineraryRepository.deleteByLocale_IdLoc(id);
        placeImageRepository.deleteByPlace_IdLoc(id);
        placesRepository.delete(place);
    }

    @Transactional
    public List<PlaceImageResp> uploadImages(Long placeId, List<MultipartFile> files, boolean cover) {
        Places place = getPlace(placeId);
        authService.requireOwnerOrAdmin(place.getCreateBy().getIdUser());

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie pelo menos uma imagem");
        }

        long existingCount = placeImageRepository.countByPlace_IdLoc(placeId);
        if (existingCount + files.size() > MAX_IMAGES_PER_PLACE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limite máximo de " + MAX_IMAGES_PER_PLACE + " imagens por local excedido"
            );
        }

        for (MultipartFile file : files) {
            ImageFileValidator.validateImage(file);
        }

        List<PlaceImageResp> uploaded = new ArrayList<>();
        List<String> storedUrls = new ArrayList<>();
        boolean shouldMarkCover = cover || place.getCoverImage() == null || place.getCoverImage().isBlank();

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String canonicalUrl = storageService.store(file, "places/" + placeId);
                storedUrls.add(canonicalUrl);

                boolean isFirstCover = shouldMarkCover && i == 0;
                if (isFirstCover) {
                    List<PlaceImage> existingImages = placeImageRepository
                            .findByPlace_IdLocOrderByCoverDescDateCreateAsc(placeId);
                    for (PlaceImage img : existingImages) {
                        if (Boolean.TRUE.equals(img.getCover())) {
                            img.setCover(false);
                            placeImageRepository.save(img);
                        }
                    }
                    place.setCoverImage(canonicalUrl);
                    placesRepository.save(place);
                }

                PlaceImage image = new PlaceImage();
                image.setPlace(place);
                image.setUrl(canonicalUrl);
                image.setOriginalName(file.getOriginalFilename());
                image.setContentType(file.getContentType());
                image.setSizeBytes(file.getSize());
                image.setCover(isFirstCover);

                uploaded.add(toImageResponse(placeImageRepository.save(image), place.getStatus()));
            }
        } catch (RuntimeException ex) {
            for (String storedUrl : storedUrls) {
                storageService.delete(storedUrl);
            }
            throw ex;
        }

        return uploaded;
    }

    @Transactional(readOnly = true)
    public List<PlaceImageResp> listImages(Long placeId) {
        Places place = getPlace(placeId);
        ensureCanViewPlace(place);
        return placeImageRepository.findByPlace_IdLocOrderByCoverDescDateCreateAsc(placeId)
                .stream()
                .map(image -> toImageResponse(image, place.getStatus()))
                .toList();
    }

    @Transactional
    public void deleteImage(Long placeId, Long imageId) {
        Places place = getPlace(placeId);
        authService.requireOwnerOrAdmin(place.getCreateBy().getIdUser());

        PlaceImage image = placeImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));

        if (!image.getPlace().getIdLoc().equals(placeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem não pertence a este local");
        }

        boolean wasCover = Boolean.TRUE.equals(image.getCover())
                || image.getUrl().equals(place.getCoverImage());
        storageService.delete(image.getUrl());
        placeImageRepository.delete(image);

        if (wasCover) {
            List<PlaceImage> remaining = placeImageRepository.findByPlace_IdLocOrderByCoverDescDateCreateAsc(placeId);
            if (remaining.isEmpty()) {
                place.setCoverImage(null);
            } else {
                PlaceImage nextCover = remaining.get(0);
                nextCover.setCover(true);
                placeImageRepository.save(nextCover);
                place.setCoverImage(nextCover.getUrl());
            }
            placesRepository.save(place);
        }
    }

    private void ensureCanViewPlace(Places place) {
        if (place.getStatus() == Status.APROVADO) {
            return;
        }
        Optional<Users> currentUser = authService.getOptionalCurrentUser();
        boolean allowed = currentUser.isPresent()
                && (currentUser.get().getRole() == Role.ADMIN
                || place.getCreateBy().getIdUser().equals(currentUser.get().getIdUser()));
        if (!allowed) {
            throw new EntityNotFoundException("Local não encontrado");
        }
    }

    private Places getPlace(Long id) {
        return placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local não encontrado"));
    }

    private Categories getCategory(Long id) {
        return categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }

    private void validateCoordinates(LocReq req) {
        BigDecimal latitude = req.getLatitude();
        BigDecimal longitude = req.getLongitude();

        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe latitude e longitude juntas"
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
    }

    private void setCommonFields(Places place, LocReq req, Categories category) {
        place.setName(req.getName().trim());
        place.setDescription(req.getDescription().trim());
        place.setCategory(category);
        place.setAddress(req.getAddress().trim());
        place.setCity(req.getCity().trim());
        place.setState(req.getState().trim());
        place.setCountry(req.getCountry().trim());
        place.setLatitude(req.getLatitude());
        place.setLongitude(req.getLongitude());
        place.setBusinessHours(req.getBusinessHours().trim());
        place.setPrice(req.getPrice());
        place.setEstimatedCost(req.getEstimatedCost());
        if (req.getCoverImage() != null && !req.getCoverImage().isBlank()) {
            place.setCoverImage(ImageFileValidator.stripQuery(req.getCoverImage().trim()));
        }
        place.setOfficialWebsite(trimToNull(req.getOfficialWebsite()));
        place.setTicketLink(trimToNull(req.getTicketLink()));
        place.setContact(trimToNull(req.getContact()));
    }

    private boolean matchesFilters(
            Places place,
            String normalizedQ,
            String normalizedCity,
            String normalizedState,
            Long catId,
            Price price
    ) {
        if (catId != null && (place.getCategory() == null || !place.getCategory().getIdCat().equals(catId))) {
            return false;
        }
        if (price != null && place.getPrice() != price) {
            return false;
        }
        if (!normalizedCity.isEmpty() && !normalizeText(place.getCity()).contains(normalizedCity)) {
            return false;
        }
        if (!normalizedState.isEmpty() && !normalizeText(place.getState()).equals(normalizedState)
                && !normalizeText(place.getState()).contains(normalizedState)) {
            return false;
        }
        if (!normalizedQ.isEmpty()) {
            String haystack = normalizeText(
                    place.getName() + " " + place.getDescription() + " "
                            + place.getCity() + " " + place.getState() + " " + place.getAddress()
                            + " " + (place.getCategory() != null ? place.getCategory().getName() : "")
            );
            return haystack.contains(normalizedQ);
        }
        return true;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public LocResp toResponse(Places place) {
        LocResp response = new LocResp();
        response.setId(place.getIdLoc());
        response.setName(place.getName());
        response.setDescription(place.getDescription());
        response.setCatId(place.getCategory() != null ? place.getCategory().getIdCat() : null);
        response.setCategoryName(place.getCategory() != null ? place.getCategory().getName() : null);
        response.setAddress(place.getAddress());
        response.setCity(place.getCity());
        response.setState(place.getState());
        response.setCountry(place.getCountry());
        response.setLatitude(place.getLatitude());
        response.setLongitude(place.getLongitude());
        response.setBusinessHours(place.getBusinessHours());
        response.setPrice(place.getPrice());
        response.setEstimatedCost(place.getEstimatedCost());
        response.setCoverImage(mediaAccessService.toAccessibleUrl(place.getCoverImage(), place.getStatus()));
        response.setOfficialWebsite(place.getOfficialWebsite());
        response.setTicketLink(place.getTicketLink());
        response.setContact(place.getContact());
        response.setUserId(place.getCreateBy().getIdUser());
        response.setCreatorName(place.getCreateBy().getName());
        response.setStatus(place.getStatus());
        response.setRejectionReason(place.getRejectionReason());
        response.setDateCreate(place.getDateCreate());

        List<PlaceImageResp> imageResps = placeImageRepository
                .findByPlace_IdLocOrderByCoverDescDateCreateAsc(place.getIdLoc())
                .stream()
                .map(image -> toImageResponse(image, place.getStatus()))
                .toList();
        response.setImages(imageResps);

        if ((response.getCoverImage() == null || response.getCoverImage().isBlank()) && !imageResps.isEmpty()) {
            response.setCoverImage(imageResps.get(0).getUrl());
        }

        List<Feedbacks> feedbacks = feedbacksRepository.findByPlaces_IdLocOrderByDateCreateDesc(place.getIdLoc());
        response.setReviewCount(feedbacks.size());
        if (!feedbacks.isEmpty()) {
            double avg = feedbacks.stream()
                    .mapToDouble(Feedbacks::getAssessment)
                    .average()
                    .orElse(0.0);
            response.setAverageRating(Math.round(avg * 10.0) / 10.0);
        } else {
            response.setAverageRating(0.0);
        }

        return response;
    }

    private PlaceImageResp toImageResponse(PlaceImage image, Status placeStatus) {
        PlaceImageResp resp = new PlaceImageResp();
        resp.setId(image.getIdImage());
        resp.setPlaceId(image.getPlace().getIdLoc());
        resp.setUrl(mediaAccessService.toAccessibleUrl(image.getUrl(), placeStatus));
        resp.setOriginalName(image.getOriginalName());
        resp.setContentType(image.getContentType());
        resp.setSizeBytes(image.getSizeBytes());
        resp.setCover(Boolean.TRUE.equals(image.getCover()));
        resp.setDateCreate(image.getDateCreate());
        return resp;
    }
}
