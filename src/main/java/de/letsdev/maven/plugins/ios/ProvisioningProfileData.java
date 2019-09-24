package de.letsdev.maven.plugins.ios;

public class ProvisioningProfileData {

    private String uuid;
    private String teamID;
    private ProvisioningProfileType type;

    public ProvisioningProfileData(String uuid, String teamID, ProvisioningProfileType type) {

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
}