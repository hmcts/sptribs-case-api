package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class CicValue<T> implements Serializable {

    private static final long serialVersionUID = 716976488443039180L;

    private T value;
}
