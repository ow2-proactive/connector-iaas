package org.ow2.proactive.connector.iaas;

import javax.annotation.PreDestroy;

import org.ow2.proactive.connector.iaas.service.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ShutdownHandler {

    @Autowired
    private InfrastructureService infrastructureService;

    @PreDestroy
    public void removeAllInfrastructures() {
        infrastructureService.getAllSupportedInfrastructure().values().stream().forEach(infrastructure -> {
            infrastructureService.deleteInfrastructure(infrastructure);
        });
    }

}
