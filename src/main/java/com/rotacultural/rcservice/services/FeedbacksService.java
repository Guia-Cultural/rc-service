package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.feedbacks.FeedbacksReq;
import com.rotacultural.rcservice.dtos.feedbacks.FeedbacksResp;
import com.rotacultural.rcservice.entities.Feedbacks;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.FeedbacksRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FeedbacksService {

    private final FeedbacksRepository feedbacksRepository;
    private final PlacesRepository placesRepository;
    private final AuthService authService;

    public FeedbacksService(
            FeedbacksRepository feedbacksRepository,
            PlacesRepository placesRepository,
            AuthService authService
    ) {
        this.feedbacksRepository = feedbacksRepository;
        this.placesRepository = placesRepository;
        this.authService = authService;
    }

    public FeedbacksResp create(FeedbacksReq req) {
        Users currentUser = authService.getCurrentUser();
        if (req.getUserId() != null && !req.getUserId().equals(currentUser.getIdUser()) && !authService.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Não é permitido publicar avaliação em nome de outro usuário"
            );
        }

        Places place = getPlace(req.getLocId());
        if (place.getStatus() != Status.APROVADO && currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Apenas locais aprovados podem receber avaliações"
            );
        }

        if (feedbacksRepository.existsByUser_IdUserAndPlaces_IdLoc(currentUser.getIdUser(), req.getLocId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Você já avaliou este local. Você pode editar sua avaliação existente."
            );
        }

        Feedbacks feedback = new Feedbacks();
        feedback.setUser(currentUser);
        feedback.setPlaces(place);
        feedback.setAssessment(req.getAssessment());
        feedback.setCommentary(req.getCommentary() != null ? req.getCommentary().trim() : null);

        return toResponse(feedbacksRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public List<FeedbacksResp> getAll() {
        return feedbacksRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbacksResp> getByPlace(Long locId) {
        return feedbacksRepository.findByPlaces_IdLocOrderByDateCreateDesc(locId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbacksResp getById(Long id) {
        return toResponse(getFeedback(id));
    }

    public FeedbacksResp update(Long id, FeedbacksReq req) {
        Feedbacks feedback = getFeedback(id);
        authService.requireOwnerOrAdmin(feedback.getUser().getIdUser());

        feedback.setAssessment(req.getAssessment());
        feedback.setCommentary(req.getCommentary() != null ? req.getCommentary().trim() : null);

        return toResponse(feedbacksRepository.save(feedback));
    }

    public void delete(Long id) {
        Feedbacks feedback = getFeedback(id);
        authService.requireOwnerOrAdmin(feedback.getUser().getIdUser());
        feedbacksRepository.delete(feedback);
    }

    private Feedbacks getFeedback(Long id) {
        return feedbacksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada"));
    }

    private Places getPlace(Long id) {
        return placesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local não encontrado"));
    }

    private FeedbacksResp toResponse(Feedbacks feedback) {
        FeedbacksResp response = new FeedbacksResp();
        response.setId(feedback.getIdFeed());
        response.setUserId(feedback.getUser().getIdUser());
        response.setUserName(feedback.getUser().getName());
        response.setLocId(feedback.getPlaces().getIdLoc());
        response.setPlaceName(feedback.getPlaces().getName());
        response.setAssessment(feedback.getAssessment());
        response.setCommentary(feedback.getCommentary());
        response.setDateCreate(feedback.getDateCreate());
        return response;
    }
}
