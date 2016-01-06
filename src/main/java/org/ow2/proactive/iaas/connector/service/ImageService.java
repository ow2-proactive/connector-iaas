package org.ow2.proactive.iaas.connector.service;

import java.util.Set;

import org.ow2.proactive.iaas.connector.cloud.CloudManager;
import org.ow2.proactive.iaas.connector.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ImageService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Image> getAllImages(String infrastructureName) {
        return cloudManager.getAllImages(infrastructureService.getInfrastructurebyName(infrastructureName));
    }

}
