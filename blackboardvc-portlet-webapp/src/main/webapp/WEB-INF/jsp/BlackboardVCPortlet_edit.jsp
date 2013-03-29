<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ include file="/WEB-INF/jsp/header.jsp"%>

<c:set var="namespace"><portlet:namespace/></c:set>
<c:if test="${!empty feedbackMessage}">
    <div class="uportal-channel-success"><spring:message code="${feedbackMessage}"/></div><br/>
</c:if>
    
<c:if test="${!empty warningMessage}">
    <div class="uportal-channel-warning"><spring:message code="${warningMessage}"/></div><br/>
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="uportal-channel-error">
    <ul>
    <c:forEach var="error" items="${errorMessage}">
        <li><spring:message code="${error}" text="${error}"/></li>
    </c:forEach>
    </ul>
    </div><br/>
</c:if>
<div class="uportal-channel-subtitle">1. Session information</div>
<hr>

<%-- Define the main action and render URLs --%>
<%--<portlet:actionURL portletMode="EDIT" var="formActionUrl" />--%>

<portlet:actionURL portletMode="EDIT" var="formActionUrl">
   <portlet:param name="action" value="saveSession"/>
</portlet:actionURL>

<portlet:actionURL portletMode="EDIT" var="addIntParticipantUrl">
    <portlet:param name="action" value="addInternalParticipant"/>
</portlet:actionURL>

<portlet:renderURL portletMode="EDIT" var="addExtParticipantUrl">
    <portlet:param name="action" value="addExternalParticipant"/>
</portlet:renderURL>

<form action="${formActionUrl}" method="post">
    <c:if test="${!empty session.sessionId}">
        <input type="hidden" name="sessionId" value="${session.sessionId}"/>
    </c:if>
    <c:if test="${!empty session.creatorOrgUnit}">
         <input type="hidden" name="creatorOrgUnit" value="${session.creatorOrgUnit}"/>
    </c:if>
