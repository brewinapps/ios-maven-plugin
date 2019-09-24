/**
 * ios-maven-plugin
 * <p/>
 * User: fkoebel
 * Date: 2016-06-23
 * <p/>
 * This code is copyright (c) 2016 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios.mojo;

import de.letsdev.maven.plugins.ios.Utils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.letsdev.maven.plugins.ios.ProjectTester;

/**
 * @author let's dev
 * @goal test
 * @phase test
 */
public class IOSTestMojo extends BaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        super.execute();
        try {
            ProjectTester.test(this.properties, this.mavenProject);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        } finally {
            try {
                super.resetXcodeVersion(Utils.getWorkDirectory(properties, mavenProject, projectName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

