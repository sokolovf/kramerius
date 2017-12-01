/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.processes.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.processes.starter.ProcessUpdatingChannel;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Utility class for processes.  <br>
 * 
 * Can be used from both sides (in server application and also in started process)
 * @author pavels
 */
public class ProcessUtils {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessUtils.class.getName());
    
    /** Lr servlet name.  This coresponds with web.xml  */
    public static final String LR_SERVLET_NAME="lr";
    
    public static String getApiPoint() {
        
        String applicationURL = KConfiguration.getInstance().getApplicationURL();
        if (!applicationURL.endsWith("/")) {
            applicationURL += '/';
        }
        String api = applicationURL +  "api/v4.6/processes";
        return api;
    }
    
    /**
     * Returns URL to LR servlet
     * @return
     */
    public static String getLrServlet() {
        String lrServlet = KConfiguration.getInstance().getApplicationURL() + '/' + LR_SERVLET_NAME;
        return lrServlet;
    }

    /**
     * Start new process
     * @param processDef Process definition
     * @param nparams nparams parameter 
     * @throws Exception Any Error has been occured
     */
    public static void startProcess(String processDef, String nparams) throws Exception{
        LOGGER.info(" spawn process '"+processDef+"'");
        String base = ProcessUtils.getLrServlet();    
        String url = base + "?action=start&def="+processDef+"&nparams="+nparams+"&token="+System.getProperty(ProcessStarter.TOKEN_KEY);
        byte[] output = new byte[0];
        try {
            output = httpGet(url);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * Start new process
     * @param processDef Process definition
     * @param params Process parameters
     */
    public static void startProcess(String processDef, String[] params) throws UnsupportedEncodingException {
        LOGGER.info(" spawn process '"+processDef+"'");
        String base = ProcessUtils.getLrServlet();    
        String url = base + "?action=start&def="+processDef+"&nparams="+nparams(params)+"&token="+System.getProperty(ProcessStarter.TOKEN_KEY);
        try {
            httpGet(url);
        } catch (Exception e) {
            LOGGER.severe("Error spawning indexer for "+processDef+":"+e);
        }
    }

    /**
     * Close auth token
     * @param processUuid Process uuid
     */
    public static void closeToken(String processUuid) {
        LOGGER.info(" close token for '"+processUuid+"'");
        String base = ProcessUtils.getLrServlet();    
        String url = base + "?action=closeToken&uuid="+processUuid;
        try {
            httpGet(url);
        } catch (Exception e) {
            LOGGER.severe("Error closing token for "+processUuid+":"+e);
        }
    }
    
    /**
     * Helper method creates nparams string from given params parameters
     * @param params Params parameters
     * @return crated string
     */
    public static String nparams(String[] params) throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer(URLEncoder.encode("{", "UTF-8"));
        for (int i = 0; i < params.length; i++) {
            buffer.append(URLEncoder.encode(nparam(params[i]),"UTF-8"));
            if (i < params.length -1) {
                buffer.append(";");
            }
        }
        buffer.append(URLEncoder.encode("}", "UTF-8"));
        return buffer.toString();
    }
    
    /**
     * Returns parametr with escape sequence
     * @param string String to be escaped
     * @return String with escape sequence
     */
    public static String nparam(String string) {
        String[] escapeChars = {"\\",":",";","{","}"};
        for (String toEscape : escapeChars) {
            if (string.contains(toEscape)) {
                string = string.replace(toEscape, "\\"+toEscape);
            }
        }
        return string;
    }


    public static byte[] httpGet(String restURL) throws MalformedURLException, IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            URL url = new URL(restURL);
            URLConnection connection = url.openConnection();
            // authentication token -> identify user
            connection.addRequestProperty("auth-token",System.getProperty(ProcessStarter.AUTH_TOKEN_KEY));
            connection.addRequestProperty(IPAddressUtils.X_IP_FORWARD, System.getProperty(IPAddressUtils.X_IP_FORWARD));
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[1 << 12];
            int read = -1;
            while ((read = inputStream.read(buffer)) > 0) {
                bos.write(buffer,0,read);
            }
            ;

            return buffer;
        } catch (Exception ex) {
            ProcessUpdatingChannel.LOGGER.severe("Problem connecting to REST URL: " + restURL + " - " + ex);
            throw new RuntimeException(ex);
        }
    }
}
