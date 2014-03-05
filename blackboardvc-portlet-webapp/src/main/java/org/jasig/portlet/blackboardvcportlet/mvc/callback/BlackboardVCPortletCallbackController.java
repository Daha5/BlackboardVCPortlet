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
package org.jasig.portlet.blackboardvcportlet.mvc.callback;

import org.jasig.portlet.blackboardvcportlet.data.ServerConfiguration;
import org.jasig.portlet.blackboardvcportlet.service.RecordingService;
import org.jasig.portlet.blackboardvcportlet.service.ServerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for session finish callback.
 * @author Richard Good
 */
@Controller
public class BlackboardVCPortletCallbackController {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private RecordingService recordingService;
    private ServerConfigurationService serverConfigurationService;
    
    @Autowired
    public void setServerConfigurationService(ServerConfigurationService value) 
    {
    	this.serverConfigurationService = value;
    }

	@Autowired
	public void setRecordingService(RecordingService recordingService)
	{
		this.recordingService = recordingService;
	}
	
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public class ResourceNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	/**
     * Callback method. Looks for a passed session_id and updates the recordings
     * @throws Exception
     */
    @RequestMapping("/recCallback/{securityToken:.*}")
    public ModelAndView callback(
            @PathVariable("securityToken") String securityToken,
            @RequestParam("session_id") long sessionId,
            @RequestParam("room_opened_millis") long roomOpenedMillis,
            @RequestParam("room_closed_millis") long roomClosedMillis, 
            @RequestParam("rec_playback_link") String recPlaybackLink) throws Exception {
    	
        final ServerConfiguration serverConfiguration = serverConfigurationService.getServerConfiguration();
        if(serverConfiguration.getRandomCallbackUrl().equalsIgnoreCase(securityToken)) {
        	if(recPlaybackLink != null && recPlaybackLink.length() > 0)
        		recordingService.updateSessionRecordings(sessionId, roomOpenedMillis, roomClosedMillis);
        } else {
        	logger.error("Invalid callback URL provided. Expected :" + serverConfiguration.getRandomCallbackUrl() + "; Received : " + securityToken);
        	throw new ResourceNotFoundException();
        }
        
        return new ModelAndView("callback");
        
    }
}
