/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
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
        vmWareServiceInstance.buildServiceInstanceFromInfrastructure(InfrastructureFixture.getInfrastructure("id-vmware",
                                                                                                             "vmware",
                                                                                                             "http://127.0.0.1/sdk",
                                                                                                             "userName",
                                                                                                             "password"));

    }

}
