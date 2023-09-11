package org.lucee.extension.aws.ec2;

import java.util.concurrent.ConcurrentHashMap;

import org.lucee.extension.aws.ec2.util.XMLUtil;

import com.amazonaws.services.ec2.AmazonEC2;

import lucee.commons.io.log.Log;
import lucee.loader.util.Util;

public class EC2 {

	public static final short URI_STYLE_VIRTUAL_HOST = 1;
	public static final short URI_STYLE_PATH = 2;
	public static final short URI_STYLE_S3 = 4;
	public static final short URI_STYLE_ARN = 8;

	static {
		XMLUtil.validateDocumentBuilderFactory();
	}

	public static final String DEFAULT_HOST = "s3.amazonaws.com";
	public static final long DEFAULT_LIVE_TIMEOUT = 600000L;
	public static final String GOOGLE = ".googleapis.com";

	public static final String[] PROVIDERS = new String[] { ".amazonaws.com", ".wasabisys.com", ".backblazeb2.com", ".digitaloceanspaces.com", ".dream.io", GOOGLE };

	private static final ConcurrentHashMap<String, Object> tokens = new ConcurrentHashMap<String, Object>();

	private final String host;
	private final String secretAccessKey;
	private final String accessKeyId;

	private Log log;

	private final long liveTimeout;
	private String region;

	/**
	 * 
	 * @param props S3 Properties
	 * @param timeout
	 * @param defaultRegion region used to create new buckets in case no bucket is defined
	 * @param cacheRegions
	 * @param log
	 * @throws S3Exception
	 */
	public EC2(String accessKeyId, String secretAccessKey, String host, String region, long liveTimeout, Log log) {
		this.host = host;
		this.secretAccessKey = secretAccessKey;
		this.accessKeyId = accessKeyId;
		this.liveTimeout = liveTimeout;
		this.region = region;
		this.log = log;
	}

	private AmazonEC2 getAmazonEC2Client() throws EC2Exception {
		if (Util.isEmpty(accessKeyId) || Util.isEmpty(secretAccessKey)) throw new EC2Exception("Could not found an accessKeyId/secretAccessKey");

		return AmazonEC2Client.get(accessKeyId, secretAccessKey, host, region, liveTimeout, log);
	}

	public static void main(String[] args) throws EC2Exception {
		EC2 ec2 = new EC2(null, null, null, null, DEFAULT_LIVE_TIMEOUT, null);
		AmazonEC2 client = ec2.getAmazonEC2Client();
		client.describeInstances();
	}
}