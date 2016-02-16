package org.ow2.proactive.connector.iaas;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ShutdownHandler {

    private final Logger logger = Logger.getLogger(ShutdownHandler.class);

    @Autowired
    private InfrastructureService infrastructureService;

    @PreDestroy
    public synchronized void removeAllInfrastructures() {
        infrastructureService.getAllSupportedInfrastructure().values().stream()
                .filter(infrastructure -> infrastructure.isToBeRemovedOnShutdown())
                .forEach(infrastructure -> {
                    try {
                        infrastructureService.deleteInfrastructure(infrastructure);
                    } catch (Exception e) {
                        logger.error(
                                "Shutdown ERROR when trying to delete infrastructure : " + infrastructure, e);
                    }
                });
    }

}
