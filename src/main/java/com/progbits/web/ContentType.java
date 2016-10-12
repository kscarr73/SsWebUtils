/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.progbits.web;

import java.nio.charset.Charset;

/**
 *
 * @author scarr
 */
public class ContentType {

    private String contentType;
    private String charSet;

    public ContentType(String reqContent) {
        if (reqContent != null) {
            String[] splitContent = reqContent.split(";");

            if (splitContent.length > 1) {
                contentType = splitContent[0];

                String[] sEquals = splitContent[1].split("=");

                if (sEquals.length > 1) {
                    charSet = sEquals[1];
                }
            } else {
                contentType = splitContent[0];
                charSet = "UTF-8";
            }

            if (contentType != null) {
                contentType = contentType.trim();
            }

            if (charSet != null) {
                charSet = charSet.trim();
            }
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCharSet() {
        return charSet;
    }

    public Charset getCharCode() {
        return Charset.forName(charSet);
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

}
