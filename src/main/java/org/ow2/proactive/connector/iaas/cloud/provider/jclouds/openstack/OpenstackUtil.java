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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.openstack;

import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class OpenstackUtil {

    public static final String OPENSTACK_TYPE = "openstack-nova";

    private static final String OPENSTACK_KEYSTONE_V3 = "3";

    @Value("${connector-iaas.openstack.scope-prefix:project}")
    protected String scopePrefix;

    @Value("${connector-iaas.openstack.default-project:admin}")
    protected String defaultProject;

    @Value("${connector-iaas.openstack.default-domain:Default}")
    protected String defaultDomain;

    @Value("${connector-iaas.openstack.default-region:RegionOne}")
    protected String defaultRegion;

    public void addCustomProperties(Infrastructure infrastructure, Properties properties) {

        if (infrastructure.getIdentityVersion().equals(OPENSTACK_KEYSTONE_V3)) {

            log.info("Configuring Openstack (Keystone v3) properties.");

            properties.put(KeystoneProperties.KEYSTONE_VERSION, OPENSTACK_KEYSTONE_V3);
            log.info("Using Openstack infrastructure with identity (Keystone) version: " + OPENSTACK_KEYSTONE_V3);

            if (infrastructure.getProject() != null) {
                properties.put(KeystoneProperties.SCOPE, scopePrefix + ":" + infrastructure.getProject());
                log.info("Using Openstack infrastructure with scope: " + scopePrefix + ":" +
                         infrastructure.getProject());
            } else {
                properties.put(KeystoneProperties.SCOPE, scopePrefix + ":" + defaultProject);
                log.info("Using default scope for Openstack infrastructure: " + scopePrefix + ":" + defaultProject);
            }
        }
    }

    public String getInfrastructureRegion(Infrastructure infrastructure) {

        String region;

        if (infrastructure.getRegion() != null) {
            region = infrastructure.getRegion();
            log.info("Using Openstack infrastructure with region: " + region);
        } else {
            region = defaultRegion;
            log.info("Using default region for Openstack infrastructure: " + region);
        }

        return region;
    }

    public void validateOpenstackInfrastructureParameters(Infrastructure infrastructure) {
        Validate.notNull(infrastructure.getType(), "Infrastructure parameter 'type' cannot be null");
        Validate.notBlank(infrastructure.getType(), "Infrastructure parameter 'type' cannot be empty");

        Validate.notNull(infrastructure.getEndpoint(), "Infrastructure parameter 'endpoint' cannot be null");
        Validate.notBlank(infrastructure.getEndpoint(), "Infrastructure parameter 'endpoint' cannot be empty");

        Validate.notNull(infrastructure.getCredentials().getDomain(),
                         "Infrastructure parameter 'user domain' (in credentials) cannot be null");
        Validate.notBlank(infrastructure.getCredentials().getDomain(),
                          "Infrastructure parameter 'user domain' (in credentials) cannot be empty");

        Validate.notNull(infrastructure.getCredentials().getUsername(),
                         "Infrastructure parameter 'user name' (in credentials) cannot be null");
        Validate.notBlank(infrastructure.getCredentials().getUsername(),
                          "Infrastructure parameter 'user name' (in credentials) cannot be empty");

        Validate.notNull(infrastructure.getCredentials().getPassword(),
                         "Infrastructure parameter 'user password' (in credentials) cannot be null");
        Validate.notBlank(infrastructure.getCredentials().getPassword(),
                          "Infrastructure parameter 'user password' (in credentials) cannot be empty");
    }

    public void validateOpenstackInstanceParameters(Instance instance) {

        Validate.notNull(instance.getTag(), "Instance parameter 'tag' cannot be null");
        Validate.notBlank(instance.getTag(), "Instance parameter 'tag' cannot be empty");

        Validate.notNull(instance.getImage(), "Instance parameter 'image' cannot be null");
        Validate.notBlank(instance.getImage(), "Instance parameter 'image' cannot be empty");

        Validate.notNull(instance.getHardware().getType(), "Instance parameter 'hardware type' cannot be null");
        Validate.notBlank(instance.getHardware().getType(), "Instance parameter 'hardware type' cannot be empty");

        Validate.notNull(instance.getNumber(), "Instance parameter 'number' cannot be null");
        Validate.notBlank(instance.getNumber(), "Instance parameter 'number type' cannot be empty");

        Validate.notNull(instance.getCredentials().getPublicKeyName(),
                         "Instance parameter 'publicKeyName' (in credentials) cannot be null");
        Validate.notBlank(instance.getCredentials().getPublicKeyName(),
                          "Instance parameter 'publicKeyName' (in credentials) cannot be empty");
    }
}
