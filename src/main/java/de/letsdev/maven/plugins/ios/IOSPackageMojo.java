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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;


/**
 * @author let's dev
 * @goal package
 * @phase package
 */
public class IOSPackageMojo extends AbstractMojo {

    /**
     * iOS app name
     *
     * @parameter expression="${ios.appName}"
     * @required
     */
    private String appName;

    /**
     * iOS configuration
     *
     * @parameter expression="${ios.configuration}"
     * default-value="Release"
     */
    private String configuration;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject mavenProject;

    /**
     *
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String targetDir = mavenProject.getBuild().getDirectory();
        String destinationDirectory = null;
        String artifactName = null;

        if (mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString())) {
            artifactName = appName + "." + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP;
            destinationDirectory = targetDir;
        }
        else if(mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IPA.toString())) {
            artifactName = appName + "." + Utils.PLUGIN_SUFFIX.IPA;
            destinationDirectory = targetDir + "/" + configuration + "-iphoneos/";
        }

        mavenProject.getArtifact().setFile(new File(destinationDirectory + "/" + artifactName));
    }
}
