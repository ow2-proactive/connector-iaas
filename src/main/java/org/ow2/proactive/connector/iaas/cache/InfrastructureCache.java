package org.ow2.proactive.connector.iaas.cache;

import java.util.Map;
import java.util.stream.Collectors;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.stereotype.Component;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.Getter;


@Component
public class InfrastructureCache {
    @Getter
    private volatile ImmutableMap<String, Infrastructure> supportedInfrastructures;

    public InfrastructureCache() {
        this.supportedInfrastructures = ImmutableMap.of();
    }

    public void registerInfrastructure(Infrastructure infrastructure) {
        Map<String, Infrastructure> tempInfrastructures = cloneSupportedInfrastructures();

        tempInfrastructures.put(infrastructure.getId(), infrastructure);
        supportedInfrastructures = ImmutableMap.copyOf(tempInfrastructures);
    }

    public void deleteInfrastructure(Infrastructure infrastructure) {
        Map<String, Infrastructure> tempInfrastructures = cloneSupportedInfrastructures();

        tempInfrastructures.remove(infrastructure.getId());
        supportedInfrastructures = ImmutableMap.copyOf(tempInfrastructures);
    }

    private Map<String, Infrastructure> cloneSupportedInfrastructures() {
        return supportedInfrastructures.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
    }
}
