package net.gentledot.survey.exception;

public class SurveyNotFoundException extends SurveyServiceException {
    public SurveyNotFoundException(ServiceError serviceError) {
        super(serviceError);
    }

    public SurveyNotFoundException(ServiceError serviceError, Throwable cause) {
        super(serviceError, cause);
    }
}