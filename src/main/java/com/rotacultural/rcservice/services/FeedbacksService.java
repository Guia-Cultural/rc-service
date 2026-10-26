package com.rotacultural.rcservice.services;

import com.rotacultural.rcservice.dtos.feedbacks.FeedbacksReq;
import com.rotacultural.rcservice.dtos.feedbacks.FeedbacksResp;
import com.rotacultural.rcservice.entities.Feedbacks;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.repositories.FeedbacksRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbacksService {

    private final FeedbacksRepository feedbacksRepository;
    private final UsersRepository usersRepository;
    private final PlacesRepository placesRepository;

    public FeedbacksService(
            FeedbacksRepository feedbacksRepository,
            UsersRepository usersRepository,
            PlacesRepository placesRepository
    ) {
        this.feedbacksRepository = feedbacksRepository;
        this.usersRepository = usersRepository;
        this.placesRepository = placesRepository;
    }

    public FeedbacksResp create(FeedbacksReq req) {
        Users user = getUser(req.getUserId());
        Places place = getPlace(req.getLocId());

        Feedbacks feedback = new Feedbacks();
        feedback.setUser(user);
        feedback.setPlaces(place);
        feedback.setAssessment(req.getAssessment());
        feedback.setCommentary(req.getCommentary());

        return toResponse(feedbacksRepository.save(feedback));
    }

    public List<FeedbacksResp> getAll() {
        return feedbacksRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FeedbacksResp getById(Long id) {
        return toResponse(getFeedback(id));
    }

    public FeedbacksResp update(Long id, FeedbacksReq req) {
        Feedbacks feedback = getFeedback(id);

        feedback.setUser(getUser(req.getUserId()));
        feedback.setPlaces(getPlace(req.getLocId()));
        feedback.setAssessment(req.getAssessment());
        feedback.setCommentary(req.getCommentary());

        return toResponse(feedbacksRepository.save(feedback));
    }

    public void delete(Long id) {
        Feedbacks feedback = getFeedback(id);
        feedbacksRepository.delete(feedback);
    }

    private Feedbacks getFeedback(Long id) {
        return feedbacksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Avaliação não encontrada"
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

    private FeedbacksResp toResponse(Feedbacks feedback) {
        FeedbacksResp response = new FeedbacksResp();

        response.setId(feedback.getIdFeed());
        response.setUserId(feedback.getUser().getIdUser());
        response.setLocId(feedback.getPlaces().getIdLoc());
        response.setAssessment(feedback.getAssessment());
        response.setCommentary(feedback.getCommentary());
        response.setDateCreate(feedback.getDateCreate());

        return response;
    }
}