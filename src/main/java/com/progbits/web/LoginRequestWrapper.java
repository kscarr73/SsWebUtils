package com.progbits.web;

import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Used for userName and the Roles associated with a user.
 * 
 * @author scarr
 */
public class LoginRequestWrapper extends HttpServletRequestWrapper {
	private final LoginPrincipal user;
	HttpServletRequest realRequest;

	public LoginRequestWrapper(LoginPrincipal user, HttpServletRequest request) {
		super(request);
		this.user = user;
		this.realRequest = request;
	}
	
	public LoginRequestWrapper(Integer loginId, String user, String fullName, List<String> roles, HttpServletRequest request) {
		super(request);
		this.user = new LoginPrincipal(loginId, user, fullName, roles);
		this.realRequest = request;
	}

	@Override
	public boolean isUserInRole(String role) {
		if (user.getRoles() == null) {
			return this.realRequest.isUserInRole(role);
		}
		return user.getRoles().contains(role);
	}

	@Override
	public Principal getUserPrincipal() {
		if (this.user == null) {
			return realRequest.getUserPrincipal();
		}

		// make an anonymous implementation to just return our user  
		return user;
	}
}
