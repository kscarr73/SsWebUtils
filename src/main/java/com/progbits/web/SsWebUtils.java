package com.progbits.web;

import com.progbits.api.model.ApiObject;
import com.progbits.db.SsDbUtils;
import com.progbits.db.Tuple;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class SsWebUtils {

    private static final Logger log = LoggerFactory.getLogger(SsWebUtils.class);
    private static final DateTimeFormatter _dateHeader = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    .withZoneUTC().withLocale(Locale.US);
    
    /**
     * Check req and see if User is already logged in.
     *
     * @param req HttpServletReq to parse information
     * @param options LoginOptions to pull SQL
     * @return TRUE/FALSE If Login was performed or found. False if Login needs
     * to be performed
     *
     * @throws Exception
     */
    public static LoginResponse handleLogin(HttpServletRequest req, LoginOptions options) throws Exception {
        LoginResponse resp = new LoginResponse();

        // Check if User Already Logged In
        if (req.getUserPrincipal() == null) {
            HttpSession sess = req.getSession();

            LoginPrincipal login = (LoginPrincipal) sess.getAttribute("LoginPrincipal");

            if (login != null) {
                resp.setHandled(true);

                req.setAttribute("loginId", login.getLoginId());
                req.setAttribute("fullName", login.getFullName());

                req.setAttribute("LoginPrincipal", login);

                resp.setReq(new LoginRequestWrapper(login, req));
            } else {
                // We have exhausted everything else, now try login
                // Check to see if Parameters for Login Exist
                String strUserName = req.getParameter("userName");
                String strPassword = req.getParameter("password");

                if (strUserName != null && strPassword != null) {
                    LoginPrincipal rLogin = returnLogin(options, strUserName, strPassword);

                    sess.setAttribute("LoginPrincipal", rLogin);

                    req.setAttribute("LoginPrincipal", login);
                    req.setAttribute("loginId", rLogin.getLoginId());
                    req.setAttribute("fullName", rLogin.getFullName());

                    resp.setHandled(true);
                    resp.setReq(new LoginRequestWrapper(rLogin, req));
                } else {
                    resp.setHandled(false);
                }
            }
        } else {
            resp.setHandled(true);
            resp.setReq(req);
        }

        return resp;
    }

    public static LoginPrincipal returnLogin(LoginOptions options, String userName, String password) throws Exception {
        Connection conn = null;
        LoginPrincipal retPrinc = null;
        Tuple<PreparedStatement, ResultSet> rs = null;
        List<String> roles = new ArrayList<String>();

        List<Object> args = new ArrayList<Object>();

        args.add(userName);
        args.add(password);

        try {
            conn = options.getDatasource().getConnection();

            Integer iLoginId = SsDbUtils.queryForInt(conn, options.getLoginIdSql(), args.toArray());

            if (iLoginId == null) {
                throw new Exception("UserName or Password not correct");
            }

            String sFullName = SsDbUtils.queryForString(conn, options.getFullNameSql(), args.toArray());

            rs = SsDbUtils.returnResultset(conn, options.getRolesSql(), new Object[]{iLoginId});

            while (rs.getSecond().next()) {
                roles.add(rs.getSecond().getString(1));
            }

            retPrinc = new LoginPrincipal(iLoginId, userName, sFullName, roles);
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (rs != null) {
                SsDbUtils.closeResultSet(rs.getSecond());
                SsDbUtils.closePreparedStatement(rs.getFirst());
            }
            SsDbUtils.closeConnection(conn);
        }

        return retPrinc;
    }

    public static void populateBean(Map<String, String[]> req, Object bean, WebFieldOptions options) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();

        BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);

        PropertyDescriptor[] props = info.getPropertyDescriptors();

        for (Map.Entry<String, String[]> entry : req.entrySet()) {
            String strName = entry.getKey();

            for (PropertyDescriptor prop : props) {
                if (strName.equals(prop.getName())) {
                    String[] sParam = entry.getValue();

                    if (sParam != null) {
                        String sValue = sParam[0];

                        Method mthd = prop.getWriteMethod();

                        if (mthd != null) {
                            String fldType = mthd.getParameterTypes()[0].toString();

                            if (fldType.contains("BigDecimal")) {
                                mthd.invoke(bean, new BigDecimal(sValue.replace(",", "")));
                            } else if (fldType.contains("Integer") || fldType.equals("int")) {
                                mthd.invoke(bean, new Integer(sValue.replace(",", "")));
                            } else if (fldType.contains("Double") || fldType.equals("double")) {
                                mthd.invoke(bean, new Double(sValue.replace(",", "")));
                            } else if (fldType.contains("Long") || fldType.equals("long")) {
                                mthd.invoke(bean, new Long(sValue.replace(",", "")));
                            } else if (fldType.contains("Float") || fldType.equals("float")) {
                                mthd.invoke(bean, new Float(sValue.replace(",", "")));
                            } else if (fldType.contains("DateTime")) {
                                mthd.invoke(bean, options.getDateTimeFormatter().parseDateTime(sValue));
                            } else {
                                mthd.invoke(bean, sValue);
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Returns the full Post Data in a String
     *
     * @param req Request object to parse the input from
     *
     * @return String of the Post Data sent
     *
     * @throws Exception
     */
    public static String getReqString(HttpServletRequest req) throws Exception {
        StringBuilder retStr = new StringBuilder();
        InputStreamReader isr = null;

        ContentType ct = new ContentType(req.getContentType());

        if ("gzip".equals(req.getHeader("Content-Encoding"))) {
            isr = new InputStreamReader(new GZIPInputStream(req.getInputStream()), ct.getCharCode());
        } else {
            isr = new InputStreamReader(req.getInputStream(), ct.getCharCode());
        }

        CharBuffer cBuff = CharBuffer.allocate(2048);

        while ((isr.read(cBuff)) > -1) {
            cBuff.flip();
            retStr.append(cBuff.toString());
            cBuff.clear();
        }

        return retStr.toString();
    }

    /**
     * Returns the IP Address of the system even if behind a Proxy.
     *
     * It checks the X-Forwarded-For Header to see if it is set as well as the
     * getRemoteAddr call.
     *
     * @param req Request object to pull IP Address from.
     *
     * @return The IP Address for the current request
     */
    public static String returnIpAddress(HttpServletRequest req) {
        String ipAddress = req.getRemoteAddr();

        String xforwardedFor = req.getHeader("X-Forwarded-For");

        if (xforwardedFor != null) {
            // xforwardedFor has style of "client, proxy1, proxy2"
            String[] splitFor = xforwardedFor.split(",");

            // Get client ip address
            ipAddress = splitFor[0];
        }

        return ipAddress;
    }

    /**
     * Used to pull BASIC Authorization Header and Parse
     *
     * @param req Http Request Object to pull information from.
     *
     * @return LoginUser object with IP Address and Username Password
     */
    public static ParsedLogin returnAuthUser(HttpServletRequest req) {
        ParsedLogin lu = null;

        String authHdr = req.getHeader("Authorization");

        if (authHdr != null) {
            lu = new ParsedLogin();

            lu.setIpAddress(returnIpAddress(req));

            String[] sAuth = authHdr.split(" ");

            if (sAuth.length == 2) {
                lu.setBase64(sAuth[1]);
                String sDecoded = new String(Base64.decodeBase64(sAuth[1]));

                String[] splitDec = sDecoded.split(":");

                if (splitDec.length == 2) {
                    lu.setUserName(splitDec[0]);
                    lu.setPassword(splitDec[1]);
                } else if (splitDec.length == 1) {
                    lu.setUserName(splitDec[0]);
                }
            }
        }

        return lu;
    }

    /**
     * Return a Map of the Headers in a Request
     *
     * @param req The Request to pull the Headers from
     * @return Map of the Headers
     */
    public static Map<String, String> getReqHeaders(HttpServletRequest req) {
        Map<String, String> retMap = new HashMap<>();

        Enumeration<String> eHdrs = req.getHeaderNames();

        while (eHdrs.hasMoreElements()) {
            String sKey = eHdrs.nextElement();
            String sValue = req.getHeader(sKey);

            retMap.put(sKey, sValue);
        }

        return retMap;
    }

    public static void populateApiObject(Map<String, String[]> req, Map<String, String> fields,
            ApiObject obj, WebFieldOptions options) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();

        for (Map.Entry<String, String[]> entry : req.entrySet()) {
            String strName = entry.getKey();
            String fldType = fields.get(strName);

            if (fldType != null) {
                String[] sParam = entry.getValue();

                if (sParam != null) {
                    String sValue = sParam[0];

                    if (fldType.contains("BigDecimal")) {
                        obj.setDecimal(strName, new BigDecimal(sValue.replace(",", "")));
                    } else if (fldType.contains("Integer") || fldType.equals("int")) {
                        obj.setInteger(strName, Integer.parseInt(sValue.replace(",", "")));
                    } else if (fldType.contains("Double") || fldType.equals("double")) {
                        obj.setDouble(strName, Double.parseDouble(sValue.replace(",", "")));
                    } else if (fldType.contains("Long") || fldType.equals("long")) {
                        obj.setLong(strName, Long.parseLong(sValue.replace(",", "")));
                    } else if (fldType.contains("DateTime")) {
                        obj.setDateTime(strName, options.getDateTimeFormatter().parseDateTime(sValue));
                    } else {
                        obj.setString(strName, sValue);
                    }

                }
            }

        }
    }

    public static Integer returnIntegerParam(HttpServletRequest req, String param) {
        String strTemp = req.getParameter(param);
        Integer iRet = null;

        try {
            iRet = Integer.parseInt(strTemp);
        } catch (NumberFormatException nfe) {

        }

        return iRet;
    }

    /**
     * Returns true if the role is accepted for this handler.
     *
     * @param handler
     * @param userRoles
     * @return
     */
    public static boolean processRole(WebHandler handler, String userRoles) {
        if ("ADMIN".equals(handler.getRole())) {
            if (userRoles == null) {
                return false;
            }
        }

        if (userRoles != null) {
            if (handler.getRole() != null) {
                return userRoles.contains(handler.getRole());
            }
        }

        return true;
    }

    public static ApiObject parseDataTableParams(HttpServletRequest req)
            throws Exception {
        ApiObject objRet = new ApiObject();

        Enumeration<String> enumParams = req.getParameterNames();

        while (enumParams.hasMoreElements()) {
            String strParam = enumParams.nextElement();

            String[] sLevels = strParam.replace("]", "").split("\\[");

            if (sLevels.length > 1) {
                if (isNumeric(sLevels[1])) {
                    Integer iCount = Integer.parseInt(sLevels[1]);

                    if (objRet.getList(sLevels[0]) == null) {
                        objRet.createList(sLevels[0]);
                    }

                    Integer iMakeUp = (iCount + 1)
                            - objRet.getList(sLevels[0]).size();

                    if (iMakeUp > 0) {
                        for (int x = 0; x < iMakeUp; x++) {
                            objRet.getList(sLevels[0]).add(new ApiObject());
                        }
                    }

                    if (sLevels.length > 4) {
                        // No clue
                    } else if (sLevels.length == 4) {
                        objRet.getList(sLevels[0])
                                .get(iCount)
                                .setObject(sLevels[2], new ApiObject());
                        objRet.getList(sLevels[0]).get(iCount)
                                .getObject(sLevels[2])
                                .setString(sLevels[3],
                                        req.getParameter(strParam));
                    } else if (sLevels.length == 3) {
                        objRet.getList(sLevels[0]).get(iCount)
                                .setString(sLevels[2],
                                        req.getParameter(strParam));
                    }
                } else {
                    if (objRet.getObject(sLevels[0]) == null) {
                        objRet.setObject(sLevels[0], new ApiObject());
                    }

                    objRet.getObject(sLevels[0]).setString(sLevels[1],
                            req.getParameter(strParam));
                }
            } else if ("start".equals(sLevels[0]) || "length".equals(sLevels[0]) || "draw".equals(sLevels[0])) {
                objRet.setInteger(sLevels[0], Integer.parseInt(req.getParameter(strParam)));
            } else {
                objRet.setString(sLevels[0], req.getParameter(strParam));
            }
        }

        return objRet;
    }

    public static boolean isNumeric(String inputData) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(inputData, pos);
        return inputData.length() == pos.getIndex();
    }

    public static String pullPath(String path, String type) {
        String strRet = path;

        int iLoc = strRet.indexOf(type);

        if (iLoc > -1) {
            strRet = strRet.substring(iLoc);
        }

        return strRet;
    }

    public static void sendFile(ServletSetup setup, String path, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String fileName = SsWebUtils.pullPath(req.getRequestURI(), path);
        fileName = URLDecoder.decode(fileName, "UTF-8");
        
        URL url;
        
        if (setup.getLoader() != null) {
            url = setup.getLoader().getResource(fileName);
        } else if (setup.getBasePath() != null) {
            url = new File(setup.getBasePath() + fileName).toURI().toURL();
        } else {
            throw new Exception("BasePath or Loader MUST be set");
        }
        
        URLConnection conn = url.openConnection();
        
        resp.setContentType(setup.getContext().getMimeType(fileName));
        resp.setContentLength(conn.getContentLength());
        resp.setHeader("Cache-Control", "max-age=" + setup.getCacheTime());
        
        if (conn.getLastModified() > 0L) {
            resp.setHeader("Last-Modified", _dateHeader.print(conn.getLastModified()));
        }

        if ("HEAD".equalsIgnoreCase(req.getMethod())) {
            // HEAD does not need content
        } else {
            SsWebUtils.writeFile(conn.getInputStream(), resp.getOutputStream());
        }
    }

    public static void writeFile(InputStream is, OutputStream out) throws Exception {
        byte[] buffer = new byte[4096]; // tweaking this number may increase performance  
        int len;

        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        out.flush();
    }

}
