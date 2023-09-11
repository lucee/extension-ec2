package org.lucee.extension.aws.ec2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;

import lucee.commons.io.log.Log;

public class AmazonEC2Client {

	private static Map<String, AmazonEC2Client> pool = new ConcurrentHashMap<String, AmazonEC2Client>();

	// private S3 s3;
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
		AmazonEC2Client client = pool.get(key);
		if (client == null || client.isExpired()) {

			AmazonEC2ClientBuilder builder = AmazonEC2ClientBuilder.standard();
			builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)));
			pool.put(key, client = new AmazonEC2Client(accessKeyId, secretAccessKey, host, region, liveTimeout, log));
		}
		return client.getAmazonEC2();
	}

	private AmazonEC2Client(String accessKeyId, String secretAccessKey, String host, String region, long liveTimeout, Log log) {
		this.accessKeyId = accessKeyId;
		this.secretAccessKey = secretAccessKey;
		this.host = host;
		this.log = log;
		this.created = System.currentTimeMillis();
		client = create();
		this.liveTimeout = liveTimeout;
		this.region = region;
	}

	public AmazonEC2 create() {
		AmazonEC2ClientBuilder builder = AmazonEC2ClientBuilder.standard();
		builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)));

		/*
		 * if (host != null && !host.isEmpty() && !host.equalsIgnoreCase(S3.DEFAULT_HOST)) { // TODO
		 * serviceEndpoint - the service endpoint either with or without the protocol (e.g. //
		 * https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com) builder =
		 * builder.withEndpointConfiguration(new EndpointConfiguration(host, region == null ? "us-east-1" :
		 * S3.toString(region))); } else { if (region != null) { builder =
		 * builder.withRegion(region.getName()); } else { builder =
		 * builder.withRegion(RegionFactory.US_EAST_1.getName()).withForceGlobalBucketAccessEnabled(true);
		 * // The first region to try your request against // If a bucket is in a different region, try
		 * again in the correct region
		 * 
		 * } }
		 */
		return builder.build();
	}

	private boolean isExpired() {
		return (liveTimeout + System.currentTimeMillis()) < created;
	}

	public AmazonEC2 getAmazonEC2() {
		return client;
	}

	private void invalidateAmazonEC2(IllegalStateException ise) {
		if (log != null) log.error("EC2", ise);
		try {
			client = create();
		}
		catch (Exception e) {
			if (log != null) log.error("EC2", e);
			throw new AmazonEC2Exception("failed to invalidate client");
		}
	}

	public void release() {
		// FUTURE remove method
	}

}
