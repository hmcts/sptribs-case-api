package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTypeTest {

    @Test
    void shouldReturnTrueIfSoleApplicationType() {
        assertThat(ApplicationType.SOLE_APPLICATION.isSole()).isTrue();
    }

    @Test
    void shouldReturnLabel() {
        assertThat(ApplicationType.SOLE_APPLICATION.getLabel()).isEqualTo("Sole Application");
    }

    @Test
    void shouldReturnFalseIfJointApplicationType() {
        assertThat(ApplicationType.JOINT_APPLICATION.isSole()).isFalse();
    }
}
