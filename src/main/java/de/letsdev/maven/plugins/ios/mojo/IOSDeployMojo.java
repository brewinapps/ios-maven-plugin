/**
 * Maven iOS Plugin
 * <p>
 * User: sbott
 * Date: 19.07.2012
 * Time: 19:54:44
 * <p>
 * This code is copyright (c) 2012 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios.mojo;

import de.letsdev.maven.plugins.ios.mojo.container.FileReplacement;
import de.letsdev.maven.plugins.ios.mojo.container.StringReplacementConfig;
import de.letsdev.maven.plugins.ios.mojo.container.XcodeExportOptions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.letsdev.maven.plugins.ios.ProjectDeployer;
import de.letsdev.maven.plugins.ios.Utils;

/**
 *
 * @author let's dev
 * @goal deploy
 * @phase deploy
 */
public class IOSDeployMojo extends BaseMojo {


    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            ProjectDeployer.deploy(properties, this.mavenProject);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        }finally {
            try {
                super.resetXcodeVersion(Utils.getWorkDirectory(properties, mavenProject, projectName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
