/**
 * Maven iOS Plugin
 * <p>
 * User: sbott
 * Date: 19.07.2012
 * Time: 19:54:44
 * <p>
 * This code is copyright (c) 2012 let's dev.
 * URL: https://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import de.letsdev.maven.plugins.ios.ProjectDeployer;
import de.letsdev.maven.plugins.ios.Utils;

/**
 * @author let's dev
 * @goal deploy
 * @phase deploy
 */
public class IOSDeployMojo extends BaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        super.execute();
        try {
            ProjectDeployer.deploy(this.properties, this.mavenProject);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        } finally {
            try {
                super.resetXcodeVersion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
