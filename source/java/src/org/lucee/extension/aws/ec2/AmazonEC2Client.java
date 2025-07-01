package org.lucee.extension.aws.ec2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

import lucee.commons.io.log.Log;
import lucee.loader.util.Util;

public class AmazonEC2Client {

	// EC2 default host (though typically not needed for standard AWS)
	private static final String DEFAULT_HOST = "ec2.amazonaws.com";

	private static Map<String, AmazonEC2Client> pool = new ConcurrentHashMap<String, AmazonEC2Client>();

	private String bucketName;
	private AmazonEC2 client;
	private Log log;
	private long created;
	private String accessKeyId;
	private String secretAccessKey;
	private String host;
	private long liveTimeout;
	private String region;

	public static AmazonEC2 get(String accessKeyId, String secretAccessKey, String host, String region, long liveTimeout, Log log) {
		String key = accessKeyId + ":" + secretAccessKey + ":" + host + ":" + region;

		if (log != null) {
			log.debug("EC2Client", "Requesting EC2 client for region: " + region + ", host: " + host);
		}

		AmazonEC2Client client = pool.get(key);
		if (client == null || client.isExpired()) {
			if (log != null) {
				if (client == null) {
					log.info("EC2Client", "Creating new EC2 client for region: " + region);
				}
				else {
					log.info("EC2Client", "Existing EC2 client expired, creating new one for region: " + region);
				}
			}
			pool.put(key, client = new AmazonEC2Client(accessKeyId, secretAccessKey, host, region, liveTimeout, log));
		}
		else {
			if (log != null) {
				log.debug("EC2Client", "Reusing existing EC2 client for region: " + region);
			}
		}

		return client.getAmazonEC2();
	}

	private AmazonEC2Client(String accessKeyId, String secretAccessKey, String host, String region, long liveTimeout, Log log) {
		this.accessKeyId = accessKeyId;
		this.secretAccessKey = secretAccessKey;
		this.host = host;
		this.region = region;
		this.log = log;
		this.liveTimeout = liveTimeout;
		this.created = System.currentTimeMillis();

		if (log != null) {
			log.info("EC2Client", "Initializing EC2 client with region: " + region + ", host: " + host + ", liveTimeout: " + liveTimeout + "ms");
		}

		client = create();
		if (log != null) {
			log.info("EC2Client", "Successfully created EC2 client");
		}
	}

	public AmazonEC2 create() {
		AmazonEC2ClientBuilder builder = AmazonEC2ClientBuilder.standard();
		builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)));

		// Set region with fallback
		String effectiveRegion = !Util.isEmpty(region, true) ? region.trim() : "us-east-1";

		if (log != null) {
			log.debug("EC2Client", "Using region: " + effectiveRegion + " (original: " + region + ")");
		}

		// Handle custom host/endpoint or standard AWS regions
		if (!Util.isEmpty(host, true) && !host.trim().equalsIgnoreCase(DEFAULT_HOST)) {
			String effectiveHost = host.trim();

			// Ensure the host has a protocol
			if (!effectiveHost.startsWith("http://") && !effectiveHost.startsWith("https://")) {
				effectiveHost = "https://" + effectiveHost;
			}

			if (log != null) {
				log.info("EC2Client", "Using custom endpoint: " + effectiveHost + " with region: " + effectiveRegion);
			}

			builder.withEndpointConfiguration(new EndpointConfiguration(effectiveHost, effectiveRegion));
		}
		else {
			if (log != null) {
				log.debug("EC2Client", "Using standard AWS endpoint for region: " + effectiveRegion);
			}

			builder.withRegion(effectiveRegion);
		}

		return builder.build();
	}

	private boolean isExpired() {
		return (liveTimeout + System.currentTimeMillis()) < created;
	}

	public AmazonEC2 getAmazonEC2() {
		return client;
	}

	public void release() {
		if (log != null) {
			log.debug("EC2Client", "Releasing EC2 client resources");
		}
		// FUTURE remove method
	}
}