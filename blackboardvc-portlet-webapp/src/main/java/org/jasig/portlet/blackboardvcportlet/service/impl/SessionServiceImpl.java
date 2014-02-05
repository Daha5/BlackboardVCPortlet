package org.jasig.portlet.blackboardvcportlet.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jasig.portlet.blackboardvcportlet.dao.ConferenceUserDao;
import org.jasig.portlet.blackboardvcportlet.dao.MultimediaDao;
import org.jasig.portlet.blackboardvcportlet.dao.PresentationDao;
import org.jasig.portlet.blackboardvcportlet.dao.SessionDao;
import org.jasig.portlet.blackboardvcportlet.dao.UserSessionUrlDao;
import org.jasig.portlet.blackboardvcportlet.dao.ws.MultimediaWSDao;
import org.jasig.portlet.blackboardvcportlet.dao.ws.PresentationWSDao;
import org.jasig.portlet.blackboardvcportlet.dao.ws.SessionWSDao;
import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser.Roles;
import org.jasig.portlet.blackboardvcportlet.data.Multimedia;
import org.jasig.portlet.blackboardvcportlet.data.Presentation;
import org.jasig.portlet.blackboardvcportlet.data.Session;
import org.jasig.portlet.blackboardvcportlet.data.UserSessionUrl;
import org.jasig.portlet.blackboardvcportlet.security.ConferenceUserService;
import org.jasig.portlet.blackboardvcportlet.service.MailTemplateService;
import org.jasig.portlet.blackboardvcportlet.service.SessionForm;
import org.jasig.portlet.blackboardvcportlet.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;
import org.springframework.ws.client.WebServiceClientException;

import com.elluminate.sas.BlackboardMultimediaResponse;
import com.elluminate.sas.BlackboardPresentationResponse;
import com.elluminate.sas.BlackboardSessionResponse;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

@Service
public class SessionServiceImpl implements SessionService, ServletContextAware {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private ConferenceUserService conferenceUserService;
	private ConferenceUserDao conferenceUserDao;
    private SessionDao sessionDao;
    private MultimediaDao multimediaDao;
    private PresentationDao presentationDao;
	private SessionWSDao sessionWSDao;
	private MultimediaWSDao multimediaWSDao;
	private PresentationWSDao presentationWSDao;
	private MailTemplateService mailService;
	private File tempDir;

	private UserSessionUrlDao userSessionUrlDao;
	
	@Autowired
	public void setMailTemplateService(MailTemplateService mailService) {
		this.mailService = mailService;
	}
	
	@Autowired
	public void setUserSessionUrlDao (UserSessionUrlDao dao) {
		this.userSessionUrlDao = dao;
	}
	
	
	@Autowired
	public void setPresentationDao(PresentationDao presentationDao) {
        this.presentationDao = presentationDao;
    }

    @Autowired
	public void setMultimediaDao(MultimediaDao multimediaDao) {
        this.multimediaDao = multimediaDao;
    }

    @Autowired
	public void setConferenceUserDao(ConferenceUserDao conferenceUserDao) {
        this.conferenceUserDao = conferenceUserDao;
    }

    @Autowired
	public void setConferenceUserService(ConferenceUserService conferenceUserService) {
        this.conferenceUserService = conferenceUserService;
    }

	@Autowired
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

	@Autowired
	public void setSessionWSDao(SessionWSDao value) {
		this.sessionWSDao = value;
	}

    @Autowired
	public void setMultimediaWSDao(MultimediaWSDao multimediaWSDao) {
        this.multimediaWSDao = multimediaWSDao;
    }

    @Autowired
    public void setPresentationWSDao(PresentationWSDao presentationWSDao) {
        this.presentationWSDao = presentationWSDao;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.tempDir = WebUtils.getTempDir(servletContext);
    }

