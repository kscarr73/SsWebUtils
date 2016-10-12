/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.progbits.web;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author scarr
 */
public class LoginResponse {
	private boolean handled = false;
	private HttpServletRequest req;

	public LoginResponse() {
	}

	public LoginResponse(boolean handled, HttpServletRequest req) {
		this.handled = handled;
		this.req = req;
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public HttpServletRequest getReq() {
		return req;
	}

	public void setReq(HttpServletRequest req) {
		this.req = req;
	}
}
