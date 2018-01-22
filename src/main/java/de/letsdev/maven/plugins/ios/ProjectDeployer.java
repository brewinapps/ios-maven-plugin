/**
 * Maven iOS Plugin
 *
 * User: sbott
 * Date: 19.07.2012
 * Time: 19:54:44
 *
 * This code is copyright (c) 2012 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

import de.letsdev.maven.plugins.ios.mojo.IOSException;


/**
 * @author let's dev
 */
public class ProjectDeployer {

    /**
     * @param properties
     * @throws IOSException
     */
    public static void deploy(final Map<String, String> properties, MavenProject mavenProject)
            throws IOSException, IOException {

        if (properties.get(Utils.PLUGIN_PROPERTIES.HOCKEY_APP_TOKEN.toString()) != null) {
            deployHockey(properties);
        } else {
            String releaseTask = System.getProperty("RELEASE_TASK");
            System.out.println("release task from environment variable is " + releaseTask);
            if (releaseTask == null || releaseTask.isEmpty()) {
                releaseTask = properties.get(Utils.PLUGIN_PROPERTIES.RELEASE_TASK.toString());
            }

            System.out.println("Determining deploying target, parsing environment variable / pom parameter RELEASE_TASK=" + releaseTask);
            if (Utils.isAppStoreBuild(properties)) {
                deployAppStore(properties, mavenProject);
            } else if (Utils.isTestflightBuild(properties)) {
                deployTestflight(properties, mavenProject);
            }
        }
    }

    private static void deployAppStore(final Map<String, String> properties, MavenProject mavenProject) throws IOException, IOSException {
        System.out.println("Deploying to AppStore ...");

        uploadToAppStore(properties, mavenProject);
    }

    private static void deployTestflight(final Map<String, String> properties, MavenProject mavenProject) throws IOException, IOSException {
        System.out.println("Deploying to Testflight ...");

        uploadToAppStore(properties, mavenProject);
    }

    private static void uploadToAppStore(final Map<String, String> properties, MavenProject mavenProject) throws IOException, IOSException{
        System.out.println("Starting app store upload...");

        // Run shell-script from resource-folder.
        final String scriptName = "upload-app.sh";

        String projectVersion = Utils.getAdjustedVersion(mavenProject, properties);
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
            projectVersion += "-b" + properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString());
        }

        File targetDirectory = new File(properties.get(Utils.PLUGIN_PROPERTIES.TARGET_DIR.toString()));
        final String ipaLocation = targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "-" + projectVersion + "." + Utils.PLUGIN_SUFFIX.IPA;
        final String iTunesConnectUsername = properties.get(Utils.PLUGIN_PROPERTIES.ITUNES_CONNECT_USERNAME.toString());
        final String iTunesConnectPassword = properties.get(Utils.PLUGIN_PROPERTIES.ITUNES_CONNECT_PASSWORD.toString());

        File tempFile = File.createTempFile(scriptName, "sh");

        InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName);
        OutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();

        //get current xcode version
        String projectName = Utils.buildProjectName(properties, mavenProject);
        File workDirectory = Utils.getWorkDirectory(properties, mavenProject, projectName);
        String currentXcodeVersion = Utils.getCurrentXcodeVersion(workDirectory);

        ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                ipaLocation,
                iTunesConnectUsername,
                iTunesConnectPassword,
                currentXcodeVersion);

        processBuilder.directory(targetDirectory);
        CommandHelper.performCommand(processBuilder);
    }

    private static void deployHockey(final Map<String, String> properties)
            throws IOSException {

        System.out.println("Deploying to HockeyApp ...");
        try {
            File appPath = new File(properties.get(Utils.PLUGIN_PROPERTIES.TARGET_DIR.toString()) + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-iphoneos/");

            // Prepare dSYM
            ProcessBuilder pb = new ProcessBuilder(
                    "zip",
                    "-r",
                    properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + ".dSYM.zip",
                    properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + ".app.dSYM");
            pb.directory(appPath);
            CommandHelper.performCommand(pb);

            // Prepare HTTP request
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            HttpPost post = new HttpPost("https://rink.hockeyapp.net/api/2/apps");
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            // Set headers and parameters
            post.addHeader("X-HockeyAppToken", properties.get(Utils.PLUGIN_PROPERTIES.HOCKEY_APP_TOKEN.toString()));
            entity.addPart(Utils.PLUGIN_SUFFIX.IPA.toString(), new FileBody(
                    new File(appPath + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "." + Utils.PLUGIN_SUFFIX.IPA.toString()),
                    "application/zip"));
            entity.addPart("dsym", new FileBody(
                    new File(appPath + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + ".dSYM.zip"),
                    "application/zip"));
            entity.addPart("notes",
                    new StringBody(properties.get(Utils.PLUGIN_PROPERTIES.RELEASE_NOTES.toString()),
                            "text/plain", Charset.forName("UTF-8")));
            post.setEntity(entity);

            // Run the request
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();

            System.out.println(response.getStatusLine());
            if (responseEntity != null) {
                System.out.println(EntityUtils.toString(responseEntity));
            }

            client.getConnectionManager().shutdown();
        } catch (Exception e) {
            throw new IOSException("An error occured while deploying build to HockeyApp: " + e.getMessage());
        }
    }
}
