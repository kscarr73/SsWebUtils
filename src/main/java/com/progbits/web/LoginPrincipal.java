/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.progbits.web;

import java.security.Principal;
import java.util.List;

/**
 *
 * @author scarr
 */
public class LoginPrincipal implements Principal {
	private final Integer loginId;
	private final String userName;
	private final String fullName;
	private final List<String> roles;

	public LoginPrincipal(Integer loginId, String userName, String fullName, List<String> roles) {
		this.loginId = loginId;
		this.userName = userName;
		this.fullName = fullName;
		this.roles = roles;
	}
	
	public String getName() {
		return userName;
	}

	public Integer getLoginId() {
		return loginId;
	}

	public String getFullName() {
		return fullName;
	}

	public List<String> getRoles() {
		return roles;
	}
	
}