<table>
    <tbody>
        <tr>
            
            <td><span class="uportal-channel-strong">Session Name: </span></td>
            <c:choose>
                <c:when test="${session.sessionId ne 0}">
                    <td><input type ="hidden" name="sessionName" value="${session.sessionName}"/>${session.sessionName}</td>
                </c:when>
                <c:otherwise>                  
                    <td><input name="sessionName" style="width: 50%;" class="uportal-input-text" value="${session.sessionName}"/></td>
                </c:otherwise>
            </c:choose>
            
        </tr>
        <tr><td></td><td class="uportal-channel-table-caption">The session name cannot be changed once created.</td></tr>
        <tr>
            <td><span class="uportal-channel-strong">Start Date and Time: </span></td>
            <td><input style="width: 82px;" value="<fmt:formatDate value="${session.startTime}" pattern="dd-MM-yyyy" />" name="startdate" id="${namespace}startdatepicker" style="width: 70px;" type="text">&nbsp;               
                <fmt:formatDate var="startHourValue" value="${session.startTime}" pattern="HH" />
                <select name="startHour">
                    <option value="00" ${startHourValue == "00" ? 'selected' : ''}>00</option>
                    <option value="01" ${startHourValue == "01" ? 'selected' : ''}>01</option>
                    <option value="02" ${startHourValue == "02" ? 'selected' : ''}>02</option>
                    <option value="03" ${startHourValue == "03" ? 'selected' : ''}>03</option>
                    <option value="04" ${startHourValue == "04" ? 'selected' : ''}>04</option>
                    <option value="05" ${startHourValue == "05" ? 'selected' : ''}>05</option>
                    <option value="06" ${startHourValue == "06" ? 'selected' : ''}>06</option>
                    <option value="07" ${startHourValue == "07" ? 'selected' : ''}>07</option>
                    <option value="08" ${startHourValue == "08" ? 'selected' : ''}>08</option>
                    <option value="09" ${startHourValue == "09" ? 'selected' : ''}>09</option>
                    <option value="10" ${startHourValue == "10" ? 'selected' : ''}>10</option>
                    <option value="11" ${startHourValue == "11" ? 'selected' : ''}>11</option>
                    <option value="12" ${startHourValue == "12" ? 'selected' : ''}>12</option>
                    <option value="13" ${startHourValue == "13" ? 'selected' : ''}>13</option>
                    <option value="14" ${startHourValue == "14" ? 'selected' : ''}>14</option>
                    <option value="15" ${startHourValue == "15" ? 'selected' : ''}>15</option>
                    <option value="16" ${startHourValue == "16" ? 'selected' : ''}>16</option>
                    <option value="17" ${startHourValue == "17" ? 'selected' : ''}>17</option>
                    <option value="18" ${startHourValue == "18" ? 'selected' : ''}>18</option>
                    <option value="19" ${startHourValue == "19" ? 'selected' : ''}>19</option>
                    <option value="20" ${startHourValue == "20" ? 'selected' : ''}>20</option>
                    <option value="21" ${startHourValue == "21" ? 'selected' : ''}>21</option>
                    <option value="22" ${startHourValue == "22" ? 'selected' : ''}>22</option>
                    <option value="23" ${startHourValue == "23" ? 'selected' : ''}>23</option>
                </select>:
                <fmt:formatDate var="startMinuteValue" value='${session.startTime}' pattern='mm' />
                <select name="startMinute">
                    <option value="00" ${startMinuteValue =="00" ? 'selected' : ''}>00</option>
                    <option value="15" ${startMinuteValue =="15" ? 'selected' : ''}>15</option>
                    <option value="30" ${startMinuteValue =="30" ? 'selected' : ''}>30</option>
                    <option value="45" ${startMinuteValue =="45" ? 'selected' : ''}>45</option>
                </select>                   
            </td>
        </tr>
        <tr><td><span class="uportal-channel-strong">End Date and Time: </span></td>
            <td><input style="width: 82px;" value="<fmt:formatDate value="${session.endTime}" pattern="dd-MM-yyyy" />" name="enddate" id="${namespace}enddatepicker" style="width: 70px;" type="text">&nbsp;
                <fmt:formatDate var="endHourValue" value="${session.endTime}" pattern="HH" />
                <select name="endHour">
                    <option value="00" ${endHourValue == "00" ? 'selected' : ''}>00</option>
                    <option value="01" ${endHourValue == "01" ? 'selected' : ''}>01</option>
                    <option value="02" ${endHourValue == "02" ? 'selected' : ''}>02</option>
                    <option value="03" ${endHourValue == "03" ? 'selected' : ''}>03</option>
                    <option value="04" ${endHourValue == "04" ? 'selected' : ''}>04</option>
                    <option value="05" ${endHourValue == "05" ? 'selected' : ''}>05</option>
                    <option value="06" ${endHourValue == "06" ? 'selected' : ''}>06</option>
                    <option value="07" ${endHourValue == "07" ? 'selected' : ''}>07</option>
                    <option value="08" ${endHourValue == "08" ? 'selected' : ''}>08</option>
                    <option value="09" ${endHourValue == "09" ? 'selected' : ''}>09</option>
                    <option value="10" ${endHourValue == "10" ? 'selected' : ''}>10</option>
                    <option value="11" ${endHourValue == "11" ? 'selected' : ''}>11</option>
                    <option value="12" ${endHourValue == "12" ? 'selected' : ''}>12</option>
                    <option value="13" ${endHourValue == "13" ? 'selected' : ''}>13</option>
                    <option value="14" ${endHourValue == "14" ? 'selected' : ''}>14</option>
                    <option value="15" ${endHourValue == "15" ? 'selected' : ''}>15</option>
                    <option value="16" ${endHourValue== "16" ? 'selected' : ''}>16</option>
                    <option value="17" ${endHourValue == "17" ? 'selected' : ''}>17</option>
                    <option value="18" ${endHourValue == "18" ? 'selected' : ''}>18</option>
                    <option value="19" ${endHourValue == "19" ? 'selected' : ''}>19</option>
                    <option value="20" ${endHourValue == "20" ? 'selected' : ''}>20</option>
                    <option value="21" ${endHourValue == "21" ? 'selected' : ''}>21</option>
                    <option value="22" ${endHourValue == "22" ? 'selected' : ''}>22</option>
                    <option value="23" ${endHourValue == "23" ? 'selected' : ''}>23</option>
                </select>:
                <fmt:formatDate var="endMinuteValue" value='${session.endTime}' pattern='mm' />
                <select name="endMinute">
                    <option value="00" ${endMinuteValue =="00" ? 'selected' : ''}>00</option>
                    <option value="15" ${endMinuteValue =="15" ? 'selected' : ''}>15</option>
                    <option value="30" ${endMinuteValue =="30" ? 'selected' : ''}>30</option>
                    <option value="45" ${endMinuteValue =="45" ? 'selected' : ''}>45</option>
                </select>  
            </td>
        </tr>
        <tr><td></td><td class="uportal-channel-table-caption">Enter dates as in dd-mm-yyyy format. Time must be entered in 15 minute increments. Start time must be in the future.</td></tr>
        <tr>
            <td><span class="uportal-channel-strong">Early Session Entry: </span></td>
            <td>
                <select name="boundaryTime">
                    <option value="15" ${session.boundaryTime == "15" ? 'selected' : ''}>15 minutes</option>
                    <option value="30" ${session.boundaryTime == "30" ? 'selected' : ''}>30 minutes</option>
                    <option value="45" ${session.boundaryTime == "45" ? 'selected' : ''}>45 minutes</option>
                    <option value="60" ${session.boundaryTime == "60" ? 'selected' : ''}>1 hour</option>
                    <option value="120" ${session.boundaryTime == "120" ? 'selected' : ''}>2 hours</option>
                    <option value="180" ${session.boundaryTime == "180" ? 'selected' : ''}>3 hours</option>
                </select> 
            </td>
        </tr>
        <tr><td></td><td class="uportal-channel-table-caption">The period before the start of the session during which users can enter the session.</td></tr>
        <c:choose>
            <c:when test="${!empty fullAccess}">             
                <tr>
                    <td><span class="uportal-channel-strong">Max Simultaneous Talkers: </span></td>
                    <td>
                        <select name="maxTalkers">
                            <option  value="1" ${session.maxTalkers == "1" ? 'selected' : ''}>1</option>
                            <option value="2" ${session.maxTalkers == "2" ? 'selected' : ''}>2</option>
                            <option value="3" ${session.maxTalkers == "3" ? 'selected' : ''}>3</option>
                            <option value="4" ${session.maxTalkers == "4" ? 'selected' : ''}>4</option>
                            <option value="5" ${session.maxTalkers == "5" ? 'selected' : ''}>5</option>
                            <option value="6" ${session.maxTalkers == "6" ? 'selected' : ''}>6</option>
                        </select> 
                    </td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">Maximum number of simultaneous talkers allowed at the start of a session.</td></tr>
                <tr>
                    <td><span class="uportal-channel-strong">Max Cameras: </span></td>
                    <td>
                        <select name="maxCameras">
                            <option value="1" ${session.maxCameras == "1" ? 'selected' : ''}>1</option>
                            <option value="2" ${session.maxCameras == "2" ? 'selected' : ''}>2</option>
                            <option value="3" ${session.maxCameras == "3" ? 'selected' : ''}>3</option>
                            <option value="4" ${session.maxCameras == "4" ? 'selected' : ''}>4</option>
                            <option value="5" ${session.maxCameras == "5" ? 'selected' : ''}>5</option>
                            <option value="6" ${session.maxCameras == "6" ? 'selected' : ''}>6</option>
                        </select> 
                    </td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">Maximum number of simultaneous web cameras allowed at the start of a session.</td></tr>
                <tr>
                    <td><span class="uportal-channel-strong">Supervised: </span></td><td><input name="mustBeSupervised" type="checkbox" value="Y" ${session.mustBeSupervised ? 'checked' : ''}/></td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">Moderators may view all private chat messages in the session.</td></tr>
                <tr>
                    <td><span class="uportal-channel-strong">All Permissions: </span></td><td><input name="permissionsOn" type="checkbox" value="Y" ${session.permissionsOn ? 'checked' : ''}/></td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">All participants have full permissions access to session resources such as audio, whiteboard, etc.</td></tr>
                <tr>
                    <td><span class="uportal-channel-strong">Raise Hand on Entry: </span></td><td><input name="raiseHandOnEnter" type="checkbox" value="Y" ${session.raiseHandOnEnter ? 'checked' : ''}/></td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">Users automatically raise their hand when they join the session.</td></tr>
                <tr>
                    <td><span class="uportal-channel-strong">Recording Mode: </span></td>
                    <td>
                        <select name="recordingModeType" disabled>
                            <option value="1" ${session.recordingModeType == "1" ? 'selected' : ''}>Manual</option>
                            <option value="2" ${session.recordingModeType == "2" ? 'selected' : ''}>Automatic</option>
                            <option value="3" ${session.recordingModeType == "3" ? 'selected' : ''}>Disabled</option>
                        </select> 
                        <input type="hidden" name="recordingModeType" value="${session.recordingModeType}"/>
                    </td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">The mode of recording the session.</td></tr>
                <tr>
                    <td><span class="uportal-channel-strong">Hide Names in Recordings: </span></td><td><input name="hideParticipantNames" type="checkbox" value="Y" ${session.hideParticipantNames  ? 'checked' : ''}/></td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">Names of session participants are hidden from viewers of recordings.</td></tr>               
                <tr>
                    <td><span class="uportal-channel-strong">Allow In-Session Invitations: </span></td><td><input name="allowInSessionInvites" type="checkbox" value="Y" ${session.allowInSessionInvites ? 'checked' : ''}/></td>
                </tr>
                <tr><td></td><td class="uportal-channel-table-caption">Moderators may send invitations to join the session from within the session.</td></tr>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="maxTalkers" value="${session.maxTalkers}"/>
                <input type="hidden" name="maxCameras" value="${session.maxCameras}"/>
                <input type="hidden" name="mustBeSupervised" value="${session.mustBeSupervised}"/>
                <input type="hidden" name="permissionsOn" value="${session.permissionsOn}" />
                <input type="hidden" name="raiseHandOnEnter" value="${session.raiseHandOnEnter}" />
                <input type="hidden" name="recordingModeType" value="${session.recordingModeType}" />
                <input type="hidden" name="hideParticipantNames" value="${session.hideParticipantNames}"/>
                <input type="hidden" name="allowInSessionInvites" value="${session.allowInSessionInvites}"/>
            </c:otherwise>
        </c:choose>
                
    </tbody>
