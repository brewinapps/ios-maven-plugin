package de.letsdev.maven.plugins.ios;

public class ProvisioningProfileData {

    private static final String DEVELOPMENT = "development";
    private static final String ENTERPRISE = "enterprise";
    private static final String AD_HOC = "ad-hoc";
    private static final String APP_STORE = "app-store";
    private String uuid;
    private String teamId;
    private String name;
    private ProvisioningProfileType type;
    private String bundleId;

    ProvisioningProfileData(String uuid, String name, String teamId, String bundleId, ProvisioningProfileType type) {

        this.uuid = uuid;
        this.name = name;
        this.teamId = teamId;
        this.type = type;
        this.bundleId = bundleId;
    }

    public String getUuid() {

        return uuid;
    }

    public String getName() {

        return name;
    }

    public String getTeamId() {

        return teamId;
    }

    public String getBundleId() {

        return bundleId;
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