package net.gentledot.survey.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@ToString
@Getter
@AllArgsConstructor
public class SurveyCreateRequest implements SurveyRequest {
    private String name;
    private String description;
    private List<SurveyQuestionRequest> questions;
}