</table>

<div class="uportal-channel-subtitle">2. Moderators</div>
<hr>
<table>
    <c:if test="${fn:length(moderators) gt 0}">
    <thead>
        <tr class="uportal-channel-table-header">           
            <th>Username</th>
            <th>Name</th>
            <th>Email address</th>
            <th></th>
        </tr>
    </thead>
    </c:if>
    <tbody>
        <c:forEach var="moderator" items="${moderators}" varStatus="loopStatus">
            <input type="hidden" name="moderatorUids" value="${moderator.uid}"/>
            <input type="hidden" name="moderatorDisplayNames" value="${moderator.displayName}"/>
            <input type="hidden" name="moderatorEmails" value="${moderator.email}"/>
          <tr class="${loopStatus.index % 2 == 0 ? 'uportal-channel-table-row-odd' : 'uportal-channel-table-row-even'}">
              <td>${moderator.uid}</td><td>${moderator.displayName}</td><td>${moderator.email}</td><td><input value="${loopStatus.index}" name="deleteModerator" type="checkbox"/></td>
          </tr>
        </c:forEach>
       
        <c:if test="${fn:length(moderators) gt 0}">
               <tr><td colspan="3"><input value="Delete Moderator(s)" name="action" style="text-transform: none;" class="uportal-button" type="submit"></td></tr>
        </c:if>
        <tr><td colspan="3"><input id="${namespace}moderatiorUidInput" name="moderatorUid"type="text">&nbsp;<input id="${namespace}addModeratorSubmit" name="action" value="Add Moderator(s)" style="text-transform: none;" class="uportal-button" type="submit"></td></tr>
        <tr><td colspan="3" class="uportal-channel-table-caption">You can search for moderators using uun or display name. To search for multiple moderators, separate each with a comma.</td></tr>
    </tbody>

