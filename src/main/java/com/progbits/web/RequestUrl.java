package com.progbits.web;

/**
 *
 * @author scarr
 */
public class RequestUrl {

    private boolean bFirst = true;
    private String rootUrl;
    private String requestUrl;

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public boolean chompUrl(String testUrl) {
        if (requestUrl.startsWith(testUrl)) {
            if (bFirst) {
                rootUrl = testUrl;
                bFirst = false;
            }

            requestUrl = requestUrl.substring(testUrl.length());
            
            return true;
        } else {
            return false;
        }
    }
}
