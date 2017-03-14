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
package org.ow2.proactive.connector.iaas.cloud.provider.azure;

import java.io.IOException;
import java.util.Optional;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.InfrastructureCredentials;
import org.springframework.stereotype.Component;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;


/**
 * @author ActiveEon Team
 * @since 07/03/17
 */
@Component
public class AzureServiceBuilder {

    public Azure buildServiceFromInfrastructure(Infrastructure infrastructure) {

        // Get credentials
        InfrastructureCredentials infrastructureCredentials = Optional.ofNullable(infrastructure.getCredentials())
                                                                      .orElseThrow(() -> new RuntimeException("ERROR missing infrastructure credentials from: " +
                                                                                                              infrastructure));
        String clientId = Optional.ofNullable(infrastructureCredentials.getUsername())
                                  .orElseThrow(() -> new RuntimeException("ERROR missing username/clientId credential from: " +
                                                                          infrastructure));
        String domain = Optional.ofNullable(infrastructureCredentials.getDomain())
                                .orElseThrow(() -> new RuntimeException("ERROR missing domain/tenantId credential from: " +
                                                                        infrastructure));
        String secret = Optional.ofNullable(infrastructureCredentials.getPassword())
                                .orElseThrow(() -> new RuntimeException("ERROR missing password/secret credential from: " +
                                                                        infrastructure));
        Optional<String> optionalSubscription = Optional.ofNullable(infrastructureCredentials.getSubscriptionId());

        // Get custom environment endpoints or get default public Azure environment
        AzureEnvironment environment;
        Optional<String> optionalAuthenticationEndpoint = Optional.ofNullable(infrastructure.getAuthenticationEndpoint());
        Optional<String> optionalManagementEndpoint = Optional.ofNullable(infrastructure.getManagementEndpoint());
        Optional<String> optionalResourceManagerEndpoint = Optional.ofNullable(infrastructure.getResourceManagerEndpoint());
        Optional<String> optionalGraphEndpoint = Optional.ofNullable(infrastructure.getGraphEndpoint());
        if (optionalAuthenticationEndpoint.isPresent() && optionalManagementEndpoint.isPresent() &&
            optionalResourceManagerEndpoint.isPresent() && optionalGraphEndpoint.isPresent()) {
            environment = new AzureEnvironment(optionalAuthenticationEndpoint.get(),
                                               optionalManagementEndpoint.get(),
                                               optionalResourceManagerEndpoint.get(),
                                               optionalGraphEndpoint.get());
        } else {
            environment = AzureEnvironment.AZURE;
        }

        AzureTokenCredentials credentials = new ApplicationTokenCredentials(clientId, domain, secret, environment);
        Azure azure;
        try {
            if (optionalSubscription.isPresent()) {
                azure = Azure.configure()
                             .withLogLevel(LogLevel.NONE)
                             .authenticate(credentials)
                             .withSubscription(optionalSubscription.get());
            } else {
                azure = Azure.configure()
                             .withLogLevel(LogLevel.NONE)
                             .authenticate(credentials)
                             .withDefaultSubscription();
            }
        } catch (IOException e) {
            throw new RuntimeException("ERROR trying to create Azure service with default subscription ID: " +
                                       infrastructure, e);
        }

        return azure;
    }
}