</table>


<div class="uportal-channel-subtitle">3. Participants</div>
<hr>

<div class="uportal-channel-subtitle">Internal participants</div>
<table>
    <c:if test="${fn:length(intParticipants) gt 0}">
    <thead>
        <tr class="uportal-channel-table-header">           
            <th>Username</th>
            <th>Name</th>
            <th>Email address</th>
            <th></th>
        </tr>
    </thead>
    </c:if>
    <tbody>
         <c:forEach var="intParticipant" items="${intParticipants}" varStatus="loopStatus">
                <input type="hidden" name="intParticipantUids" value="${intParticipant.uid}"/>
            <input type="hidden" name="intParticipantDisplayNames" value="${intParticipant.displayName}"/>
            <input type="hidden" name="intParticipantEmails" value="${intParticipant.email}"/>
          <tr class="${loopStatus.index % 2 == 0 ? 'uportal-channel-table-row-odd' : 'uportal-channel-table-row-even'}">
              <td>${intParticipant.uid}</td><td>${intParticipant.displayName}</td><td>${intParticipant.email}</td><td><input value="${loopStatus.index}" name="deleteIntParticipant" type="checkbox"/></td>
          </tr>
        </c:forEach>    
        <c:if test="${fn:length(intParticipants) gt 0}">
               <tr><td colspan="3"><input value="Delete Internal Participant(s)" name="action" style="text-transform: none;" class="uportal-button" type="submit"></td></tr>
        </c:if>
        <tr><td colspan="3"><input id="${namespace}intParticipantInput" name="intParticipants"type="text">&nbsp;<input id="${namespace}addIntParticipantSubmit" name="action" value="Add Participant(s)" style="text-transform: none;" class="uportal-button" type="submit"></td></tr>
        <tr><td colspan="3" class="uportal-channel-table-caption">You can search for participants using uun or display name. To search for multiple participants, separate each with a comma.</td></tr>
    </tbody>
