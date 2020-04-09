/**
 * ios-maven-plugin
 * <p/>
 * User: mauer
 * Date: 2019-07-09
 * <p/>
 * This code is copyright (c) 2019 let's dev.
 * URL: https://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios;

import de.letsdev.maven.plugins.ios.mojo.BaseMojo;
import de.letsdev.maven.plugins.ios.mojo.IOSException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProvisioningProfileHelper {

    private static final String NODE_NAME_STRING = "string";
    private static final String NODE_NAME_ARRAY = "array";
    private static final String NODE_NAME_KEY = "key";
    private static final String NODE_NAME_TRUE = "true";
    private static final String TEAM_ID_KEY = "TeamIdentifier";
    private static final String UUID_KEY = "UUID";
    private static final String NAME_KEY = "Name";
    private static final String APP_ID_KEY = "application-identifier";
    private static final String GET_TASK_ALLOW_KEY = "get-task-allow";
    private static final String PROVISION_DEVICES_KEY = "ProvisionsAllDevices";
    private static final String PROVISIONED_DEVICES_KEY = "ProvisionedDevices";
    private static final String provisioningProfileDirectory =
            System.getProperty("user.home") + "/Library/MobileDevice/Provisioning\\ " + "Profiles";
    private static final String provisioningProfileFileExtension = ".mobileprovision";
    private static final String shellScriptFileName = "load-xml-file.sh";
    private final String provisioningProfileName;
    private File workDirectory;
    private Map<String, String> properties;

    public ProvisioningProfileHelper(String provisioningProfileName, Map<String, String> properties,
                                     MavenProject mavenProject) {

        this.provisioningProfileName = provisioningProfileName;
        this.properties = properties;

        try {
            String projectName = Utils.buildProjectName(properties, mavenProject);
            this.workDirectory = Utils.getWorkDirectory(properties, mavenProject, projectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProvisioningProfileData getData() throws IOSException, IOException {

        File outputFile = this.buildProcess();

        return this.createProvisioningProfileData(outputFile);
    }

    private File buildProcess() throws IOSException, IOException {

        final String filepath = getProvisioningProfileFilePath(this.provisioningProfileName);

        File tempFile = Utils.createTempFile(shellScriptFileName);

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", tempFile.getAbsoluteFile().toString(),
                filepath);
        processBuilder.directory(workDirectory);

        File outputFile = File.createTempFile("outputFile.xml", "xml");
        processBuilder.redirectOutput(outputFile);
        CommandHelper.performCommand(processBuilder);

        return outputFile;
    }

    private ProvisioningProfileData createProvisioningProfileData(File inputFile) {

        Document doc;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            String uuid = null;
            String teamId = null;
            String name = null;
            String bundleId = null;

            NodeList nodeList = doc.getElementsByTagName(NODE_NAME_KEY);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getFirstChild().getNodeValue().equals(UUID_KEY)) {
                    uuid = getStringValue(nodeList, i);
                }

                if (nodeList.item(i).getFirstChild().getNodeValue().equals(NAME_KEY)) {
                    name = getStringValue(nodeList, i);
                }

                if (nodeList.item(i).getFirstChild().getNodeValue().equals(TEAM_ID_KEY)) {
                    teamId = getTeamId(nodeList, i);
                }

                if (nodeList.item(i).getFirstChild().getNodeValue().equals(APP_ID_KEY)) {
                    bundleId = getStringValue(nodeList, i);
                }
            }

            if (bundleId == null || bundleId.contains("*")) {
                bundleId = properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString());
            } else {
                properties.put(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString(), bundleId);
            }

            ProvisioningProfileType type = getProvisioningProfileType(nodeList);

            if (teamId != null && uuid != null && name != null) {
                return new ProvisioningProfileData(uuid, name, teamId, bundleId, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getStringValue(NodeList nodeList, int i) {

        Node current = nodeList.item(i).getNextSibling();
        //iterate trough notes until node with value for the uuid key is current
        while (!current.getNodeName().equals(NODE_NAME_STRING)) {
            current = current.getNextSibling();
        }

        return current.getFirstChild().getNodeValue();
    }

    private String getTeamId(NodeList nodeList, int i) {

        Node current = nodeList.item(i).getNextSibling();
        //iterate trough notes until array node, that has value of team id key as child is current
        while (!current.getNodeName().equals(NODE_NAME_ARRAY)) {
            current = current.getNextSibling();
        }
        current = current.getFirstChild();
        while (!current.getNodeName().equals(NODE_NAME_STRING)) {
            current = current.getNextSibling();
        }
        return current.getFirstChild().getNodeValue();
    }

    private static String getProvisioningProfileFilePath(String provisioningProfileName) {

        return provisioningProfileDirectory + "/" + provisioningProfileName + provisioningProfileFileExtension;
    }

    private static ProvisioningProfileType getProvisioningProfileType(NodeList nodeList) {

        ProvisioningProfileType type = null;
        for (int i = 0; i < nodeList.getLength(); i++) {

            if (checkKey(GET_TASK_ALLOW_KEY, nodeList.item(i))) {
                type = ProvisioningProfileType.TYPE_DEVELOPMENT;
                break;
            } else if (checkKey(PROVISION_DEVICES_KEY, nodeList.item(i))) {
                type = ProvisioningProfileType.TYPE_ENTERPRISE;
                break;
            } else if (nodeList.item(i).getFirstChild().getNodeValue().equals(PROVISIONED_DEVICES_KEY)) {
                Node current = nodeList.item(i).getNextSibling();
                while (true) {
                    current = current.getNextSibling();
                    if (current.getNodeName().equals(NODE_NAME_ARRAY)) {
                        if (current.hasChildNodes()) {
                            type = ProvisioningProfileType.TYPE_AD_HOC;
                        }

                        break;
                    }

                    if (current.getNodeName().equals(NODE_NAME_KEY)) {
                        break;
                    }
                }
            }
        }

        if (type == null) {
            type = ProvisioningProfileType.TYPE_APP_STORE;
        }
        return type;
    }

    private static boolean checkKey(String keyName, Node node) {

        if (node.getFirstChild().getNodeValue().equals(keyName)) {
            Node current = node.getNextSibling();
            while (true) {
                current = current.getNextSibling();
                if (current.getNodeName().equals(NODE_NAME_TRUE)) {
                    return true;
                }
                if (current.getNodeName().equals(NODE_NAME_KEY)) {
                    return false;
                }
            }
        }
        return false;
    }
}
