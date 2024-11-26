package net.gentledot.survey.service;

import jakarta.transaction.Transactional;
import net.gentledot.survey.dto.request.SurveyGenerateRequest;
import net.gentledot.survey.dto.request.SurveyQuestionRequest;
import net.gentledot.survey.exception.ServiceError;
import net.gentledot.survey.exception.SurveyCreationException;
import net.gentledot.survey.model.entity.Survey;
import net.gentledot.survey.model.entity.SurveyQuestion;
import net.gentledot.survey.model.enums.SurveyItemType;
import net.gentledot.survey.repository.SurveyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {
    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Transactional
    public Survey createSurvey(SurveyGenerateRequest surveyRequest) {
        validateCreateRequest(surveyRequest);

        List<SurveyQuestion> questions = convertToSurveyQuestions(surveyRequest.getQuestions());

        Survey survey = Survey.of(
                surveyRequest.getName(),
                surveyRequest.getDescription(),
                questions
        );

        return surveyRepository.save(survey);
    }

    private List<SurveyQuestion> convertToSurveyQuestions(List<SurveyQuestionRequest> questionRequests) {
        return questionRequests.stream()
                .map(SurveyQuestion::from)
                .collect(Collectors.toList());
    }

    private void validateCreateRequest(SurveyGenerateRequest surveyRequest) {
        List<SurveyQuestionRequest> questions = surveyRequest.getQuestions();

        if (questions == null) {
            throw new SurveyCreationException(ServiceError.CREATION_INVALID_REQUEST);
        } else if (questions.isEmpty() || questions.size() > 10) {
            throw new SurveyCreationException(ServiceError.CREATION_INSUFFICIENT_QUESTIONS);
        }

        for (SurveyQuestionRequest question : questions) {
            if ((question.getItemType() == SurveyItemType.SINGLE_SELECT || question.getItemType() == SurveyItemType.MULTI_SELECT)
                && (question.getOptions() == null || question.getOptions().isEmpty())) {
                throw new SurveyCreationException(ServiceError.CREATION_INSUFFICIENT_OPTIONS);
            }
        }
    }


}
