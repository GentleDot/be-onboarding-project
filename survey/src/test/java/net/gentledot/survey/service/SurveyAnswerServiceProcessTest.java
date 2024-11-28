package net.gentledot.survey.service;

import net.gentledot.survey.dto.request.SubmitSurveyAnswer;
import net.gentledot.survey.exception.SurveyNotFoundException;
import net.gentledot.survey.exception.SurveySubmitValidationException;
import net.gentledot.survey.model.entity.Survey;
import net.gentledot.survey.model.entity.SurveyQuestion;
import net.gentledot.survey.model.entity.SurveyQuestionOption;
import net.gentledot.survey.model.enums.ItemRequired;
import net.gentledot.survey.model.enums.SurveyItemType;
import net.gentledot.survey.repository.SurveyAnswerRepository;
import net.gentledot.survey.repository.SurveyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SurveyAnswerServiceProcessTest {

    @Autowired
    SurveyAnswerService surveyAnswerService;

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    SurveyAnswerRepository surveyAnswerRepository;

    private Survey survey;
    private List<SurveyQuestion> surveyQuestions;
    private List<SurveyQuestionOption> surveyQuestionOptions;

    @BeforeEach
    void setUp() {
        survey = surveyRepository.save(createSurvey());
    }

    private Survey createSurvey() {
        List<SurveyQuestionOption> options = new ArrayList<>();
        options.add(SurveyQuestionOption.of("Option 1"));
        options.add(SurveyQuestionOption.of("Option 2"));
        surveyQuestionOptions = options;
        List<SurveyQuestion> questions = new ArrayList<>();
        questions.add(SurveyQuestion.of("Question 1", "Description 1", SurveyItemType.SINGLE_SELECT, ItemRequired.REQUIRED, surveyQuestionOptions));
        questions.add(SurveyQuestion.of("Question 2", "Description 2", SurveyItemType.TEXT, ItemRequired.OPTIONAL, new ArrayList<>()));
        surveyQuestions = questions;
        return Survey.of("Survey 1", "Description 1", surveyQuestions);
    }

    @Test
    void submitSurveyWithValidSurveyIdAndAnswers() {
        List<SubmitSurveyAnswer> answers = new ArrayList<>();
        answers.add(new SubmitSurveyAnswer(
                survey.getQuestions().get(0).getId(),
                survey.getQuestions().get(0).getOptions().get(0).getId(), "Answer 1"));
        answers.add(new SubmitSurveyAnswer(survey.getQuestions().get(1).getId(), null, "Answer 2"));

        surveyAnswerService.submitSurveyAnswer(survey.getId(), answers);

        Assertions.assertThat(surveyAnswerRepository.findAll()).isNotEmpty();
    }

    @Test
    void failTest_submitSurveyWithInvalidSurveyId() {
        List<SubmitSurveyAnswer> answers = new ArrayList<>();
        answers.add(new SubmitSurveyAnswer(survey.getQuestions().get(0).getId(), survey.getQuestions().get(0).getOptions().get(0).getId(), "Answer 1"));


        Assertions.assertThatThrownBy(() ->
                        surveyAnswerService.submitSurveyAnswer("invalid-id", answers))
                .isInstanceOf(SurveyNotFoundException.class)
                .hasMessageContaining("요청한 서베이를 찾을 수 없습니다.")
                .satisfies(exception -> {
                    SurveyNotFoundException surveyNotFoundException = (SurveyNotFoundException) exception;
                    Assertions.assertThat(surveyNotFoundException.getServiceError().getCode()).isEqualTo("2001");
                    Assertions.assertThat(surveyNotFoundException.getServiceError().getMessage()).isEqualTo("요청한 서베이를 찾을 수 없습니다.");
                });
    }

    @Test
    void failTest_validateSurveyAnswersWithInvalidQuestionId() {
        // Arrange
        List<SubmitSurveyAnswer> answers = new ArrayList<>();
        answers.add(new SubmitSurveyAnswer(999L, survey.getQuestions().get(0).getOptions().get(0).getId(), "Answer 1"));

        // Act & Assert
        assertThrows(SurveySubmitValidationException.class, () -> {
            surveyAnswerService.validateSurveyAnswers(survey, answers);
        });
    }

    @Test
    void failTest_validateSurveyAnswersWithInvalidQuestionOptionId() {
        // Arrange
        List<SubmitSurveyAnswer> answers = new ArrayList<>();
        answers.add(new SubmitSurveyAnswer(survey.getQuestions().get(0).getId(), 999L, "Answer 1"));

        // Act & Assert
        assertThrows(SurveySubmitValidationException.class, () -> {
            surveyAnswerService.validateSurveyAnswers(survey, answers);
        });
    }

    @Test
    void failTest_validateSurveyAnswersWithEmptyAnswerForRequiredQuestion() {
        // Arrange
        List<SubmitSurveyAnswer> answers = new ArrayList<>();
        answers.add(new SubmitSurveyAnswer(survey.getQuestions().get(0).getId(), survey.getQuestions().get(0).getOptions().get(0).getId(), ""));

        // Act & Assert
        assertThrows(SurveySubmitValidationException.class, () -> {
            surveyAnswerService.validateSurveyAnswers(survey, answers);
        });
    }

    @Test
    void failTest_validateSurveyAnswersWithMultipleOptionsForSingleSelectQuestion() {
        // Arrange
        List<SubmitSurveyAnswer> answers = new ArrayList<>();
        answers.add(new SubmitSurveyAnswer(survey.getQuestions().get(0).getId(), survey.getQuestions().get(0).getOptions().get(0).getId(), "Answer 1"));
        answers.add(new SubmitSurveyAnswer(survey.getQuestions().get(0).getId(), survey.getQuestions().get(0).getOptions().get(1).getId(), "Answer 2"));

        // Act & Assert
        assertThrows(SurveySubmitValidationException.class, () -> {
            surveyAnswerService.validateSurveyAnswers(survey, answers);
        });
    }

}