/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.blackboardvcportlet.mvc.sessionmngr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.blackboardvcportlet.dao.BlackboardSessionDao;
import org.jasig.portlet.blackboardvcportlet.dao.BlackboardUserDao;
import org.jasig.portlet.blackboardvcportlet.data.BlackboardSession;
import org.jasig.portlet.blackboardvcportlet.data.BlackboardUser;
import org.jasig.portlet.blackboardvcportlet.service.AuthorisationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 * Controller for handling Portlet view mode
 *
 * @author Richard Good
 */
@Controller
@RequestMapping("VIEW")
public class BlackboardVCPortletViewController
{
	private static final Logger logger = LoggerFactory.getLogger(BlackboardVCPortletViewController.class);

	private BlackboardUserDao blackboardUserDao;
	private BlackboardSessionDao blackboardSessionDao;
	
//	private SessionService sessionService;
//	private RecordingService recordingService;
	private AuthorisationService authService;
//	private UserService userService;
	
	
	@Autowired
	public void setBlackboardSessionDao(BlackboardSessionDao blackboardSessionDao) {
        this.blackboardSessionDao = blackboardSessionDao;
    }

    @Autowired
	public void setBlackboardUserDao(BlackboardUserDao blackboardUserDao) {
        this.blackboardUserDao = blackboardUserDao;
    }

//    @Autowired
//	public void setSessionService(SessionService sessionService)
//	{
//		this.sessionService = sessionService;
//	}

//	@Autowired
//	public void setRecordingService(RecordingService recordingService)
//	{
//		this.recordingService = recordingService;
//	}

	@Autowired
	public void setAuthService(AuthorisationService authService)
	{
		this.authService = authService;
	}

//	@Autowired
//	public void setUserService(UserService userService)
//	{
//		this.userService = userService;
//	}

	@RenderMapping
	public String view(PortletRequest request, ModelMap model)
	{
		final BlackboardUser blackboardUser = this.getBlackboardUser(request);
		
		final boolean isAdmin = authService.isAdminAccess(request);
		
		final Set<BlackboardSession> sessions;
        if (isAdmin) {
            sessions = this.blackboardSessionDao.getAllSessions();
            //TODO get list of all recordings
        } else {
            sessions = new HashSet<BlackboardSession>();
            
            final Set<BlackboardSession> chairedSessionsForUser = this.blackboardUserDao.getChairedSessionsForUser(blackboardUser.getUserId());
            sessions.addAll(chairedSessionsForUser);
            
            final Set<BlackboardSession> nonChairedSessionsForUser = this.blackboardUserDao.getNonChairedSessionsForUser(blackboardUser.getUserId());
            sessions.addAll(nonChairedSessionsForUser);
            
            //TODO get list of all recordings for user
        }

		logger.debug("sessions size:" + sessions.size());
		model.addAttribute("sessions", sessions);

//		logger.debug("gotten recordings, size:" + recordings.size());
//		model.addAttribute("recordings", recordings);
		model.addAttribute("feedbackMessage", request.getParameter("feedbackMessage"));
		model.addAttribute("warningMessage", request.getParameter("warningMessage"));

		logger.debug("isAdmin:" + isAdmin);

		if (isAdmin || authService.isFullAccess(request)) {
			logger.debug("full access set for view mode");
			model.addAttribute("fullAccess", Boolean.TRUE);
		}

		return "BlackboardVCPortlet_view";
	}
	
