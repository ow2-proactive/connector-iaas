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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.InfrastructureCredentials;
import org.springframework.stereotype.Component;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
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

    private Map<Infrastructure,AzureTokenCredentials> generatedTokenPerInfra = new HashMap<>();

    // We store token, since they may be needed from the context of the infra (ex: pricing API)
    public AzureTokenCredentials getTokenfromInfra(Infrastructure infra) {
        if (!generatedTokenPerInfra.containsKey(infra))
            throw new RuntimeException("No stored token found for infrastructure " + infra.toString());
        return generatedTokenPerInfra.get(infra);
    }

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

        AzureEnvironment environment = getAzureEnvironment(infrastructure);

        AzureTokenCredentials credentials = new ApplicationTokenCredentials(clientId, domain, secret, environment);
        generatedTokenPerInfra.put(infrastructure,credentials);

        Azure azure;
        try {
            Azure.Authenticated authenticatedAzureService = Azure.configure()
                                                                 .withLogLevel(LogLevel.NONE)
                                                                 .authenticate(credentials);
            if (optionalSubscription.isPresent()) {
                azure = authenticatedAzureService.withSubscription(optionalSubscription.get());
            } else {
                try {
                    azure = authenticatedAzureService.withDefaultSubscription();
                } catch (CloudException | IOException e) {
                    throw new RuntimeException("ERROR trying to create Azure service with default subscription ID: " +
                                               infrastructure, e);
                }
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("ERROR something goes wrong while trying to create Azure service from infrastructure: " +
                                       infrastructure, throwable);
        }

        return azure;
    }

    private AzureEnvironment getAzureEnvironment(Infrastructure infrastructure) {
        // Get custom environment endpoints or get default public Azure environment
        Optional<String> optionalAuthenticationEndpoint = Optional.ofNullable(infrastructure.getAuthenticationEndpoint());
        Optional<String> optionalManagementEndpoint = Optional.ofNullable(infrastructure.getManagementEndpoint());
        Optional<String> optionalResourceManagerEndpoint = Optional.ofNullable(infrastructure.getResourceManagerEndpoint());
        Optional<String> optionalGraphEndpoint = Optional.ofNullable(infrastructure.getGraphEndpoint());
        if (optionalAuthenticationEndpoint.isPresent() && optionalManagementEndpoint.isPresent() &&
            optionalResourceManagerEndpoint.isPresent() && optionalGraphEndpoint.isPresent()) {
            HashMap<String, String> customAzureEnvironment = new HashMap<>();
            //customAzureEnvironment.put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.toString(), optionalAuthenticationEndpoint.get());
            customAzureEnvironment.put(AzureEnvironment.Endpoint.MANAGEMENT.toString(),
                                       optionalManagementEndpoint.get());
            customAzureEnvironment.put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.toString(),
                                       optionalResourceManagerEndpoint.get());
            customAzureEnvironment.put(AzureEnvironment.Endpoint.GRAPH.toString(), optionalGraphEndpoint.get());
            return new AzureEnvironment(customAzureEnvironment);
        } else {
            return AzureEnvironment.AZURE;
        }
    }
}
