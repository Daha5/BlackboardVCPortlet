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
package org.jasig.portlet.blackboardvcportlet.service;

import java.net.UnknownHostException;

import org.jasig.portlet.blackboardvcportlet.data.SessionRecording;
import org.joda.time.DateTime;



/**
 * Service Class for handling Recording interactions
 * @author Richard Good
 */
public interface RecordingService {
    void updateSessionRecordings(long sessionId, long startTime, long endTime);
    
    SessionRecording getSessionRecording(long recordingId);
    
    void updateSessionRecordingName(long recordingId, String roomName);
    
    void removeRecording(long recordingId);
    
    /**
     * Repairs local cache of recordings
     * @param startDate The beginning date you wish to process
     * @param endDate The end date you wish to process
     * @return returns a int[3]. int[0] is number of recordings processed. int[1] is how many added to local cache. int[2] is how many erred.
     */
    int[] datafixRecordings(DateTime startDate, DateTime endDate);

    void cronDatafixRecordings() throws UnknownHostException;
}
