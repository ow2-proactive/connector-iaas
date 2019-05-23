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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.google;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.googlecomputeengine.compute.options.GoogleComputeEngineTemplateOptions;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.InstanceCredentials;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class GCEJCloudsProvider extends JCloudsProvider {

    @Getter
    private final String type = "google-compute-engine";

    @Autowired
    private TagManager tagManager;

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {
        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        TemplateBuilder templateBuilder = computeService.templateBuilder();

        Optional.ofNullable(instance.getHardware())
                .map(Hardware::getMinRam)
                .filter(StringUtils::isNotBlank)
                .map(Integer::parseInt)
                .ifPresent(templateBuilder::minRam);

        Optional.ofNullable(instance.getHardware())
                .map(Hardware::getMinCores)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .ifPresent(templateBuilder::minCores);

        Optional.ofNullable(instance.getImage())
                .filter(StringUtils::isNotBlank)
                .ifPresent(templateBuilder::imageNameMatches);

        Optional.ofNullable(instance.getOptions()).map(Options::getRegion).ifPresent(templateBuilder::locationId);

        Template template = templateBuilder.build();

        GoogleComputeEngineTemplateOptions gceTemplateOptions = template.getOptions()
                                                                        .as(GoogleComputeEngineTemplateOptions.class);

        // Add the tag key connector-iaas to mark the instance as a createdInstance managed by ProActive
        gceTemplateOptions.userMetadata(tagManager.retrieveAllTags(infrastructure.getId(), instance.getOptions())
                                                  .stream()
                                                  .collect(Collectors.toMap(Tag::getKey, Tag::getValue)));

        Optional.ofNullable(instance.getCredentials())
                .map(InstanceCredentials::getUsername)
                .filter(StringUtils::isNotBlank)
                .ifPresent(gceTemplateOptions::overrideLoginUser);

        Optional.ofNullable(instance.getCredentials())
                .map(InstanceCredentials::getPublicKey)
                .filter(StringUtils::isNotBlank)
                .ifPresent(gceTemplateOptions::authorizePublicKey);

        Optional.ofNullable(instance.getCredentials())
                .map(InstanceCredentials::getPrivateKey)
                .filter(StringUtils::isNotBlank)
                .ifPresent(gceTemplateOptions::overrideLoginPrivateKey);

        Optional.ofNullable(instance.getInitScript())
                .filter(Objects::nonNull)
                .map(this::buildScriptToExecuteString)
                .ifPresent(gceTemplateOptions::runScript);

        log.debug(String.format("template for createNodesInGroup(%s): %s", instance.getTag(), template));

        try {
            Set<Instance> createdInstances = computeService.createNodesInGroup(instance.getTag(),
                                                                               Integer.parseInt(instance.getNumber()),
                                                                               template)
                                                           .stream()
                                                           .map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                                                           .peek(log::debug)
                                                           .map(this::createInstanceFromNode)
                                                           .collect(Collectors.toSet());
            log.info("Created Instances: " + createdInstances);
            return createdInstances;
        } catch (RunNodesException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    public void removeInstancePublicIp(Infrastructure infrastructure, String instanceId, String desiredIp) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    public SimpleImmutableEntry<String, String> createKeyPair(Infrastructure infrastructure, Instance instance) {
        throw new NotImplementedException("This method is not yet implemented.");
    }

    @Override
    protected RunScriptOptions getRunScriptOptionsWithCredentials(InstanceCredentials credentials) {
        // retrieve the passed username or read the default username from the property file
        String username = Optional.ofNullable(credentials.getUsername())
                                  .filter(StringUtils::isNotBlank)
                                  .orElse(getVmUserLogin());
        log.info("Credentials used to execute script on instance: [username=" + username + ", privateKey=" +
                 credentials.getPrivateKey() + "]");
        return RunScriptOptions.Builder.runAsRoot(false)
                                       .overrideLoginUser(username)
                                       .overrideLoginPrivateKey(credentials.getPrivateKey());
    }

}
