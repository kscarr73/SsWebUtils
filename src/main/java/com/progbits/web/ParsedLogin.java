package com.progbits.web;

/**
 * Used to parse the Authorization header and return the User Name and Password.
 *
 * @author scarr
 */
public class ParsedLogin {

	private String userName;
	private String password;
	private String ipAddress;
	private String base64;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

}
