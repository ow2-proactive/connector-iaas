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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.LocationScope;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.CloudProvider;
import org.ow2.proactive.connector.iaas.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public abstract class JCloudsProvider implements CloudProvider {

    @Autowired
    private JCloudsComputeServiceCache jCloudsComputeServiceCache;

    @Autowired
    private TagManager tagManager;

    /**
     * By default, the login that will be used to connect to the instances
     * and launch the script will be 'admin'. This default can be overriden
     * in the application.properties file.
     */
    @Getter
    @Setter
    @Value("${connector-iaas.vm-user-login:admin}")
    private String vmUserLogin;

    @Value("${connector-iaas.pricing-repo}")
    private String pricingRepo;

    protected abstract RunScriptOptions getRunScriptOptionsWithCredentials(InstanceCredentials credentials);

    public Set<String> listAvailableRegions(Infrastructure infrastructure) {
        return getComputeServiceFromInfastructure(infrastructure).listAssignableLocations()
                                                                 .parallelStream()
                                                                 .filter(location -> location.getScope()
                                                                                             .equals(LocationScope.REGION))
                                                                 .map(loc -> loc.getId())
                                                                 .collect(Collectors.toSet());
    }

    @Override
    public void deleteInstance(Infrastructure infrastructure, String instanceId) {
        getComputeServiceFromInfastructure(infrastructure).destroyNode(instanceId);
        log.info("Instance deleted successfully: " + instanceId);
    }

    @Override
    public Set<Instance> getAllInfrastructureInstances(Infrastructure infrastructure) {
        return createInstancesFromNodes(getAllNodes(infrastructure));
    }

    private Set<Instance> createInstancesFromNodes(Set<? extends ComputeMetadata> nodes) {
        return nodes.stream()
                    .map(computeMetadata -> (NodeMetadataImpl) computeMetadata)
                    .map(this::createInstanceFromNode)
                    .collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> getCreatedInfrastructureInstances(Infrastructure infrastructure) {
        Tag connectorIaasTag = tagManager.getConnectorIaasTag();
        Tag infrastructureIdTag = tagManager.getInfrastructureIdTag();
        return createInstancesFromNodes(getAllNodes(infrastructure).stream()
                                                                   .filter(node -> node.getUserMetadata()
                                                                                       .containsKey(connectorIaasTag.getKey()) &&
                                                                                   node.getUserMetadata()
                                                                                       .get(connectorIaasTag.getKey())
                                                                                       .equals(connectorIaasTag.getValue()) &&
                                                                                   node.getUserMetadata()
                                                                                       .containsKey(infrastructureIdTag.getKey()) &&
                                                                                   node.getUserMetadata()
                                                                                       .get(infrastructureIdTag.getKey())
                                                                                       .equals(infrastructure.getId()))
                                                                   .collect(Collectors.toSet()));
    }

    private Set<? extends ComputeMetadata> getAllNodes(Infrastructure infrastructure) {
        return getComputeServiceFromInfastructure(infrastructure).listNodes();
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceId(Infrastructure infrastructure, String instanceId,
            InstanceScript instanceScript) {
        ExecResponse execResponse;
        RunScriptOptions runScriptOptions = null;
        try {
            String scriptToExecuteString = buildScriptToExecuteString(instanceScript);
            runScriptOptions = buildScriptOptionsWithInstanceId(instanceScript, instanceId, infrastructure);
            execResponse = getComputeServiceFromInfastructure(infrastructure).runScriptOnNode(instanceId,
                                                                                              scriptToExecuteString,
                                                                                              runScriptOptions);
        } catch (Exception e) {
            log.error("Script cannot be run on instance with id: " + instanceId + ". RunScriptOptions=" +
                      runScriptOptions, e);
            throw new RuntimeException("Script cannot be run on instance with id: " + instanceId, e);
        }

        return Lists.newArrayList(new ScriptResult(instanceId, execResponse.getOutput(), execResponse.getError()));
    }

    @Override
    public List<ScriptResult> executeScriptOnInstanceTag(Infrastructure infrastructure, String instanceTag,
            InstanceScript instanceScript) {

        Map<? extends NodeMetadata, ExecResponse> execResponses;
        RunScriptOptions runScriptOptions = null;
        try {
            String scriptToExecuteString = buildScriptToExecuteString(instanceScript);
            runScriptOptions = buildScriptOptionsWithInstanceTag(instanceScript, instanceTag, infrastructure);
            execResponses = getComputeServiceFromInfastructure(infrastructure).runScriptOnNodesMatching(runningInGroup(instanceTag),
                                                                                                        scriptToExecuteString,
                                                                                                        runScriptOptions);
        } catch (Exception e) {
            log.error("Script cannot be run on instance with tag: " + instanceTag + ". RunScriptOptions=" +
                      runScriptOptions, e);
            throw new RuntimeException("Script cannot be run on instances with tag: " + instanceTag, e);
        }

        return execResponses.entrySet()
                            .stream()
                            .map(entry -> new ScriptResult(entry.getKey().getId(),
                                                           entry.getValue().getOutput(),
                                                           entry.getValue().getError()))
                            .collect(Collectors.toList());

    }

    @Override
    public Set<Image> getAllImages(Infrastructure infrastructure) {
        Set<? extends org.jclouds.compute.domain.Image> images = getComputeServiceFromInfastructure(infrastructure).listImages();
        log.info(String.format("Found %d images", images.stream().count()));
        return images.stream()
                     .map(it -> Image.builder()
                                     .id(it.getId())
                                     .name(it.getName())
                                     .location(it.getLocation().getId())
                                     .operatingSystem(OperatingSystem.builder()
                                                                     .arch(it.getOperatingSystem().getArch())
                                                                     .description(it.getOperatingSystem()
                                                                                    .getDescription())
                                                                     .family(Optional.ofNullable(it.getOperatingSystem()
                                                                                                   .getFamily())
                                                                                     .orElse(org.jclouds.compute.domain.OsFamily.UNRECOGNIZED)
                                                                                     .value())
                                                                     .is64Bit(it.getOperatingSystem().is64Bit())
                                                                     .build())
                                     .build())
                     .collect(Collectors.toSet());

    }

    @Override
    public Set<Hardware> getAllHardwares(Infrastructure infrastructure) {
        return getHardware(infrastructure, Optional.empty());
    }

    public Set<Hardware> getRegionSpecificHardware(Infrastructure infrastructure, String region) {
        return getHardware(infrastructure, Optional.of(region));
    }

    public Set<Hardware> getHardware(Infrastructure infrastructure, Optional<String> region) {
        return getComputeServiceFromInfastructure(infrastructure).listHardwareProfiles()
                                                                 .parallelStream()
                                                                 .filter(hw -> !region.isPresent() ||
                                                                               hw.getLocation()
                                                                                 .getId()
                                                                                 .contains(region.get()))
                                                                 .map(hw -> Hardware.builder()
                                                                                    .minCores("" + hw.getProcessors()
                                                                                                     .stream()
                                                                                                     .mapToDouble(Processor::getCores)
                                                                                                     .sum())
                                                                                    .minRam("" + hw.getRam())
                                                                                    .type(hw.getId())
                                                                                    .minFreq("" + hw.getProcessors()
                                                                                                    .stream()
                                                                                                    .mapToDouble(Processor::getSpeed)
                                                                                                    .sum() *
                                                                                                  1024)
                                                                                    .build())
                                                                 .collect(Collectors.toSet());
    }

    @Override
    public void deleteInfrastructure(Infrastructure infrastructure) {
        jCloudsComputeServiceCache.removeComputeService(infrastructure);
        log.info("Infrastructure deleted successfully: " + infrastructure.getId());
    }

    protected final Instance createInstanceFromNode(NodeMetadataImpl nodeMetadataImpl) {
        return Instance.builder()
                       .id(nodeMetadataImpl.getId())
                       .tag(Optional.ofNullable(nodeMetadataImpl.getName()).orElse(""))
                       .image(nodeMetadataImpl.getImageId())
                       .number("1")
                       .hardware(Optional.ofNullable(nodeMetadataImpl.getHardware())
                                         .map(hardware -> Hardware.builder()
                                                                  .minRam(String.valueOf(nodeMetadataImpl.getHardware()
                                                                                                         .getRam()))
                                                                  .minCores(String.valueOf(nodeMetadataImpl.getHardware()
                                                                                                           .getProcessors()
                                                                                                           .stream()
                                                                                                           .mapToDouble(Processor::getCores)
                                                                                                           .sum()))
                                                                  .type(nodeMetadataImpl.getHardware().getType().name())
                                                                  .build())
                                         .orElse(new Hardware()))
                       .network(Network.builder()
                                       .publicAddresses(Lists.newArrayList(nodeMetadataImpl.getPublicAddresses()))
                                       .privateAddresses(Lists.newArrayList(nodeMetadataImpl.getPrivateAddresses()))
                                       .build())
                       .status(nodeMetadataImpl.getStatus().name())
                       .build();
    }

    protected ComputeService getComputeServiceFromInfastructure(Infrastructure infrastructure) {
        return jCloudsComputeServiceCache.getComputeService(infrastructure);
    }

    protected String buildScriptToExecuteString(InstanceScript instanceScript) {

        return Optional.ofNullable(instanceScript).map(scriptToRun -> {
            ScriptBuilder scriptBuilder = new ScriptBuilder();
            Arrays.stream(scriptToRun.getScripts()).forEachOrdered(script -> scriptBuilder.addStatement(exec(script)));
            return scriptBuilder.render(OsFamily.UNIX);
        }).orElse("");

    }

    private RunScriptOptions buildScriptOptionsWithInstanceId(InstanceScript instanceScript, String instanceId,
            Infrastructure infrastructure) {
        return Optional.ofNullable(instanceScript.getCredentials())
                       .map(this::getRunScriptOptionsWithCredentials)
                       .orElseGet(() -> getDefaultRunScriptOptionsUsingInstanceId(instanceId, infrastructure));
    }

    private RunScriptOptions buildScriptOptionsWithInstanceTag(InstanceScript instanceScript, String instanceTag,
            Infrastructure infrastructure) {
        return Optional.ofNullable(instanceScript.getCredentials())
                       .map(this::getRunScriptOptionsWithCredentials)
                       .orElseGet(() -> getDefaultRunScriptOptionsUsingInstanceTag(instanceTag, infrastructure));
    }

    /**
     * @return scripts options, based on the instance identifier, that can be
     * specific to concrete implementations
     */
    protected RunScriptOptions getDefaultRunScriptOptionsUsingInstanceId(String instanceId,
            Infrastructure infrastructure) {
        return RunScriptOptions.NONE;
    }

    /**
     * @return scripts options, based on instances tag, that can be specific
     * to concrete implementations
     */
    protected RunScriptOptions getDefaultRunScriptOptionsUsingInstanceTag(String instanceTag,
            Infrastructure infrastructure) {
        return RunScriptOptions.NONE;
    }

    public PagedNodeCandidates getNodeCandidate(Infrastructure infra, String region, String imageReq, String token) {
        String type = getType();
        // In this method, we will return a list of node candidates for JCloud infrastructure, that do not have their own pricing driver.
        // To make this driver generic accross cloud provider, we consider two types of providers:
        // 1 - Known and paid cloud providers: those have their pricing encoded in JSON files, located in pricingRepo folder,
        // 2 - Unknown/private clouds: those do not have pricing files. The prices of their instances is assumed to be free.
        try {
            String fileTag = new String(java.security.MessageDigest.getInstance("SHA-1")
                                                                   .digest((type + infra.getRegion() +
                                                                            infra.getAuthenticationEndpoint()).getBytes()));
            File pricingFile = new File(this.pricingRepo + File.pathSeparator + fileTag + ".json");
            // We will use the getAllImage() API to identify which VM image are relevant.
            Set<Image> resultImages = this.getAllImages(infra)
                                          .parallelStream()
                                          .filter(image -> image.getLocation().equals(region))
                                          .filter(img -> img.getName().contains(imageReq))
                                          .collect(Collectors.toSet());
            Set<Hardware> resultHardware = this.getRegionSpecificHardware(infra, region);
            if (pricingFile.exists()) {
                // If the file exist, we are in the case of a paid cloud
                return PagedNodeCandidates.builder()
                                          .nextToken("")
                                          .nodeCandidates(getPaidNodeCandidate(infra,
                                                                               region,
                                                                               imageReq,
                                                                               pricingFile,
                                                                               resultImages,
                                                                               resultHardware))
                                          .build();
            } else {
                // Else, we assume this is a private one with no cost.
                return PagedNodeCandidates.builder()
                                          .nextToken("")
                                          .nodeCandidates(getFreeNodeCandidate(infra,
                                                                               region,
                                                                               imageReq,
                                                                               resultImages,
                                                                               resultHardware))
                                          .build();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to proceed with the digest: " + e.getLocalizedMessage());
        } catch (IOException e) {
            throw new RuntimeException("Unable to open the pricing file: " + e.getLocalizedMessage());
        }
    }

    private Set<NodeCandidate> getFreeNodeCandidate(Infrastructure infra, String region, String imageReq,
            Set<Image> resultImages, Set<Hardware> resultHardware) {
        return resultHardware.stream()
                             .map(hw -> resultImages.parallelStream()
                                                    .map(image -> NodeCandidate.builder()
                                                                               .region(region)
                                                                               .cloud(infra.getType())
                                                                               .hw(hw)
                                                                               .img(image)
                                                                               .price(0)
                                                                               .build())
                                                    .collect(Collectors.toSet()))
                             .reduce((nc1, nc2) -> {
                                 nc1.addAll(nc2);
                                 return nc1;
                             })
                             .orElse(new HashSet<>());
    }

    private Set<NodeCandidate> getPaidNodeCandidate(Infrastructure infra, String region, String imageReq,
            File pricingFile, Set<Image> resultImages, Set<Hardware> resultHardware) throws IOException {
        // The structure of the JSON file is an array of JSON containing (i) hardwareId, (ii) imageId and (iii) cost. Each file is specific to a cloud and a region.
        final String HARDWARE_ID = "hardwareId";
        final String IMAGE_ID = "imageId";
        final String COST = "cost";
        // We constitute a list of hardwareId of the cloud region, to be seek into later on
        List<String> listOfhardwareId = resultHardware.parallelStream()
                                                      .map(Hardware::getType)
                                                      .collect(Collectors.toList());
        // We constitute multiple list of Image ID beacause image specification diverges on the Cloud infra provide.
        List<String> listOfAvailableImageId = resultImages.parallelStream()
                                                          .map(Image::getId)
                                                          .distinct()
                                                          .collect(Collectors.toList());
        List<String> listOfAvailableImageName = resultImages.parallelStream()
                                                            .map(Image::getName)
                                                            .distinct()
                                                            .filter(Objects::nonNull)
                                                            .filter(s -> !s.isEmpty())
                                                            .collect(Collectors.toList());
        //  we read and proces the pricingfile.
        String fileContent = Files.readAllLines(pricingFile.toPath()).stream().collect(Collectors.joining());
        JSONArray princingArrayOfaRegion = new JSONArray(fileContent);
        // JSONArray cannot be stream. We will use this constrain to enforce the JSONObject typing in the JSONArray
        List<JSONObject> checkedPricingArray = new ArrayList<>();
        for (Object entree : princingArrayOfaRegion) {
            checkedPricingArray.add((JSONObject) entree);
        }
        // We determine the pricing to be analyzed in the region, based on which Hardware and image are available from user account.
        List<JSONObject> releavantPricing = checkedPricingArray.parallelStream()
                                                               .filter(Objects::nonNull)
                                                               .filter(jo -> jo.has(HARDWARE_ID))
                                                               .filter(jo -> jo.has(IMAGE_ID))
                                                               .filter(jo -> listOfhardwareId.contains(jo.get(HARDWARE_ID)))
                                                               .filter(jo -> listOfAvailableImageId.contains(jo.getString(IMAGE_ID)) ||
                                                                             listOfAvailableImageName.stream()
                                                                                                     .filter(s -> s.contains(jo.getString(IMAGE_ID)))
                                                                                                     .count() > 0)
                                                               .collect(Collectors.toList());
        // We select pricing that are match the search criteria
        List<JSONObject> selectedPricing = releavantPricing.parallelStream()
                                                           .filter(jo -> jo.getString(IMAGE_ID).contains(imageReq))
                                                           .collect(Collectors.toList());
        // We build the nodeCandidate based on these
        Set<NodeCandidate> result = selectedPricing.parallelStream().map(jo -> {
            return NodeCandidate.builder()
                                .cloud(infra.getType())
                                .region(region)
                                .hw(resultHardware.stream()
                                                  .filter(hw -> hw.getType().contains(jo.getString(HARDWARE_ID)))
                                                  .findFirst()
                                                  .get())
                                .img(resultImages.stream().filter(img -> {
                                    String imageId = jo.getString(IMAGE_ID);
                                    return img.getName().contains(imageId) || img.getId().contains(imageId);
                                }).findFirst().get())
                                .price(jo.optDouble(COST))
                                .build();
        }).collect(Collectors.toSet());
        return result;
    }
}
