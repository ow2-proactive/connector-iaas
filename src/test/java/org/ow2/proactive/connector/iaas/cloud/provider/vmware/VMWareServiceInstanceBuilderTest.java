package org.ow2.proactive.connector.iaas.cloud.provider.vmware;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;


public class VMWareServiceInstanceBuilderTest {

    private VMWareServiceInstanceBuilder vmWareServiceInstance;

    @Before
    public void init() {
        this.vmWareServiceInstance = new VMWareServiceInstanceBuilder();
    }

    @Test(expected = RuntimeException.class)
    public void testBuildServiceInstanceConnectionException() {
        vmWareServiceInstance.buildServiceInstanceFromInfrastructure(InfrastructureFixture
                .getInfrastructure("id-vmware", "vmware", "http://127.0.0.1/sdk", "userName", "password"));

    }

}
