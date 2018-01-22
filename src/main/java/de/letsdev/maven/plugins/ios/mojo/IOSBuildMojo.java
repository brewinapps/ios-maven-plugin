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

package de.letsdev.maven.plugins.ios.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.letsdev.maven.plugins.ios.ProjectBuilder;
import de.letsdev.maven.plugins.ios.mojo.BaseMojo;

/**
 * 
 * @author let's dev
 * @goal build
 * @phase compile
 */
public class IOSBuildMojo extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
		try {
			ProjectBuilder.build(this.properties, this.mavenProject, this.fileReplacements, this.xcodeBuildParameters, this.xcodeExportOptions, this.stringReplacements);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
}
