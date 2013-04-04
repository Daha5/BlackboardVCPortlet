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

import freemarker.template.utility.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.blackboardvcportlet.dao.BlackboardSessionDao;
import org.jasig.portlet.blackboardvcportlet.dao.BlackboardUserDao;
import org.jasig.portlet.blackboardvcportlet.data.*;
import org.jasig.portlet.blackboardvcportlet.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import javax.portlet.*;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private SessionService sessionService;
	private RecordingService recordingService;
	private AuthorisationService authService;
	private UserService userService;
	
	
	@Autowired
	public void setBlackboardSessionDao(BlackboardSessionDao blackboardSessionDao) {
        this.blackboardSessionDao = blackboardSessionDao;
    }

    @Autowired
	public void setBlackboardUserDao(BlackboardUserDao blackboardUserDao) {
        this.blackboardUserDao = blackboardUserDao;
    }

    @Autowired
	public void setSessionService(SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	@Autowired
	public void setRecordingService(RecordingService recordingService)
	{
		this.recordingService = recordingService;
	}

	@Autowired
	public void setAuthService(AuthorisationService authService)
	{
		this.authService = authService;
	}

	@Autowired
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	/**
	 * Standard view mode handler
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RenderMapping
	public ModelAndView view(RenderRequest request, RenderResponse response)
	{
		logger.debug("view called");
		logger.debug("feedbackMessage value:" + request.getParameter("feedbackMessage"));
		ModelAndView modelAndView = new ModelAndView("BlackboardVCPortlet_view");

		final PortletPreferences prefs = request.getPreferences();

		Map<String, String> userInfo = (Map<String, String>) request.getAttribute(PortletRequest.USER_INFO);
		uid = userInfo.get("uid");
		displayName = userInfo.get("displayName");
		mail = userInfo.get("mail");

		eduPersonAffiliation = userInfo.get("eduPersonAffiliation");
		eduPersonOrgUnitDn = userInfo.get("eduPersonOrgUnitDN");
		logger.debug("uid:" + uid);
		logger.debug("displayName:" + displayName);
		logger.debug("mail:" + mail);
		logger.debug("eduPersonAffiliation:" + eduPersonAffiliation);
		logger.debug("eduPersonOrgUnitDN:" + eduPersonOrgUnitDn);
		isAdmin = authService.isAdminAccess(request);
		List<BlackboardSession> sessions;
		if (isAdmin)
		{
			sessions = sessionService.getAllSessions();
		} else
		{
			sessions = sessionService.getSessionsForUser(uid);
		}

		logger.debug("sessions size:" + sessions.size());
		modelAndView.addObject("sessions", sessions);

		List<SessionRecording> recordings;

		if (isAdmin)
		{
			recordings = recordingService.getRecordingsForAdmin();
		} else
		{
			recordings = recordingService.getRecordingsForUser(uid);
		}

		logger.debug("gotten recordings, size:" + recordings.size());
		modelAndView.addObject("recordings", recordings);
		modelAndView.addObject("feedbackMessage", request.getParameter("feedbackMessage"));
		modelAndView.addObject("warningMessage", request.getParameter("warningMessage"));

		logger.debug("isAdmin:" + isAdmin);

		if (isAdmin || authService.isFullAccess(request))
		{
			logger.debug("full access set for view mode");
			modelAndView.addObject("fullAccess", "true");
		}

		return modelAndView;
	}

	protected String getAttribute(PortletRequest request, String preferenceName, String... defaultValues) {
	    final PortletPreferences prefs = request.getPreferences();
	    final String[] attributeNames = prefs.getValues(preferenceName, defaultValues);
	    
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
	 * Launch page for a specific session
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RenderMapping(params = "action=viewSession")
	public ModelAndView viewSession(RenderRequest request, RenderResponse response)
	{

		logger.debug("viewSession called");
		final PortletPreferences prefs = request.getPreferences();
		ModelAndView modelAndView = new ModelAndView("BlackboardVCPortlet_viewSession");

		String sessionId = request.getParameter("sessionId");
		logger.debug("sessionId:" + sessionId);

		try
		{
			logger.debug("calling sessionService.getSession");
			BlackboardSession session = sessionService.getSession(Long.valueOf(sessionId));

			logger.debug("done call");
			if (session == null)
			{
				logger.error("session is null!");
			}

            final BlackboardUser blackboardUser = getBlackboardUser(request);
            final Set<BlackboardUser> sessionChairs = this.blackboardSessionDao.getSessionChairs(session.getSessionId());
            
	        if (blackboardUser.equals(session.getCreator()) || sessionChairs.contains(blackboardUser) || authService.isAdminAccess(request)) {
	            modelAndView.addObject("currUserCanEdit", true);
	        }
	        else
			{
			    modelAndView.addObject("currUserCanEdit", false);
			}
			displayName = userInfo.get("displayName");
//			SessionUrlId sessionUrlId;
			SessionUrl sessionUrl;
			if (session.getEndTime().after(new Date()))
			{
				if (authService.isAdminAccess(request) || (session.getChairList() != null && session.getChairList().indexOf(uid) != -1))
				{
					// Get the guest launch URL
					sessionUrlId = new SessionUrlId();
					sessionUrlId.setSessionId(session.getSessionId());
					sessionUrlId.setDisplayName("Guest");
					sessionUrl = sessionService.getSessionUrl(sessionUrlId);
					// Removing the username parameter will make collaborate prompt for the person's name
					modelAndView.addObject("guestUrl", sessionUrl.getUrl().replaceFirst("&username=Guest", ""));
				}
			}

			if (session.getEndTime().after(new Date()))
			{
				logger.debug("Session is still open, we can show the launch url");
				// If the user is specified in chair or non chair list then get the URL
				if ((session.getChairList() != null && session.getChairList().indexOf(uid) != -1) || (session.getNonChairList() != null && session.getNonChairList().indexOf(uid) != -1))
				{
					modelAndView.addObject("showLaunchSession", "true");
					logger.debug("User is in the chair/non-chair list");
					sessionUrlId = new SessionUrlId();
					sessionUrlId.setSessionId(session.getSessionId());
					sessionUrlId.setUserId(uid);
					sessionUrlId.setDisplayName(displayName);
					sessionUrl = sessionService.getSessionUrl(sessionUrlId);
					logger.debug("gotten user sessionUrl");
					modelAndView.addObject("launchSessionUrl", sessionUrl.getUrl());
				}
			} else
			{
				logger.debug("Session is closed");
				modelAndView.addObject("showLaunchSession", "false");
			}


			modelAndView.addObject("session", session);
			if (authService.isAdminAccess(request) || (session.getChairList() != null && session.getChairList().indexOf(uid) != -1))
			{
				modelAndView.addObject("showCSVDownload", "true");
			}

		}
		catch (Exception e)
		{
			logger.error("error caught:", e);
			modelAndView.addObject("errorMessage", "A problem occurred retrieving the session details. If this problem re-occurs please contact support.");
		}
		return modelAndView;

	}

    private BlackboardUser getBlackboardUser(RenderRequest request) {
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

	/**
	 * CSV Download function
	 *
	 * @param request
	 * @param response
	 */
	@ResourceMapping(value = "csvDownload")
	public void csvDownload(ResourceRequest request, ResourceResponse response)
	{
		logger.debug("csvDownload called");
		//ModelAndView modelAndView = new ModelAndView("csvDownload");

		String sessionId = request.getParameter("sessionId");
		logger.debug("sessionId:" + sessionId);

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/csv");
		response.setProperty("Content-Disposition", "inline; filename=participantList_" + sessionId + ".csv");

		String userId = request.getRemoteUser();

		if (userId == null)
		{
			logger.debug("Remote user not set, falling back to form post user");
			userId = (String) request.getParameter("uid");
		}

		try
		{
			PrintWriter stringWriter = response.getWriter();
			//ByteArrayOutputStream outputStream = new ByteArrayOutputStream(response.getPortletOutputStream());
			stringWriter.println("UID,Display Name,Email address,Participant type");
			logger.debug("calling sessionService.getSession");
			BlackboardSession session = sessionService.getSession(Long.valueOf(sessionId));
			logger.debug("done call");
			if (session == null)
			{
				logger.error("session is null!");
				stringWriter.flush();
				stringWriter.close();
				return;
			} else if (session.getChairList() == null || session.getChairList().indexOf(userId) == -1)
			{
				logger.warn("User not authorised to see csv");
				stringWriter.flush();
				stringWriter.close();
				return;
			}


			User user;
			if (session.getChairList() != null && !session.getChairList().equals(""))
			{
				logger.debug("Adding chair list into moderators");
				String[] chairList = StringUtil.split(session.getChairList(), ',');
				//List<User> moderatorList = new ArrayList<User>();
				for (int i = 0; i < chairList.length; i++)
				{
					user = userService.getUserDetails(chairList[i]);
					if (user == null)
					{
						user = new User();
						user.setUid(chairList[i]);
						user.setDisplayName("Unknown user");
					}
					stringWriter.println(user.getUid() + "," + user.getDisplayName() + "," + user.getEmail() + ",Moderator");
				}

				logger.debug("added moderators to CSV output");
			}

			if (session.getNonChairList() != null && !session.getChairList().equals(""))
			{
				logger.debug("Adding nonchair list to participants");
				String[] nonChairList = StringUtil.split(session.getNonChairList(), ',');


				for (int i = 0; i < nonChairList.length; i++)
				{
					user = userService.getUserDetails(nonChairList[i]);
					if (user != null)
					{
						stringWriter.println(user.getUid() + "," + user.getDisplayName() + "," + user.getEmail() + ",Internal Participant");
					} else
					{
						user = sessionService.getExtParticipant(session.getSessionId(), nonChairList[i]);
						if (user == null)
						{
							user = new User();
							user.setEmail(nonChairList[i]);
						}

						stringWriter.println(user.getUid() + "," + user.getDisplayName() + "," + user.getEmail() + ",External Participant");
					}
				}

			}
			stringWriter.flush();
			stringWriter.close();
		}
		catch (Exception e)
		{
			logger.error("Exception caught building model for CSV download", e);
		}
	}
}
