package org.ow2.proactive.connector.iaas.service;

import java.util.Optional;
import java.util.Set;

import javax.ws.rs.NotFoundException;

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
        return Optional.ofNullable(infrastructureService.getInfrastructure(infrastructureId))
                .map(infrastructure -> cloudManager.getAllImages(infrastructure))
                .orElseThrow(() -> new NotFoundException(
                    "infrastructure id  : " + infrastructureId + " does not exists"));
    }

}
