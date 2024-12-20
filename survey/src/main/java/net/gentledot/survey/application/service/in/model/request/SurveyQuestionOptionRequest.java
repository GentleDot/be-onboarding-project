package net.gentledot.survey.application.service.in.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SurveyQuestionOptionRequest {
    private String option;
}
