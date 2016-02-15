package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.vim25.mo.ServiceInstance;


@Component
public class VMWareServiceInstanceCache {

    @Autowired
    private VMWareServiceInstanceBuilder ServiceInstanceBuilder;

    private Map<Infrastructure, ServiceInstance> serviceInstanceCache;

    public VMWareServiceInstanceCache() {
        serviceInstanceCache = new ConcurrentHashMap<Infrastructure, ServiceInstance>();
    }

    public ServiceInstance getServiceInstance(Infrastructure infrastructure) {
        return buildComputeService.apply(infrastructure);
    }

    public void removeServiceInstance(Infrastructure infrastructure) {
        serviceInstanceCache.remove(infrastructure);
    }

    private Function<Infrastructure, ServiceInstance> buildComputeService = memoise(infrastructure -> {
        return ServiceInstanceBuilder.buildServiceInstanceFromInfrastructure(infrastructure);
    });

    private Function<Infrastructure, ServiceInstance> memoise(Function<Infrastructure, ServiceInstance> fn) {
        return (a) -> serviceInstanceCache.computeIfAbsent(a, fn);
    }

}
