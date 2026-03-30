package uk.gov.hmcts.sptribs.notification;

import java.time.LocalDate;

public enum EmailRespondentResponses {

    OUT_OF_TIME_RESPONSE(
        "Out of time appeal - You should provide the tribunal with a case bundle by %s. "
            + "Do not issue to the Subject/Applicant/Representative until we notify you the appeal has been admitted."
    ),
    IN_TIME_RESPONSE(
        "You should provide the tribunal and the "
            + "Subject/Applicant/Representative with a case bundle by %s"
    );

    private final String template;

    EmailRespondentResponses(String template) {
        this.template = template;
    }

    public String format(LocalDate date) {
        return String.format(template, date);
    }
}
