package com.progbits.web;

import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestChomp {

	@Test
	public void testChomp1() {
		RequestUrl req = new RequestUrl();

		req.setRequestUrl("/mytest/thisvalue");

		if (req.chompUrl("/mytest")) {
			System.out.println("Found: " + req.getRequestUrl());
		} else {
			System.out.println("Not Found: " + req.getRequestUrl());
		}
                
                if (req.chompUrl("/thisvalue")) {
                    	System.out.println("Found: " + req.getRequestUrl());
		} else {
			System.out.println("Not Found: " + req.getRequestUrl());
                }

	}
}
