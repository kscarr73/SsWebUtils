package com.progbits.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author scarr
 */
public interface WebHandler {
	public String getUrl();
        public String getRole();
	public void handle(String method, RequestUrl requestUrl, HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
