/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * Copyright 2013-2015 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.rtr.rmbt.mapServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.naming.NamingException;

import com.google.common.collect.Sets;
import org.json.JSONException;
import org.json.JSONObject;

import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import com.google.gson.Gson;

import at.rtr.rmbt.shared.ResourceManager;
import at.rtr.rmbt.util.capability.Capabilities;

public class ServerResource extends org.restlet.resource.ServerResource
{
    protected Connection conn;
    protected ResourceBundle labels;
    protected ResourceBundle settings;
    protected Capabilities capabilities = new Capabilities();
    
    @Override
    public void doInit() throws ResourceException
    {
        super.doInit();
        
        settings = ResourceManager.getCfgBundle();
        // Set default Language for System
        Locale.setDefault(new Locale(settings.getString("RMBT_DEFAULT_LANGUAGE")));
        labels = ResourceManager.getSysMsgBundle();
        
        try {
	        if (getQuery().getNames().contains("capabilities")) {
	        	capabilities = new Gson().fromJson(getQuery().getValues("capabilities"), Capabilities.class);
	        }
        } catch (final Exception e) {
        	e.printStackTrace();
        }
        
        // Get DB-Connection
        try
        {
            conn = DbConnection.getConnection();
        }
        catch (final NamingException e)
        {
            e.printStackTrace();
        }
        catch (final SQLException e)
        {
            System.out.println(labels.getString("ERROR_DB_CONNECTION_FAILED"));
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doRelease() throws ResourceException
    {
        super.doRelease();
        try
        {
            conn.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public void readCapabilities(final JSONObject request) throws JSONException {
    	if (request != null) {
    		if (request.has("capabilities")) {
        		capabilities = new Gson().fromJson(request.get("capabilities").toString(), Capabilities.class);	
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
    protected void addAllowOrigin()
    {
        getResponse().setAccessControlAllowCredentials(false);
        getResponse().setAccessControlAllowMethods(Sets.newHashSet(Method.GET, Method.POST, Method.OPTIONS));
        getResponse().setAccessControlMaxAge(60);
        getResponse().setAccessControlAllowOrigin("*");
        getResponse().setAccessControlAllowHeaders(Sets.newHashSet("Content-Type"));
    }
    
    @Options
    public void doOptions(final Representation entity)
    {
        addAllowOrigin();
    }


    protected String getSetting(String key, String lang)
    {
        if (conn == null)
            return null;


        try (final PreparedStatement st = conn.prepareStatement(
                "SELECT value"
                        + " FROM settings"
                        + " WHERE key=? AND (lang IS NULL OR lang = ?)"
                        + " ORDER BY lang NULLS LAST LIMIT 1");)
        {

            st.setString(1, key);
            st.setString(2, lang);

            try (final ResultSet rs = st.executeQuery();)
            {

                if (rs != null && rs.next())
                    return rs.getString("value");
            }
            return null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