    /**
	 * A user needs "edit" to view the set of session chairs but we don't want the call to fail
	 * if they only have "view" permission. So we pre-auth them with view and then filter all
	 * the results unless they have "edit"
	 */
    @Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
	@PostFilter("hasRole('ROLE_ADMIN') || hasPermission(#session, 'edit')")
    public Set<ConferenceUser> getSessionChairs(Session session) {
        return new LinkedHashSet<ConferenceUser>(sessionDao.getSessionChairs(session));
    }
    
    @Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
    public boolean isSessionParticipant(Session session, ConferenceUser user) {
    	ConferenceUser userFromDB = conferenceUserDao.getUser(user.getUserId());
    	return (sessionDao.getSessionChairs(session).contains(userFromDB)
    			|| sessionDao.getSessionNonChairs(session).contains(userFromDB));
    }

    @Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
	@Transactional
	public String getOrCreateSessionUrl(ConferenceUser user, Session session) {
		return getOrCreateSessionUrl(user, session,false);
	}
	
    @Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
	@Transactional
	public String getOrCreateSessionUrl(ConferenceUser user, Session session, boolean forceFetch) {
		//check for the url in the db
		UserSessionUrl url = null;
		
		if(!forceFetch)
			url = userSessionUrlDao.getUserSessionUrlsBySessionAndUser(session, user);
		
		if(url == null) {
			//if null then create a user's session url via web service call
			String urlString = sessionWSDao.buildSessionUrl(session.getBbSessionId(), user);
			//save to the database
			url = userSessionUrlDao.createUserSessionUrl(session, user, urlString);
		}
		return url.getUrl();
	}
    
    @Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
	@Transactional
	public void deleteSessionUrl(ConferenceUser user, Session session) {
    	userSessionUrlDao.deleteOldSessionUrls(session, user);
    }
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
	public void populateLaunchUrl(ConferenceUser user, Session session) {
		
		if(isSessionParticipant(session, user)) {
			session.setLaunchUrl(getOrCreateSessionUrl(user, session));
		} else {
			session.setLaunchUrl(session.getGuestUrl());
		}
	}
	
    /**
     * A user needs "edit" to view the set of session non chairs but we don't want the call to fail
     * if they only have "view" permission. So we pre-auth them with view and then filter all
     * the results unless they have "edit"
     */
    @Override
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
    @PostFilter("hasRole('ROLE_ADMIN') || hasPermission(#session, 'edit')")
    public Set<ConferenceUser> getSessionNonChairs(Session session) {
        return new LinkedHashSet<ConferenceUser>(sessionDao.getSessionNonChairs(session));
    }

    /**
     * A user needs "edit" and ROLE_FULL_ACCESS to view the set of session multemedia
     * but we don't want the call to fail if they only have "view" permission. So we
     * pre-auth them with view and then filter all the results unless they have ROLE_FULL_ACCESS and "edit"
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#session, 'view')")
    @PostFilter("hasRole('ROLE_ADMIN') || (hasRole('ROLE_FULL_ACCESS') && hasPermission(#session, 'edit'))")
    public Set<Multimedia> getSessionMultimedia(Session session) {
        return new LinkedHashSet<Multimedia>(sessionDao.getSessionMultimedias(session));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'view')")
    public Session getSession(long sessionId) {
        return this.sessionDao.getSession(sessionId);
    }
    
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Set<Session> getAllSessions() {
        return this.sessionDao.getAllSessions();
    }
    

    /*
     * Not rolling back for WS related exceptions so the work done "so far" is still persisted to the database in
     * an attempt to keep the WS and DB layers in sync 
     */
    @Override
    @Transactional(noRollbackFor = { WebServiceClientException.class, XmlMappingException.class })
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSession(long sessionId) {
        final Session session = this.sessionDao.getSession(sessionId);
        
        
        final Set<Multimedia> multimedias = this.sessionDao.getSessionMultimedias(session);
        for (final Multimedia multimedia : multimedias) {
            removeMultimediaFromSession(session, null, multimedia);
        }
        
        this.deletePresentation(session.getSessionId());
        
        //This sends cancellation email for creator, chairs, and non-chairs
        this.mailService.buildAndSendCancelationMeetingEmail(session);
        
        this.sessionWSDao.deleteSession(session.getBbSessionId());
        this.sessionDao.deleteSession(session);
    }

