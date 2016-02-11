package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.springframework.stereotype.Component;

import com.vmware.vim25.mo.ServiceInstance;


@Component
public class VMWareServiceInstanceBuilder {

    public ServiceInstance buildServiceInstanceFromInfrastructure(Infrastructure infrastructure) {

        try {
            return new ServiceInstance(new URL(infrastructure.getEndpoint()),
                infrastructure.getCredentials().getUsername(), infrastructure.getCredentials().getPassword(),
                true);
        } catch (RemoteException | MalformedURLException e) {
            throw new RuntimeException(
                "ERROR trying to create VMWare ServiceInstance with infrastructure  : " + infrastructure, e);
        }

    }

}
