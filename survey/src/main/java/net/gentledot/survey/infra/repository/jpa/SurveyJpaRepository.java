package net.gentledot.survey.infra.repository.jpa;

import net.gentledot.survey.domain.surveybase.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyJpaRepository extends JpaRepository<Survey, String> {
}
