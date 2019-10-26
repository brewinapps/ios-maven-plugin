/**
 * Maven iOS Plugin
 *
 * User: sbott
 * Date: 19.07.2012
 * Time: 19:54:44
 *
 * This code is copyright (c) 2012 let's dev.
 * URL: https://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios.mojo;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;

import java.io.*;

import de.letsdev.maven.plugins.ios.Utils;


/**
 * @author let's dev
 * @goal package
 * @phase package
 */
public class IOSPackageMojo extends BaseMojo {

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        Artifact currentArtifact = mavenProject.getArtifact();
        final String targetDir = mavenProject.getBuild().getDirectory();
        String destinationDirectory;
        String artifactName;

        String projectVersion = mavenProject.getVersion();

        String adjustedVersion = Utils.getAdjustedVersion(mavenProject, this.properties);
        String appNameOfExecution = Utils.getAppName(this.properties);
        String configurationOfExecution = Utils.getConfiguration(this.properties);
        String classifierOfExecution = Utils.getClassifier(this.properties);
        String buildIdOfExecution = Utils.getBuildId(this.properties);
        boolean isMacOsFrameworkBuildOfExecution = Utils.isMacOSFramework(this.properties);
        boolean isIosFrameworkBuildOfExecution = Utils.isiOSFramework(mavenProject, this.properties);

        String classifierString = (classifierOfExecution != null? "-" + classifierOfExecution + "-" : "-");

        String artifactType = (currentArtifact.getType() == null || "pom".equals(currentArtifact.getType() ) ? Utils.PLUGIN_PACKAGING.IPA.toString() : currentArtifact.getType());
        if (isIosFrameworkBuildOfExecution || isMacOsFrameworkBuildOfExecution || artifactType.equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString())) {
            artifactType = Utils.PLUGIN_PACKAGING.FRAMEWORK_ZIP.toString();
        }

        if (buildIdOfExecution != null) {
            adjustedVersion += "-b" + buildIdOfExecution;
        }

        if (Utils.isiOSFramework(mavenProject, isIosFrameworkBuildOfExecution) || isMacOsFrameworkBuildOfExecution) {
            artifactName = appNameOfExecution + "." + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP;
            destinationDirectory = targetDir;
//            this.projectHelper.attachArtifact( mavenProject, Utils.PLUGIN_SUFFIX.IOS_FRAMEWORK.toString(), null, new File(destinationDirectory + File.separator + artifactName));
        }
        else {
            mavenProject.setPackaging(Utils.PLUGIN_PACKAGING.IPA.toString());
            artifactName = appNameOfExecution + "-" + adjustedVersion + "." + Utils.PLUGIN_SUFFIX.IPA;
            destinationDirectory = targetDir + File.separator + configurationOfExecution + "-iphoneos" + File.separator;
        }

        File destinationFile = new File(destinationDirectory + File.separator + artifactName);
        File artifactFile =  new File(targetDir + File.separator + appNameOfExecution + classifierString + projectVersion + "." + (Utils.isiOSFramework(mavenProject, isIosFrameworkBuildOfExecution) ? Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP : Utils.PLUGIN_SUFFIX.IPA));

        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(destinationFile);
            out = new FileOutputStream(artifactFile, true);
            IOUtils.copy(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        currentArtifact.setFile(artifactFile);
        projectHelper.attachArtifact(mavenProject, artifactType, classifierOfExecution, artifactFile);
    }
}
