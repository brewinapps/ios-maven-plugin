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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

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
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     *
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        Artifact currentArtifact = mavenProject.getArtifact();
        final String targetDir = mavenProject.getBuild().getDirectory();
        String destinationDirectory = null;
        String artifactName = null;

        if (mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString())) {
            artifactName = appName + "." + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP;
            destinationDirectory = targetDir;
//            this.projectHelper.attachArtifact( mavenProject, Utils.PLUGIN_SUFFIX.IOS_FRAMEWORK.toString(), null, new File(destinationDirectory + "/" + artifactName));
        }
        else if(mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IPA.toString())) {
            artifactName = appName + "." + Utils.PLUGIN_SUFFIX.IPA;
            destinationDirectory = targetDir + "/" + configuration + "-iphoneos/";
        }

        currentArtifact.setFile(new File(destinationDirectory + "/" + artifactName));
    }
}