    private void removeMultimediaFromSession(Session session, Set<Long> bbMultimediaIds, Multimedia multimedia) {
        //Un-link multimedia file from session
        if (bbMultimediaIds != null) {
        	if(bbMultimediaIds.contains(multimedia.getBbMultimediaId()))
        		this.multimediaWSDao.removeSessionMultimedia(session.getBbSessionId(), multimedia.getBbMultimediaId());
        } else {
        	this.multimediaWSDao.removeSessionMultimedia(session.getBbSessionId(), multimedia.getBbMultimediaId());
        }
        this.sessionDao.deleteMultimediaFromSession(session, multimedia);

        //Delete multimedia file from repository
        try {
            this.multimediaWSDao.removeRepositoryMultimedia(multimedia.getBbMultimediaId());
        }
        catch (WebServiceClientException e) {
            //See if the multimedia file actually exists
            final List<BlackboardMultimediaResponse> userMultimedias = this.multimediaWSDao.getRepositoryMultimedias(null, multimedia.getBbMultimediaId(), null);
            
            //Multimedia file exists but we failed to remove it, throw the exception
            if (!userMultimedias.isEmpty()) {
                throw e;
            }
            
            //Multimedia file doesn't exist on the BB side, remove our local DB version
        }
        this.multimediaDao.deleteMultimedia(multimedia);
    }

