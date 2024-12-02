package net.gentledot.survey.service;

import jakarta.transaction.Transactional;
import net.gentledot.survey.dto.request.SurveyCreateRequest;
import net.gentledot.survey.dto.request.SurveyQuestionOptionRequest;
import net.gentledot.survey.dto.request.SurveyQuestionRequest;
import net.gentledot.survey.dto.request.SurveyUpdateRequest;
import net.gentledot.survey.dto.response.SurveyCreateResponse;
import net.gentledot.survey.dto.response.SurveyUpdateResponse;
import net.gentledot.survey.model.entity.Survey;
import net.gentledot.survey.model.entity.SurveyQuestion;
import net.gentledot.survey.model.enums.ItemRequired;
import net.gentledot.survey.model.enums.SurveyItemType;
import net.gentledot.survey.repository.SurveyQuestionOptionRepository;
import net.gentledot.survey.repository.SurveyQuestionRepository;
import net.gentledot.survey.repository.SurveyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class SurveyServiceProcessTest {

    private SurveyCreateRequest surveyRequest;

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    SurveyQuestionRepository questionRepository;

    @Autowired
    SurveyQuestionOptionRepository questionOptionRepository;
    @Autowired
    private SurveyQuestionRepository surveyQuestionRepository;

    @BeforeEach
    void setUp() {
        surveyRequest = SurveyCreateRequest.builder()
                .name("test")
                .description("survey description")
                .questions(List.of(
                        SurveyQuestionRequest.builder()
                                .question("question1")
                                .description("question1 description")
                                .type(SurveyItemType.MULTI_SELECT)
                                .required(ItemRequired.REQUIRED)
                                .options(List.of(
                                        new SurveyQuestionOptionRequest("option1"),
                                        new SurveyQuestionOptionRequest("option2")
                                )).build()
                )).build();
    }


    @Test
    void createProcessTest() {
        SurveyCreateResponse createdSurvey = surveyService.createSurvey(surveyRequest);
        Assertions.assertThat(createdSurvey.surveyId()).isNotBlank();
        Assertions.assertThat(createdSurvey.createdAt()).isBefore(LocalDateTime.now());
    }

    @Transactional
    @Test
    void updateProcessTest() {
        // a
        SurveyCreateResponse createdSurvey = surveyService.createSurvey(surveyRequest);
        String surveyId = createdSurvey.surveyId();

        Survey survey = surveyRepository.findById(surveyId).get();
        List<SurveyQuestion> questions = survey.getQuestions();
        SurveyQuestion beforeQuestion = surveyQuestionRepository.findById(questions.get(0).getId()).get();

        SurveyUpdateRequest updateRequest = SurveyUpdateRequest.builder()
                .id(surveyId)
                .name("test changed")
                .description("changed description")
                .questions(List.of(
                        SurveyQuestionRequest.builder()
                                .question("changed1")
                                .description("changed1 description")
                                .type(SurveyItemType.MULTI_SELECT)
                                .required(ItemRequired.REQUIRED)
                                .options(List.of(
                                        new SurveyQuestionOptionRequest("changed option1"),
                                        new SurveyQuestionOptionRequest("changed option2")
                                )).build()
                )).build();

        // a
        SurveyUpdateResponse surveyUpdateResponse = surveyService.updateSurvey(updateRequest);

        Survey updatedSurvey = surveyRepository.findById(surveyId).get();
        List<SurveyQuestion> updatedQuestions = updatedSurvey.getQuestions();


        // a
        Assertions.assertThat(surveyUpdateResponse.surveyId()).isEqualTo(surveyId);
        Assertions.assertThat(surveyUpdateResponse.updatedAt()).isBefore(LocalDateTime.now());

        Assertions.assertThat(updatedQuestions).hasSize(1);
        SurveyQuestion updatedQuestion = updatedQuestions.get(0);
        Assertions.assertThat(updatedQuestion.getSurvey()).isEqualTo(updatedSurvey);
        Assertions.assertThat(updatedQuestion.getItemName()).isEqualTo("changed1");
        Assertions.assertThat(updatedQuestion.getItemDescription()).isEqualTo("changed1 description");
        Assertions.assertThat(updatedQuestion.getOptions()).hasSize(2);
        Assertions.assertThat(updatedQuestion.getOptions().get(0).getOptionText()).isEqualTo("changed option1");
        Assertions.assertThat(updatedQuestion.getOptions().get(1).getOptionText()).isEqualTo("changed option2");

        Assertions.assertThat(beforeQuestion.getSurvey()).isNull();
        Assertions.assertThat(beforeQuestion.getItemName()).isEqualTo("question1");
        Assertions.assertThat(updatedQuestion.getSurvey()).isEqualTo(updatedSurvey);
    }
}