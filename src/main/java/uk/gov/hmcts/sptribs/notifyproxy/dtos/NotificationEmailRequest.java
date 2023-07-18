package uk.gov.hmcts.sptribs.notifyproxy.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.sptribs.notifyproxy.dtos.enums.NotificationType;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(NON_NULL)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "refundNotificationEmailRequestWith")
public class NotificationEmailRequest {

    @NotNull(message = "Template ID cannot be null")
    @NotEmpty(message = "Template ID cannot be blank")
    private String templateId;

    @NotNull(message = "Recipient Email Address cannot be null")
    @NotEmpty(message = "Recipient Email Address cannot be blank")
    @Email(message = "Please enter a valid Email Address")
    private String recipientEmailAddress;

    @NotNull(message = "Reference cannot be null")
    @NotEmpty(message = "Reference cannot be blank")
    private String reference;

    private String emailReplyToId;

    @ApiModelProperty(example = "EMAIL")
    @Value("EMAIL")
    private NotificationType notificationType;

    @NotNull
    @Valid
    private Personalisation personalisation;

    @NotNull(message = "service Name cannot be null")
    @NotEmpty(message = "Service cannot be blank")
    private String serviceName;

    /*private TemplatePreviewDto templatePreview;*/
}
