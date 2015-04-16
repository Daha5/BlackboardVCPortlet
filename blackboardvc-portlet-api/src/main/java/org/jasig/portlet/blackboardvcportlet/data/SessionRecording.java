package org.jasig.portlet.blackboardvcportlet.data;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * Entity class for storing recordings.
 * 
 * @author Eric Dalquist
 */
public interface SessionRecording extends Serializable {

    Session getSession();

    String getRoomName();
    
    void setRoomName(String roomName);

    long getRecordingSize();
    
    String getDisplayRecordingSize();

    DateTime getCreationDate();

    boolean isSecureSignOn();

    String getRecordingUrl();

    DateTime getRoomEnd();

    DateTime getRoomStart();

    long getBbRecordingId();

    long getRecordingId();
    
    boolean isCreated();

}