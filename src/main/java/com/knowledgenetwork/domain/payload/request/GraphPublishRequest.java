package com.knowledgenetwork.domain.payload.request;

import com.knowledgenetwork.domain.enums.LicenseType;

public class GraphPublishRequest {

    private LicenseType licenseType = LicenseType.ALL_RIGHTS_RESERVED;
    private String customAttribution;

    public GraphPublishRequest() {
    }

    public GraphPublishRequest(LicenseType licenseType, String customAttribution) {
        this.licenseType = licenseType != null ? licenseType : LicenseType.ALL_RIGHTS_RESERVED;
        this.customAttribution = customAttribution;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType != null ? licenseType : LicenseType.ALL_RIGHTS_RESERVED;
    }

    public String getCustomAttribution() {
        return customAttribution;
    }

    public void setCustomAttribution(String customAttribution) {
        this.customAttribution = customAttribution;
    }
}
