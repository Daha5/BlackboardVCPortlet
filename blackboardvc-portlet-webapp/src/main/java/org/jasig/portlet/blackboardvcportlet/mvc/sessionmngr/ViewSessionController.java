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

import java.util.Set;

import javax.portlet.PortletRequest;

import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
import org.jasig.portlet.blackboardvcportlet.data.Session;
import org.jasig.portlet.blackboardvcportlet.security.ConferenceUserService;
import org.jasig.portlet.blackboardvcportlet.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.google.common.collect.ImmutableSortedSet;

/**
 * Controller for handling Portlet view mode
 *
 * @author Richard Good
 */
@Controller
@RequestMapping("VIEW")
public class ViewSessionController
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private ConferenceUserService conferenceUserService;
	private SessionService sessionService;
	
	
	@Autowired
	public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

	@Autowired
    public void setConferenceUserService(ConferenceUserService conferenceUserService) {
        this.conferenceUserService = conferenceUserService;
    }

	@RenderMapping(params = "action=viewSession")
	public String viewSession(PortletRequest request, @RequestParam long sessionId, ModelMap model)	{
        final Session session = this.sessionService.getSession(sessionId);
        //TODO what if session is null?
        model.addAttribute("session", session);
        
        final Set<ConferenceUser> sessionChairs = this.sessionService.getSessionChairs(session);
        model.addAttribute("sessionChairs", ImmutableSortedSet.copyOf(ConferenceUserDisplayNameComparator.INSTANCE, sessionChairs));
        
        final Set<ConferenceUser> sessionNonChairs = this.sessionService.getSessionNonChairs(session);
        model.addAttribute("sessionNonChairs", ImmutableSortedSet.copyOf(ConferenceUserDisplayNameComparator.INSTANCE, sessionNonChairs));
        
        //Session hasn't completed yet, show session launch URL
        //TODO should we check if we are within the "boundary time"?
        if (session.getEndTime().isAfterNow()) {
            final ConferenceUser conferenceUser = this.conferenceUserService.getCurrentConferenceUser();
            
            if (sessionChairs.contains(conferenceUser) || sessionNonChairs.contains(conferenceUser)) {
                model.addAttribute("launchUrl", this.sessionService.getOrCreateSessionUrl(conferenceUser, session));
            }
            else {
                //Fall back to guest URL
                model.addAttribute("launchUrl", session.getGuestUrl());
            }
        }

        return "BlackboardVCPortlet_viewSession";
	}
}
