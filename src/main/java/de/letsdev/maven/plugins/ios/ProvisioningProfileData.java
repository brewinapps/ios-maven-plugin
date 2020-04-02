package de.letsdev.maven.plugins.ios;

public class ProvisioningProfileData {

    private static final String DEVELOPMENT = "development";
    private static final String ENTERPRISE = "enterprise";
    private static final String AD_HOC = "ad-hoc";
    private static final String APP_STORE = "app-store";
    private String uuid;
    private String teamID;
    private String name;
    private ProvisioningProfileType type;
    private String bundleID;

    ProvisioningProfileData(String uuid, String name, String teamID, String bundleID, ProvisioningProfileType type) {

        this.uuid = uuid;
        this.name = name;
        this.teamID = teamID;
        this.type = type;
        this.bundleID = bundleID;
    }

    public String getUuid() {

        return uuid;
    }

    public String getName() {

        return name;
    }

    public String getTeamID() {

        return teamID;
    }

    public String getBundleID() {

        return bundleID;
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