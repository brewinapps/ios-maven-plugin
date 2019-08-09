package de.letsdev.maven.plugins.ios;

import de.letsdev.maven.plugins.ios.mojo.IOSException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class ProvisioningProfileHelper {

    private static final String provisioningProfileDirectory =
            System.getProperty("user.home") + "/Library/MobileDevice/Provisioning\\ " + "Profiles";
    private static final String provisioningProfileFileExtension = ".mobileprovision";
    private static final String shellScriptFileName = "load-xml-file.sh";
    private final String provisioningProfileName;
    private final Map<String, String> properties;
    private MavenProject mavenProject;
    private String projectName;
    private File workDirectory;

    public ProvisioningProfileHelper(String provisioningProfileName, Map<String, String> properties,
                                     MavenProject mavenProject) {

        this.provisioningProfileName = provisioningProfileName;
        this.properties = properties;
        this.mavenProject = mavenProject;

        try {
            this.projectName = Utils.buildProjectName(properties, mavenProject);
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

        File tempFile = File.createTempFile(shellScriptFileName, "sh");
        InputStream iStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + shellScriptFileName);
        OutputStream oStream = new FileOutputStream(tempFile);

        try {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = iStream.read(buffer)) != -1) {
                oStream.write(buffer, 0, bytesRead);
            }
        } finally {
            oStream.flush();
            oStream.close();
        }

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
            String teamID = null;

            NodeList nodeList = doc.getElementsByTagName("key");
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getFirstChild().getNodeValue().equals("UUID")) {
                    Node current = nodeList.item(i).getNextSibling();
                    //iterate trough notes until node with value for the uuid key is current
                    while (!current.getNodeName().equals("string")) {
                        current = current.getNextSibling();
                    }
                    uuid = current.getFirstChild().getNodeValue();
                }

                if (nodeList.item(i).getFirstChild().getNodeValue().equals("TeamIdentifier")) {
                    Node current = nodeList.item(i).getNextSibling();
                    //iterate trough notes until array node, that has value of team id key as child is current
                    while (!current.getNodeName().equals("array")) {
                        current = current.getNextSibling();
                    }
                    current = current.getFirstChild();
                    while (!current.getNodeName().equals("string")) {
                        current = current.getNextSibling();
                    }
                    teamID = current.getFirstChild().getNodeValue();
                }
            }

            ProvisioningProfileType type = getProvisioningProfileType(nodeList);

            if (teamID != null && uuid != null) {
                return new ProvisioningProfileData(uuid, teamID, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getProvisioningProfileFilePath(String provisioningProfileName) {

        return provisioningProfileDirectory + "/" + provisioningProfileName + provisioningProfileFileExtension;
    }

    private static ProvisioningProfileType getProvisioningProfileType(NodeList nodeList) {

        ProvisioningProfileType type = null;
        for (int i = 0; i < nodeList.getLength(); i++) {

            if (checkKey("get-task-allow", nodeList.item(i))) {
                type = ProvisioningProfileType.TYPE_DEVELOPEMENT;
            } else if (checkKey("ProvisionsAllDevices", nodeList.item(i))) {
                type = ProvisioningProfileType.TYPE_ENTERPRISE;
            } else if (nodeList.item(i).getFirstChild().getNodeValue().equals("ProvisionedDevices")) {
                Node current = nodeList.item(i).getNextSibling();
                while (true) {
                    current = current.getNextSibling();
                    if (current.getNodeName().equals("array")) {
                        if (current.hasChildNodes()) {
                            type = ProvisioningProfileType.TYPE_AD_HOC;
                        }

                        break;
                    }

                    if (current.getNodeName().equals("key")) {
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

    public static boolean checkKey(String keyName, Node node) {

        if (node.getFirstChild().getNodeValue().equals(keyName)) {
            Node current = node.getNextSibling();
            while (true) {
                current = current.getNextSibling();
                if (current.getNodeName().equals("true")) {
                    return true;
                }
                if (current.getNodeName().equals("key")) {
                    return false;
                }
            }
        }
        return false;
    }
}
