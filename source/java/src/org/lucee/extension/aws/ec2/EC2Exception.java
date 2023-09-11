package org.lucee.extension.aws.ec2;

import java.io.IOException;

public class EC2Exception extends IOException {

	private static final long serialVersionUID = 1785247900379572030L;
	private String ec;
	private long proposedSize;

	public EC2Exception(String message) {
		super(message);
	}

	public EC2Exception(String message, Throwable t) {
		super(message, t);
	}

	public void setErrorCode(String ec) {
		this.ec = ec;
	}

	public String getErrorCode() {
		return ec;
	}

	public void setProposedSize(long proposedSize) {
		this.proposedSize = proposedSize;
	}

	public long getProposedSize() {
		return proposedSize;
	}
}