	/**
	 * Launch page for a specific session
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RenderMapping(params = "action=viewSession")
	public String viewSession(PortletRequest request, @RequestParam long sessionId, ModelMap model)
	{
	    logger.debug("viewSession called");

		logger.debug("sessionId:" + sessionId);

//		try
//		{
			logger.debug("calling sessionService.getSession");
			BlackboardSession session = this.blackboardSessionDao.getSession(sessionId);
			model.addAttribute("session", session);

			logger.debug("done call");
			if (session == null)
			{
				logger.error("session is null!");
			}

            final BlackboardUser blackboardUser = getBlackboardUser(request);
            final Set<BlackboardUser> sessionChairs = this.blackboardSessionDao.getSessionChairs(session.getSessionId());
            
	        if (blackboardUser.equals(session.getCreator()) || sessionChairs.contains(blackboardUser) || authService.isAdminAccess(request)) {
	            model.addAttribute("currUserCanEdit", true);
	        }
	        else
			{
			    model.addAttribute("currUserCanEdit", false);
			}
	        
	        
			if (session.getEndTime().isAfterNow())
			{
				if (authService.isAdminAccess(request) || sessionChairs.contains(blackboardUser))
				{
					// Removing the username parameter will make collaborate prompt for the person's name
					model.addAttribute("guestUrl", session.getGuestUrl().replaceFirst("&username=Guest", ""));
				}

				logger.debug("Session is still open, we can show the launch url");
				final Set<BlackboardUser> sessionNonChairs = this.blackboardSessionDao.getSessionNonChairs(session.getSessionId());
				
				// If the user is specified in chair or non chair list then get the URL
				if (sessionChairs.contains(blackboardUser) || sessionNonChairs.contains(blackboardUser))
				{
				    //TODO need to lookup the UserSessionUrl
				    
					model.addAttribute("showLaunchSession", true);
					logger.debug("User is in the chair/non-chair list");
					logger.debug("gotten user sessionUrl");
					model.addAttribute("launchSessionUrl", "TODO");
				}
			} else
			{
				logger.debug("Session is closed");
				model.addAttribute("showLaunchSession", false);
			}


			model.addAttribute("session", session);
			if (sessionChairs.contains(blackboardUser) || authService.isAdminAccess(request))
			{
				model.addAttribute("showCSVDownload", true);
			}
			
			//TODO use spring error handling
//		catch (Exception e)
//		{
//			logger.error("error caught:", e);
//			model.addAttribute("errorMessage", "A problem occurred retrieving the session details. If this problem re-occurs please contact support.");
//		}
		return "BlackboardVCPortlet_viewSession";

	}

    private BlackboardUser getBlackboardUser(PortletRequest request) {
        final String mail = getAttribute(request, "emailAttributeName", "mail");
        final String displayName = getAttribute(request, "displayNameAttributeName", "displayName");
        
        BlackboardUser user = this.blackboardUserDao.getBlackboardUser(mail);
        if (user == null) {
            user = this.blackboardUserDao.createBlackboardUser(mail, displayName);
        }
        
        //Update with current display name
        user.setDisplayName(displayName);
        
        //Update all key user attributes
        final PortletPreferences prefs = request.getPreferences();
        final String[] keyUserAttributeNames = prefs.getValues("keyUserAttributeNames", new String[0]);
        @SuppressWarnings("unchecked")
        final Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        for (final String keyUserAttributeName : keyUserAttributeNames) {
            final String value = userInfo.get(keyUserAttributeName);
            final Map<String, String> userAttributes = user.getAttributes();
            if (StringUtils.isNotEmpty(value)) {
                userAttributes.put(keyUserAttributeName, value);
            }
            else {
                userAttributes.remove(keyUserAttributeName);
            }
        }
        
        //Persist modification
        this.blackboardUserDao.updateBlackboardUser(user);
        
        return user;
    }
    
    private String getAttribute(PortletRequest request, String preferenceName, String... defaultValues) {
        final PortletPreferences prefs = request.getPreferences();
        final String[] attributeNames = prefs.getValues(preferenceName, defaultValues);
        
        @SuppressWarnings("unchecked")
        final Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
        
        for (final String mailAttributeName : attributeNames) {
            final String value = userInfo.get(mailAttributeName);
            if (value != null) {
                return value;
            }
        }
        
        return null;
    }

	/**
	 * CSV Download function
	 *
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@ResourceMapping(value = "csvDownload")
	public void csvDownload(ResourceRequest request, @RequestParam long sessionId, ResourceResponse response) throws IOException
	{
		logger.debug("csvDownload called");
		logger.debug("sessionId:" + sessionId);

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/csv");
		response.setProperty("Content-Disposition", "inline; filename=participantList_" + sessionId + ".csv");

		final BlackboardUser blackboardUser = this.getBlackboardUser(request);

		PrintWriter stringWriter = null;
		try
		{
			stringWriter = response.getWriter();
			//ByteArrayOutputStream outputStream = new ByteArrayOutputStream(response.getPortletOutputStream());
			stringWriter.println("UID,Display Name,Email address,Participant type");
			logger.debug("calling sessionService.getSession");
			
			final BlackboardSession session = this.blackboardSessionDao.getSession(sessionId);
			
			logger.debug("done call");
			if (session == null) {
				logger.error("session is null!");
				return;
			}
			
			final Set<BlackboardUser> sessionChairs = this.blackboardSessionDao.getSessionChairs(session.getSessionId());
            if (!sessionChairs.contains(blackboardUser)) {
				logger.warn("User not authorised to see csv");
				return;
			}

            
			logger.debug("Adding chair list into moderators");
			for (final BlackboardUser user : sessionChairs) {
				stringWriter.println(user.getEmail() + "," + user.getDisplayName() + "," + user.getEmail() + ",Moderator");
			}
			logger.debug("added moderators to CSV output");


			final Set<BlackboardUser> sessionNonChairs = this.blackboardSessionDao.getSessionNonChairs(session.getSessionId());
			logger.debug("Adding nonchair list to participants");
            for (final BlackboardUser user : sessionNonChairs) {
                if (user.getAttributes().isEmpty()) {
                    stringWriter.println(user.getEmail() + "," + user.getDisplayName() + "," + user.getEmail() + ",External Participant");
                }
                else {
                    stringWriter.println(user.getEmail() + "," + user.getDisplayName() + "," + user.getEmail() + ",Internal Participant");
                }
            }
		}
		finally {
		    stringWriter.close();
		}
		
		//TODO use spring error handling
//		catch (Exception e)
//		{
//			logger.error("Exception caught building model for CSV download", e);
//		}
	}
}
