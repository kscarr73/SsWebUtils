package com.progbits.web;

import javax.servlet.ServletContext;

/**
 *
 * @author scarr
 */
public class ServletSetup {
    private Class _loader;
    private ServletContext _context = null;
    private String _basePath = null;
    private Integer _cacheTime;

    public Integer getCacheTime() {
        return _cacheTime;
    }

    public void setCacheTime(Integer cacheTime) {
        this._cacheTime = cacheTime;
    }

    public ServletContext getContext() {
        return _context;
    }

    public void setContext(ServletContext context) {
        this._context = context;
    }

    public Class getLoader() {
        return _loader;
    }

    public void setLoader(Class loader) {
        this._loader = loader;
    }

    public String getBasePath() {
        return _basePath;
    }

    public void setBasePath(String basePath) {
        this._basePath = basePath;
    }
    
}
