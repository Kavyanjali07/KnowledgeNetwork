package com.knowledgenetwork.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LicenseTypeTest {

    @Test
    @DisplayName("Should correctly evaluate derivative permissions for licenses")
    void testAllowsDerivatives() {
        assertThat(LicenseType.ALL_RIGHTS_RESERVED.allowsDerivatives()).isFalse();
        assertThat(LicenseType.CC_BY_4_0.allowsDerivatives()).isTrue();
        assertThat(LicenseType.CC_BY_SA_4_0.allowsDerivatives()).isTrue();
        assertThat(LicenseType.CC_BY_NC_4_0.allowsDerivatives()).isTrue();
        assertThat(LicenseType.CC_BY_ND_4_0.allowsDerivatives()).isFalse();
    }

    @Test
    @DisplayName("Should correctly evaluate attribution requirements")
    void testRequiresAttribution() {
        assertThat(LicenseType.ALL_RIGHTS_RESERVED.requiresAttribution()).isTrue();
        assertThat(LicenseType.CC_BY_4_0.requiresAttribution()).isTrue();
        assertThat(LicenseType.CC_BY_SA_4_0.requiresAttribution()).isTrue();
        assertThat(LicenseType.CC_BY_NC_4_0.requiresAttribution()).isTrue();
        assertThat(LicenseType.CC_BY_ND_4_0.requiresAttribution()).isTrue();
    }

    @Test
    @DisplayName("Should correctly evaluate ShareAlike requirements")
    void testRequiresShareAlike() {
        assertThat(LicenseType.ALL_RIGHTS_RESERVED.requiresShareAlike()).isFalse();
        assertThat(LicenseType.CC_BY_4_0.requiresShareAlike()).isFalse();
        assertThat(LicenseType.CC_BY_SA_4_0.requiresShareAlike()).isTrue();
        assertThat(LicenseType.CC_BY_NC_4_0.requiresShareAlike()).isFalse();
        assertThat(LicenseType.CC_BY_ND_4_0.requiresShareAlike()).isFalse();
    }
}
