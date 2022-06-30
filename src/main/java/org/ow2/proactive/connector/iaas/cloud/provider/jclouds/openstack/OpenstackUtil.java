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
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class OpenstackUtil {

    public static final String OPENSTACK_TYPE = "openstack-nova";

    private static final String OPENSTACK_KEYSTONE_V3 = "3";

    private static final String SEPARATOR = ":";

    public void addCustomProperties(Infrastructure infrastructure, Properties properties) {

        if (infrastructure.getIdentityVersion().equals(OPENSTACK_KEYSTONE_V3)) {

            log.info("Configuring Openstack (Keystone v3) properties.");

            properties.put(KeystoneProperties.KEYSTONE_VERSION, OPENSTACK_KEYSTONE_V3);
            log.info("Using Openstack infrastructure with identity (Keystone) version: " + OPENSTACK_KEYSTONE_V3);

            Validate.notNull(infrastructure.getScope(), "Infrastructure parameter 'scope' cannot be null");

            Validate.notNull(infrastructure.getScope().getPrefix(),
                             "Infrastructure parameter 'scope prefix' cannot be null");
            Validate.notBlank(infrastructure.getScope().getPrefix(),
                              "Infrastructure parameter 'scope prefix' cannot be empty");

            Validate.notNull(infrastructure.getScope().getValue(),
                             "Infrastructure parameter 'scope value' cannot be null");
            Validate.notBlank(infrastructure.getScope().getValue(),
                              "Infrastructure parameter 'scope prefix' cannot be empty");

            String prefix = infrastructure.getScope().getPrefix();
            String value = infrastructure.getScope().getValue();

            properties.put(KeystoneProperties.SCOPE, prefix + SEPARATOR + value);

            if (infrastructure.getCredentials().getDomain() != null &&
                !infrastructure.getCredentials().getDomain().isEmpty()) {
                properties.put(KeystoneProperties.PROJECT_DOMAIN_NAME, infrastructure.getCredentials().getDomain());
            }
            log.info("Using Openstack infrastructure with scope: " + prefix + SEPARATOR + value);

        }
    }

    public String getInfrastructureRegion(Infrastructure infrastructure) {

        String region;

        Validate.notNull(infrastructure.getRegion(), "Infrastructure parameter 'region' cannot be null");
        Validate.notBlank(infrastructure.getRegion(), "Infrastructure parameter 'region' cannot be empty");

        region = infrastructure.getRegion();
        log.info("Using Openstack infrastructure with region: " + region);

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
    }

    /**
     * @return the Operating system family for OpenStack images either by default from the metadata collected by Jclouds
     * or from the user metadata if the OSFamily is set to UNRECOGNIZED. Created to patch the limitation introduced by
     * Jclouds for OpenStack images (not taking the distro_family into account).
     */
    public static OsFamily getOpenStackOSFamily(Image image) {
        if (image.getOperatingSystem().getFamily() == OsFamily.UNRECOGNIZED) {
            if (image.getUserMetadata().containsKey("distro_family")) {
                return OsFamily.fromValue(image.getUserMetadata().get("distro_family"));
            } else {
                log.warn("the image \"{}\" with ID \"{}\" is added with no recognized operating system family",
                         image.getName(),
                         image.getId());
                return OsFamily.UNRECOGNIZED;
            }
        } else {
            return image.getOperatingSystem().getFamily();
        }
    }
}
