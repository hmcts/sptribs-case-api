package uk.gov.hmcts.sptribs.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class CicValue<T> implements Serializable {

    private static final long serialVersionUID = 7878L;

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public CicValue(T value) {
        this.value = value;
    }

    public CicValue() {
        log.info("CicValue no args constructor");
    }
}
