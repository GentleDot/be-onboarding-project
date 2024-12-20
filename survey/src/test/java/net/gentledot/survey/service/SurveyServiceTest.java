package net.gentledot.survey.service;

import net.gentledot.survey.application.service.in.model.request.SurveyCreateRequest;
import net.gentledot.survey.application.service.in.model.request.SurveyQuestionOptionRequest;
import net.gentledot.survey.application.service.in.model.request.SurveyQuestionRequest;
import net.gentledot.survey.domain.enums.ItemRequired;
import net.gentledot.survey.domain.enums.SurveyItemType;
import net.gentledot.survey.domain.exception.ServiceError;
import net.gentledot.survey.domain.exception.SurveyCreationException;
import net.gentledot.survey.domain.surveybase.Survey;
import net.gentledot.survey.domain.surveybase.SurveyQuestion;
import net.gentledot.survey.domain.surveybase.SurveyQuestionOption;
import net.gentledot.survey.domain.surveybase.dto.SurveyQuestionDto;
import net.gentledot.survey.domain.surveybase.dto.SurveyQuestionOptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurveyServiceTest {
    SurveyCreateRequest surveyRequest;

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

    @DisplayName("service 구상 및 제약조건 설정 로직 구상")
    @Test
    void createServiceTest() {

        /*
         *  필요한 제약 조건
         * */
        List<SurveyQuestion> questions = surveyRequest.getQuestions().stream()
                .map(this::convertToSurveyQuestion)
                .collect(Collectors.toList());

        Survey survey = Survey.of(
                surveyRequest.getName(),
                surveyRequest.getDescription(),
                questions
        );

        assertThat(survey).isNotNull();
        assertThat(survey.getId()).isNotBlank();
        assertThat(survey.getName()).isEqualTo(surveyRequest.getName());
        assertThat(survey.getDescription()).isEqualTo(surveyRequest.getDescription());
    }

    @DisplayName("service 구상 및 제약조건 설정 로직 구상 - SurveyRequest 로 Question과 Option 생성되도록 변경")
    @Test
    void createServiceTestVer2() {
        Survey survey = generateSurvey(surveyRequest);

        assertThat(survey).isNotNull();
        assertThat(survey.getId()).isNotBlank();
        assertThat(survey.getName()).isEqualTo(surveyRequest.getName());
        assertThat(survey.getDescription()).isEqualTo(surveyRequest.getDescription());
    }

    @DisplayName("Survey 생성 요청 시 질문 없이 요청하는 경우 예외 발생")
    @Test
    void testFailWhenRequestWithNoQuestion() {
        SurveyCreateRequest requestWithNoQuestions = SurveyCreateRequest.builder()
                .name("no questions!")
                .description("description")
                .questions(Collections.emptyList())
                .build();

        assertThatThrownBy(() -> generateSurvey(requestWithNoQuestions))
                .isInstanceOf(SurveyCreationException.class);
    }

    @DisplayName("Survey 생성 요청 시 단일 또는 다중선택을 설정했음에도 옵션을 입력하지 않으면 예외 발생")
    @Test
    void createSurveyQuestionWithMultiSelectButNoOptions() {
        SurveyCreateRequest requestWithMultiSelectNoOptions = SurveyCreateRequest.builder()
                .name("test survey with multi-select question but no options")
                .description("description")
                .questions(List.of(
                        SurveyQuestionRequest.builder()
                                .question("question1")
                                .description("question1 description")
                                .type(SurveyItemType.MULTI_SELECT)
                                .required(ItemRequired.REQUIRED)
                                .options(Collections.emptyList())
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> generateSurvey(requestWithMultiSelectNoOptions))
                .isInstanceOf(SurveyCreationException.class);
    }


    private Survey generateSurvey(SurveyCreateRequest surveyRequest) {
        validateRequest(surveyRequest);

        List<SurveyQuestion> questions = convertToSurveyQuestions(surveyRequest.getQuestions());

        return Survey.of(
                surveyRequest.getName(),
                surveyRequest.getDescription(),
                questions
        );
    }

    private void validateRequest(SurveyCreateRequest surveyRequest) {
        List<SurveyQuestionRequest> questions = surveyRequest.getQuestions();

        if (questions == null) {
            throw new SurveyCreationException(ServiceError.CREATION_INVALID_REQUEST);
        } else if (questions.isEmpty() || questions.size() > 10) {
            throw new SurveyCreationException(ServiceError.CREATION_INSUFFICIENT_QUESTIONS);
        }

        for (SurveyQuestionRequest question : questions) {
            if ((question.getType() == SurveyItemType.SINGLE_SELECT || question.getType() == SurveyItemType.MULTI_SELECT)
                && (question.getOptions() == null || question.getOptions().isEmpty())) {
                throw new SurveyCreationException(ServiceError.CREATION_INSUFFICIENT_OPTIONS);
            }
        }
    }

    private List<SurveyQuestion> convertToSurveyQuestions(List<SurveyQuestionRequest> questionRequests) {
        return questionRequests.stream()
                .map(SurveyQuestionDto::from)
                .map(SurveyQuestion::from)
                .collect(Collectors.toList());
    }

    private SurveyQuestion convertToSurveyQuestion(SurveyQuestionRequest questionRequest) {
        List<SurveyQuestionOption> options = questionRequest.getOptions()
                .stream()
                .map(this::convertToSurveyItemOption)
                .collect(Collectors.toList());

        return SurveyQuestion.of(
                questionRequest.getQuestion(),
                questionRequest.getDescription(),
                questionRequest.getType(),
                questionRequest.getRequired(),
                options
        );
    }

    private SurveyQuestionOption convertToSurveyItemOption(SurveyQuestionOptionRequest optionRequest) {
        return SurveyQuestionOption.from(new SurveyQuestionOptionDto(optionRequest.getOption()));
    }
}