</table>

<div class="uportal-channel-subtitle">External participants</div>
<table>
    <c:if test="${fn:length(extParticipants) gt 0}">
    <thead>
        <tr class="uportal-channel-table-header">           
            <th>Display Name</th>           
            <th>Email address</th>
            <th></th>
        </tr>
    </thead>
    </c:if>
    <tbody>  
        <c:forEach var="extParticipant" items="${extParticipants}" varStatus="loopStatus">
            
            <input type="hidden" name="extParticipantDisplayNames" value="${extParticipant.displayName}"/>
            <input type="hidden" name="extParticipantEmails" value="${extParticipant.email}"/>
               <tr class="${loopStatus.index % 2 == 0 ? 'uportal-channel-table-row-odd' : 'uportal-channel-table-row-even'}">
                   <td><c:out value="${extParticipant.displayName}"/></td><td><c:out value="${extParticipant.email}"/></td><td><input value="${loopStatus.index}" name="deleteExtParticipant" type="checkbox"/></td>
                </tr>
            </c:forEach>
           <c:if test="${fn:length(extParticipants) gt 0}">
               <tr><td colspan="3"><input value="Delete External Participant(s)" name="action" style="text-transform: none;" class="uportal-button" type="submit"></td></tr>
           </c:if>
    </tbody>
</table>
<table>
    <tbody>    
        <tr><td>Display name: </td><td><input id="${namespace}extParticipantDisplayNameInput" name="extParticipantDisplayName"type="text"></td></tr>
        <tr><td>Email: </td><td><input id="${namespace}extParticipantEmailInput" name="extParticipantEmail" type="text"></td></tr>
        <tr><td><input id="${namespace}addExtParticipantSubmit" name="action" value="Add External Participant" class="uportal-button" type="submit"></td></tr>
        <tr><td colspan="3" class="uportal-channel-table-caption">Enter an external participant.</td></tr>     
    </tbody>
