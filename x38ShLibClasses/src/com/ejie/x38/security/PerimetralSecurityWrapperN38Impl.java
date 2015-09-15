/*
* Copyright 2011 E.J.I.E., S.A.
*
* Licencia con arreglo a la EUPL, Versión 1.1 exclusivamente (la «Licencia»);
* Solo podrá usarse esta obra si se respeta la Licencia.
* Puede obtenerse una copia de la Licencia en
*
* http://ec.europa.eu/idabc/eupl.html
*
* Salvo cuando lo exija la legislación aplicable o se acuerde por escrito,
* el programa distribuido con arreglo a la Licencia se distribuye «TAL CUAL»,
* SIN GARANTÍAS NI CONDICIONES DE NINGÚN TIPO, ni expresas ni implícitas.
* Véase la Licencia en el idioma concreto que rige los permisos y limitaciones
* que establece la Licencia.
*/
package com.ejie.x38.security;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import n38a.exe.N38APISesion;
import n38c.exe.N38API;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Document;

import com.ejie.x38.util.StaticsContainer;

/**
 * 
 * @author UDA
 *
 */
public class PerimetralSecurityWrapperN38Impl implements
		PerimetralSecurityWrapper {

	private static final Logger logger = LoggerFactory
			.getLogger(PerimetralSecurityWrapperN38Impl.class);
	
	private Long xlnetCachingPeriod;

	private N38API validateSession(HttpServletRequest httpRequest) {
		logger.info( "Refreshing XLNET session!");
		
		N38API n38Api = XlnetCore.getN38API(httpRequest);
		
		Document xmlSecurity = XlnetCore.getN38ItemSesion(n38Api);

		if (XlnetCore.isXlnetSessionContainingErrors(xmlSecurity)
				|| XlnetCore.isXlnetSessionContainingWarnings(xmlSecurity)) {
			logger.error("XLNET session is invalid. Proceeding to clean the Security Context Holder.");
			xmlSecurity = null;
			return null;
		}
				
		return n38Api;
	}

	@Override
	public String getUserConnectedUserName(HttpServletRequest httpRequest) {
		String result = null;

		if(isXlnetCachingActive(httpRequest) && httpRequest.getSession(false)!=null && httpRequest.getSession(false).getAttribute("UserName")!=null){
			result = (String) httpRequest.getSession(false).getAttribute("UserName");
			logger.debug("Obtained the userName of the request: " + result);
			logger.info("The incoming user \""+result+"\" is already authenticated in the security system");
		}else{
			if (httpRequest.getSession(false).getAttribute("Position") == null && httpRequest.getSession(false).getAttribute("UidSession") == null && httpRequest.getSession(false).getAttribute("UserProfiles") == null){
				if ((validateSession(httpRequest)== null)){
					SecurityContextHolder.clearContext();
					throw new AuthenticationCredentialsNotFoundException("The XLNET session is invalid or the Xlnet's user isn't logger");
				}
			}
			
			N38API n38Api = XlnetCore.getN38API(httpRequest);
			if(n38Api!=null){
				result = XlnetCore.getLogin(n38Api);
			}
			
			if (result != null){
				logger.trace("Connected User's Name is: "+result);
			}
			else{
				logger.warn("Connected User's Name is null!");
				return null;
			}
			//Save the userName value
			if(httpRequest.getSession(false)!=null){
				httpRequest.getSession(false).setAttribute("UserName", result);
			}
		}
		return result;
		
	}
	
	@Override
	public String getUserPosition(HttpServletRequest httpRequest) {
		String result = null;

		if(isXlnetCachingActive(httpRequest) && httpRequest.getSession(false)!=null && httpRequest.getSession(false).getAttribute("Position")!=null ){
			result = (String) httpRequest.getSession(false).getAttribute("Position");
		}else{
			if (httpRequest.getSession(false).getAttribute("UserName") == null && httpRequest.getSession(false).getAttribute("UidSession") == null && httpRequest.getSession(false).getAttribute("UserProfiles") == null){
				if ((validateSession(httpRequest)== null)){
					logger.error("The XLNET session is invalid or the Xlnet's user isn't logger. Proceeding to clean the Security Context Holder.");
					SecurityContextHolder.clearContext();
					throw new AuthenticationCredentialsNotFoundException("The XLNET session is invalid or the Xlnet's user isn't logger");
				}
			}
			
			N38API n38Api = XlnetCore.getN38API(httpRequest);
			if(n38Api!=null){
				result = XlnetCore.getPuesto(n38Api);
			}
			
			if (result != null){
				logger.trace("Connected User's Position is: "+result);
			}
			else{
				logger.warn("Connected User's Position is null!");
				return null;
			}
			//Save the position value
			if(httpRequest.getSession(false)!=null){
				httpRequest.getSession(false).setAttribute("Position", result);
			}
		}
		return result;
	}	

	@Override
	public String getUserConnectedUidSession(HttpServletRequest httpRequest) {
		String result = null;
		
		if(isXlnetCachingActive(httpRequest) && httpRequest.getSession(false)!=null && httpRequest.getSession(false).getAttribute("UidSession")!=null){
			result = (String) httpRequest.getSession(false).getAttribute("UidSession");
		}else{
			if (httpRequest.getSession(false).getAttribute("Position") == null && httpRequest.getSession(false).getAttribute("UserName") == null && httpRequest.getSession(false).getAttribute("UserProfiles") == null){
				if ((validateSession(httpRequest)== null)){
					logger.error("The XLNET session is invalid or the Xlnet's user isn't logger. Proceeding to clean the Security Context Holder.");
					SecurityContextHolder.clearContext();
					throw new AuthenticationCredentialsNotFoundException("The XLNET session is invalid or the Xlnet's user isn't logger");
				}
			}
			
			N38API n38Api = XlnetCore.getN38API(httpRequest);
			if(n38Api!=null){
				result = XlnetCore.getUidSesion(n38Api);
			}
			
			if (result != null){
				logger.trace("Connected UserConnectedUidSession is: "+result);
			}
			else{
				logger.warn("Connected UserConnectedUidSession is null!");
				return null;
			}
			//Save the uidSession value
			if(httpRequest.getSession(false)!=null){
				httpRequest.getSession(false).setAttribute("UidSession", result);
			}
		}
		return result;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Vector<String> getUserInstances(HttpServletRequest httpRequest) {		
		Vector<String> result = null;
		
		if(isXlnetCachingActive(httpRequest) && httpRequest.getSession(false)!=null && httpRequest.getSession(false).getAttribute("UserProfiles")!=null){			
			result = (Vector<String>) httpRequest.getSession(false).getAttribute("UserProfiles");
		}else{
			if (httpRequest.getSession(false).getAttribute("Position") ==null && httpRequest.getSession(false).getAttribute("UserName")==null && httpRequest.getSession(false).getAttribute("UidSession")!=null){
				if ((validateSession(httpRequest)== null)){
					logger.error("The XLNET session is invalid or the Xlnet's user isn't logger. Proceeding to clean the Security Context Holder.");
					SecurityContextHolder.clearContext();
					throw new AuthenticationCredentialsNotFoundException("The XLNET session is invalid or the Xlnet's user isn't logger");
				}
			}
			
			N38API n38Api = XlnetCore.getN38API(httpRequest);
			Document xmlSecurity = null;
			
			if(n38Api!=null){
				xmlSecurity = XlnetCore.getN38ItemSeguridad(n38Api, StaticsContainer.webAppName);
				if (xmlSecurity != null) {
					result = XlnetCore.searchParameterIntoXlnetSesion(xmlSecurity, XlnetCore.PATH_SUBTIPO_N38INSTANCIA);
				}
				xmlSecurity = null;
			}
						
			if (result != null){
				logger.trace("Connected UserProfiles are: "+result);
			}
			else{
				logger.warn("Connected UserProfiles is null!");
				return null;
			}
			//Save the UserProfiles value
			if(httpRequest.getSession(false)!=null)httpRequest.getSession(false).setAttribute("UserProfiles", result);
		}
		return result;
		
	}

	@Override
	public String getURLLogin(String originalURL) {
		logger.debug("Original URLLogin is :"+originalURL);
		StringBuilder resultURL = new StringBuilder("StaticsContainer.loginUrl");

		if (originalURL != null && !"".equals(originalURL)){
			resultURL.append("?N38API=");
			resultURL.append(originalURL);
		}
		logger.debug("URLLogin is: "+resultURL);
		
		return resultURL.toString();
	}

	//Method not properly working due to N38 related issues
	@Override
	public void logout(HttpServletRequest httpRequest) {
		String uidSession = getUserConnectedUidSession(httpRequest);
		logger.info( "Proceeding to destroy uidSession: "+ uidSession);

		N38APISesion n38ApiSesion = new N38APISesion();
		n38ApiSesion.n38APISesionDestruir(uidSession);

		logger.info( "Session "+uidSession+" destroyed!");
	}
	
	private boolean isXlnetCachingActive(HttpServletRequest httpRequest){
		boolean caching = true;

		//If the sessionId changed, disable XLNET caching
		if (httpRequest.getSession(false)!=null) {
			
				//XLNets retrieve n38UidSesionGlobal cookie
				Cookie requestCookies[] = httpRequest.getCookies ();
				String n38UidSesion = null;
				String n38UidSistemasXLNetS = null;
				StringBuilder udaXLNetsSessionId = new StringBuilder();
			
				if (requestCookies != null){
					for (int i = 0; i < requestCookies.length; i++) {
						if (requestCookies[i].getName().equals("n38UidSesion")){
							n38UidSesion = requestCookies[i].getValue ();
						} else if (requestCookies[i].getName().equals("n38UidSistemasXLNetS")){
							n38UidSistemasXLNetS = requestCookies[i].getValue ();
						}
                    }
				}
				
				udaXLNetsSessionId.append(n38UidSistemasXLNetS); 
				udaXLNetsSessionId.append("-");
				udaXLNetsSessionId.append(n38UidSesion);
				
				if (n38UidSesion != null && n38UidSistemasXLNetS != null){
					if (httpRequest.getSession(false).getAttribute("udaXLNetsSessionId")!=null){
						if(!(httpRequest.getSession(false).getAttribute("udaXLNetsSessionId").equals(udaXLNetsSessionId.toString()))){
							httpRequest.getSession(false).setAttribute("udaTimeStamp", System.currentTimeMillis());
							
							//It clears the cache of XLNets  
							xlnetCleanCache(httpRequest);
							
							logger.info("XLNet's caching of session "+httpRequest.getSession(false).getId()+" expired, because the XLNets user has changed");
							httpRequest.getSession(false).setAttribute("reloadData", "true");
							httpRequest.getSession(false).setAttribute("userChange", "true");
							caching = false;
							
							httpRequest.getSession(false).setAttribute("udaXLNetsSessionId", udaXLNetsSessionId.toString());
						}
					} else {
						httpRequest.getSession(false).setAttribute("udaXLNetsSessionId", udaXLNetsSessionId.toString());
					}
				}
				
				udaXLNetsSessionId = null;
		}
		//If the last XLNET session refresh was performed more than X minutes ago, disable caching
		if(httpRequest.getSession(false)!=null && httpRequest.getSession(false).getAttribute("udaTimeStamp")!=null){
			if((System.currentTimeMillis()- Long.valueOf(httpRequest.getSession(false).getAttribute("udaTimeStamp")+""))>TimeUnit.MILLISECONDS.convert(xlnetCachingPeriod.longValue(), TimeUnit.SECONDS)){
				
				httpRequest.getSession(false).setAttribute("udaTimeStamp", System.currentTimeMillis());
				
				//It clears the cache of XLNets  
				xlnetCleanCache(httpRequest);
	
				httpRequest.getSession(false).setAttribute("reloadData", "true");
				
				logger.info("XLNet's caching of session "+httpRequest.getSession(false).getId()+" expired, after, at least, "+xlnetCachingPeriod+" Seconds");
				caching = false;
			}
		}
		//If the session is new, disable XLNET caching
		if(httpRequest.getSession(false)!=null && (Boolean)httpRequest.getSession(false).getAttribute("udaVirgin")){
			httpRequest.getSession(false).setAttribute("udaVirgin", Boolean.FALSE);
			httpRequest.getSession(false).setAttribute("udaTimeStamp", System.currentTimeMillis());
			
			//It clears the cache of XLNets  
			xlnetCleanCache(httpRequest);
			
			logger.info("Session server "+httpRequest.getSession(false).getId()+" is new");
			caching = false;
		}
		//If the session does not exist, disable XLNET caching
		if(httpRequest.getSession(false)==null){
			httpRequest.getSession(true);
			logger.info("Session server "+httpRequest.getSession(false).getId()+" created");
			caching = false;
		}
		
		return caching;
	}

	//Getters & Setters
	public Long getXlnetCachingPeriod() {
		return xlnetCachingPeriod;
	}

	public void setXlnetCachingPeriod(Long xlnetCachingPeriod) {
		this.xlnetCachingPeriod = xlnetCachingPeriod;
	}
	
	//Cleaner method  of XLNETs cached information
	private void xlnetCleanCache(HttpServletRequest httpRequest){
		HttpSession session = httpRequest.getSession(false);
		session.removeAttribute("UserName");
		session.removeAttribute("Position");
		session.removeAttribute("UidSession");
		session.removeAttribute("UserProfiles");
	}
	
}