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

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;


/**
 * @author let's dev
 */
public class ProjectDeployer {

    /**
     * @param properties
     * @throws IOSException
     */
    public static void deploy(final Map<String, String> properties)
            throws IOSException {

        System.out.println("Deploying to HockeyApp ...");
        try {
            File appPath = new File(properties.get(Utils.PLUGIN_PROPERTIES.TARGET_DIR.toString()) + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-iphoneos/");

            // Prepare dSYM
            ProcessBuilder pb = new ProcessBuilder(
                    "zip",
                    "-r",
                    properties.get(Utils.PLUGIN_PROPERTIES.APPNAME.toString()) + ".dSYM.zip",
                    properties.get(Utils.PLUGIN_PROPERTIES.APPNAME.toString()) + ".app.dSYM");
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
                    new File(appPath + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APPNAME.toString()) + "." + Utils.PLUGIN_SUFFIX.IPA.toString()),
                    "application/zip"));
            entity.addPart("dsym", new FileBody(
                    new File(appPath + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APPNAME.toString()) + ".dSYM.zip"),
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
