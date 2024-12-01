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
public class SurveyUpdateRequest implements SurveyRequest {
    private String id;
    private String name;
    private String description;
    private List<SurveyQuestionRequest> questions;
}
