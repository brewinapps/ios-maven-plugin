package com.brewinapps.ios;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;


/**
 * 
 * @author Brewin' Apps AS
 * @goal package
 * @phase package
 */
public class IOSPackageMojo extends AbstractMojo {
	
	/**
	 * iOS app name
	 * @parameter
	 * 		expression="${ios.appName}"
	 * @required
	 */
	private String appName;
	
	/**
	 * iOS configuration
	 * @parameter
	 * 		expression="${ios.configuration}"
	 * 		default-value="Release"
	 */
	private String configuration;

    /**
     * <p>Classifier to add to the artifact generated.</p>
     *
     * @parameter
     */
    private String classifier;

	/**
	* The maven project.
	* 
	* @parameter expression="${project}"
	* @required
	* @readonly
	*/
	protected MavenProject project;
	
	 /**
     * Maven ProjectHelper.
     *
     * @component
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			final String targetDir = project.getBuild().getDirectory();			
			final String appDir = targetDir + "/" + configuration + "-iphoneos/";			
			final String artifactName = appName + ".zip";
			
			ProcessBuilder pb = new ProcessBuilder(
					"zip",
					"-r", 
					artifactName, 
					appName + ".app.dSYM",
					appName + ".ipa");
			pb.directory(new File(appDir));
			CommandHelper.performCommand(pb);

			if (classifier == null) {
				project.getArtifact().setFile(new File(appDir + "/" + artifactName));
			} else {
				projectHelper.attachArtifact(project, "ios", classifier, new File(appDir + "/" + artifactName));
			}

		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
