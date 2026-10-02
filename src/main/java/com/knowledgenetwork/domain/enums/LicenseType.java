package com.knowledgenetwork.domain.enums;

public enum LicenseType {
    ALL_RIGHTS_RESERVED,
    CC0_1_0,
    CC_BY_4_0,
    CC_BY_SA_4_0,
    CC_BY_NC_4_0,
    CC_BY_NC_SA_4_0,
    CC_BY_ND_4_0,
    CC_BY_NC_ND_4_0;

    public boolean allowsDerivatives() {
        return this == CC0_1_0 || this == CC_BY_4_0 || this == CC_BY_SA_4_0 || this == CC_BY_NC_4_0 || this == CC_BY_NC_SA_4_0;
    }

    public boolean requiresShareAlike() {
        return this == CC_BY_SA_4_0 || this == CC_BY_NC_SA_4_0;
    }

    public boolean allowsCommercial() {
        return this == ALL_RIGHTS_RESERVED || this == CC0_1_0 || this == CC_BY_4_0 || this == CC_BY_SA_4_0 || this == CC_BY_ND_4_0;
    }

    public boolean requiresAttribution() {
        return this != CC0_1_0;
    }
}