    @Override
    @Transactional
    @PreAuthorize("#sessionForm.newSession || hasRole('ROLE_ADMIN') || hasPermission(#sessionForm.sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public Session createOrUpdateSession(ConferenceUser user, SessionForm sessionForm) {
    	final Session session;
        if (sessionForm.isNewSession()) {
            final BlackboardSessionResponse sessionResponse = sessionWSDao.createSession(user, sessionForm);
            final String guestUrl = sessionWSDao.buildGuestSessionUrl(sessionResponse.getSessionId());
            
            session = sessionDao.createSession(sessionResponse, guestUrl);
            mailService.buildAndSendSessionEmails(session, false, true);
        }
        else {
            session = sessionDao.getSession(sessionForm.getSessionId());
            final BlackboardSessionResponse sessionResponse = sessionWSDao.updateSession(session.getBbSessionId(), sessionForm);
            
            boolean isTimeChange = !(session.getStartTime().getMillis() == sessionResponse.getStartTime())
            		|| !(session.getEndTime().getMillis() == sessionResponse.getEndTime());
            sessionDao.updateSession(sessionResponse);
            if(isTimeChange) {
            	mailService.buildAndSendSessionEmails(session, true, sessionForm.isNeedToSendInitialEmail());
            }
        }
        return session;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public ConferenceUser addSessionChair(long sessionId, String displayName, String email) {
        final ConferenceUser newSessionChair = this.conferenceUserService.getOrCreateConferenceUser(displayName, email);
        
        this.addSessionChair(sessionId, newSessionChair, true);
        
        return newSessionChair;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public ConferenceUser updateRole(long sessionId, long userId, Roles newRole) {
    	final ConferenceUser user = this.conferenceUserDao.getUser(userId);
    	final Session session = this.sessionDao.getSession(sessionId);
    	this.deleteSessionUrl(user, session);
    	if(Roles.CHAIR.equals(newRole)) {
    		//remove nonchair
    		this.removeSessionNonChairs(sessionId, false, userId);
    		//add chair
    		this.addSessionChair(sessionId, user, false);
    		
    		
    	} else {
    		//remove chair
    		this.removeSessionChairs(sessionId, false, userId);
    		//add nonchair
    		this.addSessionNonChair(sessionId, user, false);
    		
    	}
    	//send update role email
    	mailService.sendEmail(mailService.buildSwitchRolesEmail(user, session, newRole));
    	return user;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public ConferenceUser addSessionChair(long sessionId, long userId, boolean sendEmail) {
    	final ConferenceUser user = this.conferenceUserDao.getUser(userId);
        if (user != null) {
            addSessionChair(sessionId, user, sendEmail);
        }
        return user;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public ConferenceUser addSessionChair(long sessionId, long userId) {
        return addSessionChair(sessionId,userId, true);
    }

    private void addSessionChair(long sessionId, ConferenceUser user, boolean sendEmail) {
        Assert.notNull(user, "user must not be null");
        
        final Session session = this.sessionDao.getSession(sessionId);
        final Set<ConferenceUser> sessionChairs = new LinkedHashSet<ConferenceUser>(this.getSessionChairs(session));
        sessionChairs.add(user);
        
        final BlackboardSessionResponse sessionResponse = this.sessionWSDao.setSessionChairs(session.getBbSessionId(), sessionChairs);
        sessionDao.updateSession(sessionResponse);
        if(sendEmail)
        	mailService.sendEmail(mailService.buildModeratorMailTask(user, session, false));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionChairs(long sessionId, boolean sendEmail, long... userIds) {
    	final Set<ConferenceUser> users = conferenceUserDao.getUsers(userIds);
        
        this.removeSessionChairs(sessionId, users, sendEmail);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionChairs(long sessionId, long... userIds) {
        removeSessionChairs(sessionId, true, userIds);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionChairs(long sessionId, Iterable<ConferenceUser> users) {
        removeSessionChairs(sessionId, users, true);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionChairs(long sessionId, Iterable<ConferenceUser> users, boolean sendCancelEmail) {
    	final Session session = this.sessionDao.getSession(sessionId);
        final Set<ConferenceUser> sessionChairs = new LinkedHashSet<ConferenceUser>(this.getSessionChairs(session));
        
        for (final ConferenceUser user : users) {

            // the user might or might not be a session chair,
            // so we might or might not be making a change.
            // If we're not making a change because they are not a chair to remove
            // then don't bother sending an email.

            // remove the user if present and note whether was present
            boolean madeChange = sessionChairs.remove(user);

            // if we made a change and we are to send an email, send it.
            if(madeChange && sendCancelEmail) {
            	mailService.sendEmail(mailService.buildCancellationNoticeMailTask(user, session));
            }

        }
        
        final BlackboardSessionResponse sessionResponse;
        
        if(sessionChairs.isEmpty()) {
            this.sessionWSDao.clearSessionChairList(session.getBbSessionId());
            sessionDao.clearSessionUserList(session.getSessionId(), true);
        } else {
            sessionResponse = this.sessionWSDao.setSessionChairs(session.getBbSessionId(), sessionChairs);
            sessionDao.updateSession(sessionResponse);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public ConferenceUser addSessionNonChair(long sessionId, String displayName, String email) {
        final ConferenceUser newSessionNonChair = this.conferenceUserService.getOrCreateConferenceUser(displayName, email);
        
        addSessionNonChair(sessionId, newSessionNonChair, true);
        
        return newSessionNonChair;
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public ConferenceUser addSessionNonChair(long sessionId, long userId) {
        final ConferenceUser user = this.conferenceUserDao.getUser(userId);
        if (user != null) {
            this.addSessionNonChair(sessionId, user, true);
        }
        return user;
    }

    private void addSessionNonChair(long sessionId, ConferenceUser user, boolean sendEmail) {
        Assert.notNull(user, "user must not be null");
        final Session session = this.sessionDao.getSession(sessionId);
        final Set<ConferenceUser> sessionNonChairs = new LinkedHashSet<ConferenceUser>(this.getSessionNonChairs(session));
        sessionNonChairs.add(user);
        
        final BlackboardSessionResponse sessionResponse = this.sessionWSDao.setSessionNonChairs(session.getBbSessionId(), sessionNonChairs);
        sessionDao.updateSession(sessionResponse);
        if(sendEmail)
        	mailService.sendEmail(mailService.buildParticipantMailTask(user, session, false));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionNonChairs(long sessionId, long... userIds) {
        final Set<ConferenceUser> users = conferenceUserDao.getUsers(userIds);
        
        this.removeSessionNonChairs(sessionId, users);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionNonChairs(long sessionId, boolean sendEmail, long... userIds) {
        final Set<ConferenceUser> users = conferenceUserDao.getUsers(userIds);
        this.removeSessionNonChairs(sessionId, users, sendEmail);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionNonChairs(long sessionId, Iterable<ConferenceUser> users) {
    	removeSessionNonChairs(sessionId, users,true);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit')")
    public void removeSessionNonChairs(long sessionId, Iterable<ConferenceUser> users, boolean sendEmail) {
        final Session session = this.sessionDao.getSession(sessionId);
        final Set<ConferenceUser> sessionNonChairs = new LinkedHashSet<ConferenceUser>(this.getSessionNonChairs(session));
        
        for (final ConferenceUser user : users) {

            // the user might or might not be a non-chair, so
            // we might or might not be there to remove.
            // if this is a no-op, do not bother the user with a confusing email.

            // remove the user and note whether this had any effect

            boolean madeChange = sessionNonChairs.remove(user);

            if (madeChange && sendEmail) {
                mailService.sendEmail(mailService.buildCancellationNoticeMailTask(user, session));
            }

        }
        
        if(sessionNonChairs.isEmpty()) {
            this.sessionWSDao.clearSessionNonChairList(session.getBbSessionId());
            sessionDao.clearSessionUserList(session.getSessionId(), false);
        } else {
            final BlackboardSessionResponse sessionResponse = this.sessionWSDao.setSessionNonChairs(session.getBbSessionId(), sessionNonChairs);
            sessionDao.updateSession(sessionResponse);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasRole('ROLE_FULL_ACCESS') && hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit'))")
    public void addMultimedia(long sessionId, MultipartFile file) {
        final Session session = this.sessionDao.getSession(sessionId);
        final ConferenceUser conferenceUser = this.conferenceUserService.getCurrentConferenceUser();
        
        final BlackboardMultimediaResponse multimediaResponse = createSessionMultimedia(session, conferenceUser, file);
        
        //Add Multimedia object to local DB
        final String filename = FilenameUtils.getName(file.getOriginalFilename());
        final Multimedia multimedia = this.multimediaDao.createMultimedia(multimediaResponse, filename);
        
        //Associate Multimedia with session
        this.sessionDao.addMultimediaToSession(session, multimedia);
    }
    
    @Override
    @Transactional(noRollbackFor = { WebServiceClientException.class, XmlMappingException.class })
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasRole('ROLE_FULL_ACCESS') && hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit'))")
    public void deleteMultimedia(long sessionId, long... multimediaIds) {
        final Session session = this.sessionDao.getSession(sessionId);
        
        final Set<Long> bbMultimediaIds = getBlackboardMultimediaIds(session);
        for (final long multimediaId : multimediaIds) {
            final Multimedia multimedia = this.multimediaDao.getMultimediaById(multimediaId);
            removeMultimediaFromSession(session, bbMultimediaIds, multimedia);
        }
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasRole('ROLE_FULL_ACCESS') && hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit'))")
    public void addPresentation(long sessionId, MultipartFile file) {
        final Session session = this.sessionDao.getSession(sessionId);
        if (session.getPresentation() != null) {
            this.deletePresentation(session.getSessionId());
        }
        
        final ConferenceUser conferenceUser = this.conferenceUserService.getCurrentConferenceUser();
        
        final BlackboardPresentationResponse presentationResponse = createSessionPresentation(session, conferenceUser, file);
        
        final String filename = FilenameUtils.getName(file.getOriginalFilename());
        final Presentation presentation = this.presentationDao.createPresentation(presentationResponse, filename);
        
        //Associate Presentation with session
        this.sessionDao.addPresentationToSession(session, presentation);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') || (hasRole('ROLE_FULL_ACCESS') && hasPermission(#sessionId, 'org.jasig.portlet.blackboardvcportlet.data.Session', 'edit'))")
    public void deletePresentation(long sessionId) {
        final Session session = this.sessionDao.getSession(sessionId);
        final Presentation presentation = session.getPresentation();
        if (presentation == null) {
            return;
        }
        
        try {
            this.presentationWSDao.deleteSessionPresenation(session.getBbSessionId(), presentation.getBbPresentationId());
        }
        catch (WebServiceClientException e) {
            //See if the presentation association actually exists
            final List<BlackboardPresentationResponse> sessionPresentations = this.presentationWSDao.getSessionPresentations(session.getBbSessionId());
            
            //Session presentation exists but failed to remove it, throw the exception
            if (!sessionPresentations.isEmpty()) {
                throw e;
            }
            
            //Presentation association doesn't exist but we still need to delete the association no our side.
        }
        this.sessionDao.removePresentationFromSession(session);
        
        
        try {
            this.presentationWSDao.deletePresentation(presentation.getBbPresentationId());
        }
        catch (WebServiceClientException e) {
            //See if the presentation actually exists
            final ConferenceUser creator = session.getCreator();
            final List<BlackboardPresentationResponse> repositoryPresentations = this.presentationWSDao.getRepositoryPresentations(creator.getUniqueId(), presentation.getBbPresentationId(), null);
            
            //Presentation exists but failed to remove it, throw the exception
            if (!repositoryPresentations.isEmpty()) {
                throw e;
            }
            
            //Presentation doesn't exist but we still need to delete the association no our side.
        }
        this.presentationDao.deletePresentation(presentation);
    }

    private Set<Long> getBlackboardMultimediaIds(final Session session) {
        final List<BlackboardMultimediaResponse> multimedias = this.multimediaWSDao.getSessionMultimedias(session.getBbSessionId());
        final Iterator<BlackboardMultimediaResponse> multimediasItr = multimedias.iterator();
        return ImmutableSet.copyOf(
            Iterators.transform(multimediasItr, 
                new Function<BlackboardMultimediaResponse, Long>() {
                    public Long apply(BlackboardMultimediaResponse r) {
                        return r.getMultimediaId();
                    }
                }));
    }

    private BlackboardMultimediaResponse createSessionMultimedia(Session session, ConferenceUser conferenceUser, MultipartFile file) {
        final String filename = FilenameUtils.getName(file.getOriginalFilename());
        
        File multimediaFile = null;
        try {
            //Transfer the uploaded file to our own temp file so we can use a FileDataSource
            multimediaFile = File.createTempFile(filename, ".tmp", this.tempDir);
            file.transferTo(multimediaFile);
            
            //Upload the file to BB
            return this.multimediaWSDao.createSessionMultimedia(
                session.getBbSessionId(), 
                conferenceUser.getUniqueId(), 
                filename, 
                "",
                new DataHandler(new FileDataSource(multimediaFile)));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to upload multimedia file '" + filename + "'", e);
        }
        finally {
            FileUtils.deleteQuietly(multimediaFile);
        }
    }

    //TODO consolidate these, extract uploadPresentation/createSessionMultimedia to generic interface and use that
    private BlackboardPresentationResponse createSessionPresentation(Session session, ConferenceUser conferenceUser, MultipartFile file) {
        final String filename = FilenameUtils.getName(file.getOriginalFilename());
        
        File multimediaFile = null;
        try {
            //Transfer the uploaded file to our own temp file so we can use a FileDataSource
            multimediaFile = File.createTempFile(filename, ".tmp", this.tempDir);
            file.transferTo(multimediaFile);
            
            //Upload the file to BB
            return this.presentationWSDao.uploadPresentation(
                    session.getBbSessionId(), 
                    conferenceUser.getUniqueId(), 
                    filename, 
                    "",
                    new DataHandler(new FileDataSource(multimediaFile)));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to upload multimedia file '" + filename + "'", e);
        }
        finally {
            FileUtils.deleteQuietly(multimediaFile);
        }
    }
}