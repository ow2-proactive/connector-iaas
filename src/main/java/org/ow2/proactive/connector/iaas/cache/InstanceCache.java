package org.ow2.proactive.connector.iaas.cache;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ImmutableTable.Builder;

import lombok.Getter;


@Component
public class InstanceCache {
    @Getter
    private volatile ImmutableTable<String, String, Instance> supportedInstancePerInfrastructure;

    public InstanceCache() {
        this.supportedInstancePerInfrastructure = ImmutableTable.of();
    }

    public void registerInstance(Infrastructure infrastructure, Instance instance) {
        Builder<String, String, Instance> tempInstancesBuilder = cloneSupportedIntances();

        tempInstancesBuilder.put(infrastructure.getId(), instance.getId(), instance);
        supportedInstancePerInfrastructure = tempInstancesBuilder.build();
    }

    public void deleteInstance(Infrastructure infrastructure, Instance instance) {
        Builder<String, String, Instance> tempInstancesBuilder = ImmutableTable.builder();
        supportedInstancePerInfrastructure.cellSet().stream()
                .filter(cell -> !cell.getColumnKey().equals(instance.getId())).forEach(cell -> {
                    tempInstancesBuilder.put(cell);
                });
        supportedInstancePerInfrastructure = tempInstancesBuilder.build();
    }

    public void deleteInstancesByInfrastructure(Infrastructure infrastructure) {
        Builder<String, String, Instance> tempInstancesBuilder = ImmutableTable.builder();

        supportedInstancePerInfrastructure.cellSet().stream()
                .filter(cell -> !cell.getRowKey().equals(infrastructure.getId())).forEach(cell -> {
                    tempInstancesBuilder.put(cell);
                });

        supportedInstancePerInfrastructure = tempInstancesBuilder.build();
    }

    private Builder<String, String, Instance> cloneSupportedIntances() {
        return ImmutableTable.<String, String, Instance> builder().putAll(supportedInstancePerInfrastructure);
    }
}
