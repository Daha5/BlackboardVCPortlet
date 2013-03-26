package org.jasig.portlet.blackboardvcportlet.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.jasig.portlet.blackboardvcportlet.dao.ServerConfigurationDao;
import org.jasig.portlet.blackboardvcportlet.data.ServerConfiguration;
import org.jasig.portlet.blackboardvcportlet.service.ServerConfigurationService;
import org.jasig.portlet.blackboardvcportlet.service.util.SASWebServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.elluminate.sas.BasicAuth;
import com.elluminate.sas.GetServerConfigurationResponseCollection;
import com.elluminate.sas.ObjectFactory;
import com.elluminate.sas.ServerConfigurationResponse;

@Service("serverConfigurationService")
public class ServerConfigurationServiceImpl implements ServerConfigurationService {
	private static final Logger logger = LoggerFactory.getLogger(ServerConfigurationServiceImpl.class);
	private boolean isInit = false;
    private BasicAuth user;
    
    @Value("${bbc.username}")
    private String username;
    
    @Value("${bbc.password}")
    private String password;
    
    @Autowired
    ServerConfigurationDao serverConfigurationDao;

	@Autowired
	private SASWebServiceTemplate sasWebServiceTemplate;

	@Autowired
	private ObjectFactory objectFactory;

    /**
     * Gets the server configuration
     * @param prefs PortletPreferences
     * @return ServerConfiguration
     */
    public ServerConfiguration getServerConfiguration(PortletPreferences prefs) {
        refreshServerConfiguration(prefs);
        return serverConfigurationDao.getServerConfiguration();
    }

    /**
     * Stores the server configuration
     * @param serverConfiguration ServerConfiguration
     */
    public void storeServerConfiguration(ServerConfiguration serverConfiguration) {
        serverConfigurationDao.deleteServerConfiguration();
        serverConfigurationDao.saveServerConfiguration(serverConfiguration);
    }

    /**
     * Refreshes the server configuration, only updates local cache if last update
     * was older than an hour.
     * @param prefs PortletPreferences
     */
    public void refreshServerConfiguration(PortletPreferences prefs) {
        // Quota will refresh on the hour
        ServerConfiguration serverConfiguration = serverConfigurationDao.getServerConfiguration();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Date date = calendar.getTime();

		if (serverConfiguration == null || serverConfiguration.getLastUpdated().before(date))
		{

			logger.debug("refreshing server configuration");
			if (!this.isInit())
			{
				doInit(prefs);
			}

			try
			{ // Call Web Service Operation
				com.elluminate.sas.ServerConfiguration sc = objectFactory.createServerConfiguration();

				GetServerConfigurationResponseCollection responseCollection = (GetServerConfigurationResponseCollection) sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/GetServerConfiguration", sc);
				List<ServerConfigurationResponse> configResult = responseCollection.getServerConfigurationResponses();

				logger.debug("Result = " + configResult);
				for (ServerConfigurationResponse response : configResult)
				{
					ServerConfiguration configuration = new ServerConfiguration();
					configuration.setBoundaryTime(response.getBoundaryTime());
					configuration.setLastUpdated(new Date());
					configuration.setMaxAvailableCameras(response.getMaxAvailableCameras());
					configuration.setMaxAvailableTalkers(response.getMaxAvailableTalkers());
					if (response.isMayUseSecureSignOn())
					{
						configuration.setMayUseSecureSignOn('Y');
					} else
					{
						configuration.setMayUseSecureSignOn('N');
					}

					if (response.isMustReserveSeats())
					{
						configuration.setMustReserveSeats('Y');
					} else
					{
						configuration.setMustReserveSeats('N');
					}

					if (response.isRaiseHandOnEnter())
					{
						configuration.setRaiseHandOnEnter('Y');
					} else
					{
						configuration.setRaiseHandOnEnter('N');
					}

					configuration.setTimezone(response.getTimeZone());

					this.storeServerConfiguration(configuration);

				}
			}
			catch (Exception ex)
			{
				logger.error(ex.toString());
			}
		} else
		{
			logger.debug("Configuration doesn't need refreshed.");
		}
	}
    
    private boolean isInit() {
        return this.isInit;
    }

    /**
     * Init method for basic auth user.
     * @param prefs PortletPreferences
     */
    private void doInit(PortletPreferences prefs) {
        logger.debug("doInit called");
        user = new BasicAuth();
        user.setName(username);
        user.setPassword(password);
        isInit = true;
    }
}