</table>
<c:choose>
    <c:when test="${!empty fullAccess}">
       <c:if test="${session.sessionId ne 0}">
        <div class="uportal-channel-subtitle">4. File upload</div>
        <hr/>
        <div class="uportal-channel-subtitle"><spring:message code="editscreen.presentationuploadsubtitle" text="Presentation upload"/></div>
        <table summary="<spring:message code="editscreen.presentationuploadsubtitle" text="Presentation upload"/>">
            <thead>
                <tr class="uportal-channel-table-header">
                    <th>Filename</th>
                    <th style="width: 70px;"></th>
                </tr>
            </thead>
            <tbody>
                <c:if test="${!empty presentation}">
                    <tr class="uportal-channel-table-row-odd">
                       <input type="hidden" name="presentationId" value="${presentation.presentationId}"/>
                      <td>${presentation.fileName}</td><td><input value="Delete Presentation" name="action" class="uportal-button" type="submit">
                      </td>
                    </tr>
                </c:if>
                    <tr><td colspan="2"><input name="presentationUpload" size="40" type="file">&nbsp;<input value="Upload Presentation" name="action" class="uportal-button" type="submit"></td></tr>
                <tr><td colspan="2" class="uportal-channel-table-caption"><spring:message code="editscreen.presentationuploadcaption" text="Select a presentation/plan file to upload. You can only attach one file at a time."/></td>
                </tr>
                
            </tbody>
        </table>

        <div class="uportal-channel-subtitle"><spring:message code="editscreen.multimediauploadsubtitle" text="Multimedia upload"/></div>
        
        
        
        <table summary="Multimedia upload">
            <thead>
                <tr class="uportal-channel-table-header">
                    <th>Filename</th>
                    <th style="width: 70px;"></th>
                </tr>
            </thead>
            <tbody>
                
                <c:if test="${!empty multimedia}">
                    <c:forEach items="${multimedia}" var="multimediaItem" varStatus="loopStatus">
                        <tr class="${loopStatus.index % 2 == 0 ? 'uportal-channel-table-row-odd' : 'uportal-channel-table-row-even'}">
                         <input type="hidden" name="multimediaId" value="${multimediaItem.multimediaId}"/>
                         <td>${multimediaItem.fileName}</td><td><input type="checkbox" name="deleteMultimediaFiles" value="${multimediaItem.multimediaId}"/></td>
                       </tr>
                    </c:forEach>
                   
                    <tr><td colspan="3"><input value="Delete Multimedia Item(s)" name="action" class="uportal-button" type="submit"></td></tr>
 
                </c:if>              
                <tr><td colspan="2"><input name="multimediaUpload" size="40" type="file">&nbsp;<input value="Upload Multimedia" name="action" class="uportal-button" type="submit"></td></tr>
                <tr><td colspan="2" class="uportal-channel-table-caption">Select other multimedia files to upload. Any files will be scanned for viruses upon upload.</td></tr>
            </tbody>
        </table>
       </c:if>
    </c:when>
</c:choose>
<table>
    <tbody>
        <tr>
            <td><input class="uportal-button" name="action" value="Save Session" type="submit"></td>
                <portlet:renderURL var="cancelAction" portletMode="VIEW" windowState="NORMAL"/>
            <td><input class="uportal-button" name="cancel" value="Cancel" onclick="window.location='${cancelAction}'" type="button"></td>
        </tr>
    </tbody>

</table>
</form>     
<script type="text/javascript">
up.jQuery(function() {
    var $ = up.jQuery;
    $(document).ready(function(){       
        $("#${namespace}startdatepicker" ).datepicker({showButtonPanel: true,dateFormat: 'dd-mm-yy'});
        $("#${namespace}enddatepicker" ).datepicker({showButtonPanel: true,dateFormat: 'dd-mm-yy'});
        $('#${namespace}moderatiorUidInput').keypress(function (e) {if (e.which == 13) {$('#${namespace}addModeratorSubmit').focus().click();return false;}});
        $('#${namespace}intParticipantInput').keypress(function (e) {if (e.which == 13) {$('#${namespace}addIntParticipantSubmit').focus().click();return false;}});
        $('#${namespace}extParticipantDisplayNameInput').keypress(function (e) {if (e.which == 13) {$('#${namespace}addExtParticipantSubmit').focus().click();return false;}});
        $('#${namespace}extParticipantEmailInput').keypress(function (e) {if (e.which == 13) {$('#${namespace}addExtParticipantSubmit').focus().click();return false;}});
    });
});
</script>