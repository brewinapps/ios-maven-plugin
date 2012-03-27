package com.brewinapps.maven.plugins.ios;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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


/**
 * 
 * @author Brewin' Apps AS
 */
public class ProjectDeployer {
	
	/**
	 * 
	 * @param apiToken
	 * @param teamToken
	 * @throws AutopilotException
	 */
	public static void deploy(final String apiToken, final String teamToken, 
			final List<String> distributionLists, final String notes, final boolean notify, 
			final String ipa, final boolean replace) 
	throws AutopilotException {
		
		try {
			System.out.println("Deploying to TestFlight...");
			
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			
			HttpPost post = new HttpPost("http://testflightapp.com/api/builds.xml");
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			// Add parameters (including IPA)
			entity.addPart("file", 
					new FileBody(new File(ipa), "application/zip"));
			entity.addPart("api_token", 
					new StringBody(apiToken, "text/plain", Charset.forName("UTF-8")));
			entity.addPart("team_token", 
					new StringBody(teamToken, "text/plain", Charset.forName("UTF-8")));
			entity.addPart("notes", 
					new StringBody(notes, "text/plain", Charset.forName("UTF-8")));
			
			if (distributionLists.size() != 0) {
				entity.addPart("distribution_lists", 
						new StringBody(StringUtils.join(distributionLists.toArray(), ","), 
								"text/plain", Charset.forName("UTF-8")));
			}
			
			entity.addPart("replace",
					new StringBody(replace ? "True" : "False", "text/plain", Charset.forName("UTF-8")));
			
			entity.addPart("notify", 
					new StringBody(notify ? "True" : "False", "text/plain", Charset.forName("UTF-8")));
			
			post.setEntity(entity);
									
			// Run the request
			HttpResponse response = client.execute(post);
			HttpEntity responseEntity = response.getEntity();
			
			System.out.println(response.getStatusLine());
			if (responseEntity != null) {
				System.out.println(EntityUtils.toString(responseEntity));
			}
			
			// Shutdown the HttpClient
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			throw new AutopilotException("An error occured while deploying build to TestFlight");
		}
	}
}
