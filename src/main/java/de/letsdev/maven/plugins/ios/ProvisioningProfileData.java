package de.letsdev.maven.plugins.ios;

public class ProvisioningProfileData {

    private static final String DEVELOPMENT = "development";
    private static final String ENTERPRISE = "enterprise";
    private static final String AD_HOC = "ad-hoc";
    private static final String APP_STORE = "app-store";
    private String uuid;
    private String teamID;
    private ProvisioningProfileType type;

    ProvisioningProfileData(String uuid, String teamID, ProvisioningProfileType type) {

        this.uuid = uuid;
        this.teamID = teamID;
        this.type = type;
    }

    public String getUuid() {

        return uuid;
    }

    public String getTeamID() {

        return teamID;
    }

    public ProvisioningProfileType getType() {

        return type;
    }

    public String getTypeId() {
        String typeId;

        switch (type) {
            case TYPE_DEVELOPMENT:
                typeId = DEVELOPMENT;
                break;
            case TYPE_ENTERPRISE:
                typeId = ENTERPRISE;
                break;
            case TYPE_AD_HOC:
                typeId = AD_HOC;
                break;
            case TYPE_APP_STORE:
            default:
                typeId = APP_STORE;
                break;
        }

        return typeId;
    }
}