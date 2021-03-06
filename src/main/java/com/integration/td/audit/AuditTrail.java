package com.integration.td.audit;

import com.integration.td.constants.DataSource;


//import org.apache.log4j.Logger;

public class AuditTrail {

    //private static final Logger LOGGER = Logger.getLogger(AuditTrail.class.getName());
    private static AuditTrail auditLog = null;

    public static AuditTrail getAuditLogInstance() {
        if (auditLog == null) {
            synchronized (AuditTrail.class) {
                if (auditLog == null) {
                    auditLog = new AuditTrail();
                }
            }
        }
        return auditLog;
    }
    
    /**
     * 
     * @param createdBy
     * @param createdDate
     * @param actionDescription
     * @param actionResult
     * @param userName
     * @param tenantId
     * @param requestPayload
     * @param responsePayload
     */
    public synchronized  void updateAuditTrial(String createdBy, String createdDate, String actionDescription, String actionResult, String userName, String tenantId, String requestPayload, String responsePayload, DataSource dataSource) {
        AuditTrailDAO auditTrailDAO = new AuditTrailDAO();
        AuditLogs auditlogs = new AuditLogs(createdBy, createdDate, actionDescription, actionResult, userName, tenantId, requestPayload, responsePayload);
        try {
            auditTrailDAO.addAuditTrail(auditlogs,dataSource);
        } catch (Exception dbse) {
           // LOGGER.error("Exception add audit trails on " + actionDescription + " :" + dbse);
        }
    }
}

