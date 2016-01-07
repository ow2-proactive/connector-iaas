package org.ow2.proactive.connector.iaas.service;

import java.util.Set;

import org.ow2.proactive.connector.iaas.cloud.CloudManager;
import org.ow2.proactive.connector.iaas.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ImageService {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private CloudManager cloudManager;

    public Set<Image> getAllImages(String infrastructureId) {
        return cloudManager.getAllImages(infrastructureService.getInfrastructure(infrastructureId));
    }

}
