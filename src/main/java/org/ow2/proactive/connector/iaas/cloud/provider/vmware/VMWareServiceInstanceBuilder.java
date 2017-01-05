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
                                       infrastructure.getCredentials().getUsername(),
                                       infrastructure.getCredentials().getPassword(),
                                       true);
        } catch (RemoteException | MalformedURLException e) {
            throw new RuntimeException("ERROR trying to create VMWare ServiceInstance with infrastructure  : " +
                                       infrastructure, e);
        }

    }

}
