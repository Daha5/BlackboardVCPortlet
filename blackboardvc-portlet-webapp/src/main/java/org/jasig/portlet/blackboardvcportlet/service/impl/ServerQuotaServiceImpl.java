package org.jasig.portlet.blackboardvcportlet.service.impl;

import com.elluminate.sas.GetServerQuotasResponseCollection;
import com.elluminate.sas.ObjectFactory;
import com.elluminate.sas.ServerQuotasResponse;
import org.jasig.portlet.blackboardvcportlet.dao.ServerQuotaDao;
import org.jasig.portlet.blackboardvcportlet.dao.impl.ServerQuotaImpl;
import org.jasig.portlet.blackboardvcportlet.data.ServerQuota;
import org.jasig.portlet.blackboardvcportlet.service.ServerQuotaService;
import org.jasig.portlet.blackboardvcportlet.service.util.SASWebServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * Service class for Server Quota
 * @author rgood
 */
@Service
public class ServerQuotaServiceImpl implements ServerQuotaService
{
	private static final Logger logger = LoggerFactory.getLogger(ServerQuotaServiceImpl.class);

    private ServerQuotaDao serverQuotaDao;
	private SASWebServiceTemplate sasWebServiceTemplate;
	private ObjectFactory objectFactory;

	@Autowired
	public void setServerQuotaDao(ServerQuotaDao serverQuotaDao)
	{
		this.serverQuotaDao = serverQuotaDao;
	}

	@Autowired
	public void setSasWebServiceTemplate(SASWebServiceTemplate sasWebServiceTemplate)
	{
		this.sasWebServiceTemplate = sasWebServiceTemplate;
	}

	@Autowired
	public void setObjectFactory(ObjectFactory objectFactory)
	{
		this.objectFactory = objectFactory;
	}

	/**
     * Gets the Server quota
     * @return ServerQuota
     */
     public ServerQuota getServerQuota()
     {
         return this.serverQuotaDao.getServerQuota();
     }
    
     /**
      * Refresh the server quota, only goes to Collaborate if last update
      * was longer than an hour ago.
      */
	@Scheduled(fixedRate=3600000)
    public void refreshServerQuota()
    {
		logger.info("Server Quota being refreshed");
		try
		{
			// Call Web Service Operation
			GetServerQuotasResponseCollection serverQuotasResponseCollection = (GetServerQuotasResponseCollection)sasWebServiceTemplate.marshalSendAndReceiveToSAS("http://sas.elluminate.com/GetServerQuotas", objectFactory.createGetServerQuotas(null));
			List<ServerQuotasResponse> quotaResult = serverQuotasResponseCollection.getServerQuotasResponses();
			logger.debug("Result = " + quotaResult);
			for (ServerQuotasResponse response : quotaResult)
			{
				ServerQuota quota = new ServerQuotaImpl();
				quota.setDiskQuota(response.getDiskQuota());
				quota.setDiskQuotaAvailable(response.getDiskQuotaAvailable());
				quota.setSessionQuota(response.getSessionQuota());
				quota.setSessionQuotaAvailable(response.getSessionQuotaAvailable());
				quota.setLastUpdated(new Date());
				logger.debug("disk quota:" + quota.getDiskQuota());
				logger.debug("disk quota available:" + quota.getDiskQuotaAvailable());
				logger.debug("session quota:" + quota.getSessionQuota());
				logger.debug("session quota available:" + quota.getSessionQuotaAvailable());

				//ServerQuotaService serviceQuotaImpl = new ServerQuotaService();
				/// serviceQuotaImpl.saveServerQuota(quota);
				serverQuotaDao.deleteServerQuota();
				serverQuotaDao.saveServerQuota(quota);
			}
		}
		catch (Exception ex)
		{
			logger.error("Error refreshing ServerQuota data: ", ex);
		}
	}
}