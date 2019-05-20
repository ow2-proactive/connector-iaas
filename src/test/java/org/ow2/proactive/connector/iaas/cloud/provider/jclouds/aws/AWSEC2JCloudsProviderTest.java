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
package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.aws;

import static org.hamcrest.Matchers.is;
import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.scriptbuilder.domain.Statements.exec;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ComputeType;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.ImageImpl;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsComputeServiceCache;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.Image;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.ScriptResult;
import org.ow2.proactive.connector.iaas.model.Tag;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.compute.VirtualMachineExtension;

import jersey.repackaged.com.google.common.collect.Sets;


public class AWSEC2JCloudsProviderTest {

    @InjectMocks
    private AWSEC2JCloudsProvider jcloudsProvider;

    @Mock
    private JCloudsComputeServiceCache computeServiceCache;

    @Mock
    private ComputeService computeService;

    @Mock
    private TemplateBuilder templateBuilder;

    @Mock
    private Template template;

    @Mock
    private AWSEC2TemplateOptions awsEC2TemplateOptions;

    private Map<String, VirtualMachineExtension> virtualMachineExtensionsMap;

    @Mock
    private TagManager tagManager;

    private Tag connectorIaasTag = Tag.builder().key("connector-iaas-tag-key").value("default-value").build();

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(jcloudsProvider, "vmUserLogin", "admin", String.class);
    }

    @Test
    public void testCreateInstance() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        when(computeService.templateBuilder()).thenReturn(templateBuilder);

        Instance instance = InstanceFixture.getInstance("instance-id",
                                                        "instance-name",
                                                        "image",
                                                        "2",
                                                        "512",
                                                        "2",
                                                        "77.154.227.148",
                                                        "1.0.0.2",
                                                        "running");

        when(templateBuilder.minRam(Integer.parseInt(instance.getHardware().getMinRam()))).thenReturn(templateBuilder);

        when(templateBuilder.minCores(Double.parseDouble(instance.getHardware()
                                                                 .getMinCores()))).thenReturn(templateBuilder);

        when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

        when(templateBuilder.build()).thenReturn(template);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        when(computeService.createNodesInGroup(instance.getTag(),
                                               Integer.parseInt(instance.getNumber()),
                                               template)).thenReturn(nodes);

        TemplateOptions templateOptions = mock(TemplateOptions.class);
        when(template.getOptions()).thenReturn(templateOptions);

        when(templateOptions.runAsRoot(true)).thenReturn(templateOptions);
        when(templateOptions.as(AWSEC2TemplateOptions.class)).thenReturn(awsEC2TemplateOptions);

        // Tags
        when(tagManager.retrieveAllTags(any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        Set<Instance> created = jcloudsProvider.createInstance(infratructure, instance);

        assertThat(created.size(), is(1));

        assertThat(created.stream().findAny().get().getId(), is("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592"));

        verify(computeService, times(1)).createNodesInGroup(instance.getTag(),
                                                            Integer.parseInt(instance.getNumber()),
                                                            template);

    }

    @Test
    public void testCreateInstanceWithSpotPrice() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        when(computeService.templateBuilder()).thenReturn(templateBuilder);

        Instance instance = InstanceFixture.getInstanceWithSpotPrice("instance-id",
                                                                     "instance-name",
                                                                     "image",
                                                                     "2",
                                                                     "512",
                                                                     "2",
                                                                     "77.154.227.148",
                                                                     "1.0.0.2",
                                                                     "running",
                                                                     "0.05f");

        when(templateBuilder.minRam(Integer.parseInt(instance.getHardware().getMinRam()))).thenReturn(templateBuilder);

        when(templateBuilder.minCores(Double.parseDouble(instance.getHardware()
                                                                 .getMinCores()))).thenReturn(templateBuilder);

        when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

        when(templateBuilder.build()).thenReturn(template);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        when(computeService.createNodesInGroup(instance.getTag(),
                                               Integer.parseInt(instance.getNumber()),
                                               template)).thenReturn(nodes);

        TemplateOptions templateOptions = mock(TemplateOptions.class);
        when(template.getOptions()).thenReturn(templateOptions);

        when(templateOptions.runAsRoot(true)).thenReturn(templateOptions);

        when(templateOptions.as(AWSEC2TemplateOptions.class)).thenReturn(awsEC2TemplateOptions);

        // Tags
        when(tagManager.retrieveAllTags(any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        Set<Instance> created = jcloudsProvider.createInstance(infratructure, instance);

        assertThat(created.size(), is(1));

        assertThat(created.stream().findAny().get().getId(), is("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592"));

        verify(computeService, times(1)).createNodesInGroup(instance.getTag(),
                                                            Integer.parseInt(instance.getNumber()),
                                                            template);

        verify(awsEC2TemplateOptions, times(1)).spotPrice(Float.valueOf(instance.getOptions().getSpotPrice()));

    }

    @Test(expected = RuntimeException.class)
    public void testCreateInstanceWithFailure() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        when(computeService.templateBuilder()).thenReturn(templateBuilder);

        Instance instance = InstanceFixture.getInstance("instance-id",
                                                        "instance-name",
                                                        "image",
                                                        "2",
                                                        "512",
                                                        "1",
                                                        "77.154.227.148",
                                                        "1.0.0.2",
                                                        "running");

        when(templateBuilder.minRam(Integer.parseInt(instance.getHardware().getMinRam()))).thenReturn(templateBuilder);

        when(templateBuilder.minCores(Double.parseDouble(instance.getHardware()
                                                                 .getMinCores()))).thenReturn(templateBuilder);

        when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

        when(templateBuilder.build()).thenReturn(template);

        Set nodesMetaData = Sets.newHashSet();
        NodeMetadataImpl nodeMetadataImpl = mock(NodeMetadataImpl.class);
        when(nodeMetadataImpl.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        nodesMetaData.add(nodeMetadataImpl);

        when(computeService.createNodesInGroup(instance.getTag(),
                                               Integer.parseInt(instance.getNumber()),
                                               template)).thenThrow(new RuntimeException());

        jcloudsProvider.createInstance(infratructure, instance);

    }

    @Test
    public void testDeleteInfrastructure() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("someId");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        jcloudsProvider.deleteInfrastructure(infratructure);

        verify(computeServiceCache, times(1)).removeComputeService(infratructure);

    }

    @Test
    public void testCreateInstanceWithSecurityGroup() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        when(computeService.templateBuilder()).thenReturn(templateBuilder);

        Instance instance = InstanceFixture.getInstanceWithSecurityGroup("instance-id",
                                                                         "instance-name",
                                                                         "image",
                                                                         "2",
                                                                         "512",
                                                                         "2",
                                                                         "77.154.227.148",
                                                                         "1.0.0.2",
                                                                         "running",
                                                                         "default");

        when(templateBuilder.minRam(Integer.parseInt(instance.getHardware().getMinRam()))).thenReturn(templateBuilder);

        when(templateBuilder.minCores(Double.parseDouble(instance.getHardware()
                                                                 .getMinCores()))).thenReturn(templateBuilder);

        when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

        when(templateBuilder.build()).thenReturn(template);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        when(computeService.createNodesInGroup(instance.getTag(),
                                               Integer.parseInt(instance.getNumber()),
                                               template)).thenReturn(nodes);

        TemplateOptions templateOptions = mock(TemplateOptions.class);
        when(template.getOptions()).thenReturn(templateOptions);

        when(templateOptions.runAsRoot(true)).thenReturn(templateOptions);

        when(templateOptions.as(AWSEC2TemplateOptions.class)).thenReturn(awsEC2TemplateOptions);

        Set<Instance> created = jcloudsProvider.createInstance(infratructure, instance);

        assertThat(created.size(), is(1));

        assertThat(created.stream().findAny().get().getId(), is("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592"));

        verify(computeService, times(1)).createNodesInGroup(instance.getTag(),
                                                            Integer.parseInt(instance.getNumber()),
                                                            template);

        verify(awsEC2TemplateOptions, times(1)).securityGroupIds(instance.getOptions().getSecurityGroupNames());

    }

    @Test
    public void testCreateInstanceWithSubnetId() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        when(computeService.templateBuilder()).thenReturn(templateBuilder);

        Instance instance = InstanceFixture.getInstanceWithSubnetId("instance-id",
                                                                    "instance-name",
                                                                    "image",
                                                                    "2",
                                                                    "512",
                                                                    "2",
                                                                    "77.154.227.148",
                                                                    "1.0.0.2",
                                                                    "running",
                                                                    "127.0.0.1");

        when(templateBuilder.minRam(Integer.parseInt(instance.getHardware().getMinRam()))).thenReturn(templateBuilder);

        when(templateBuilder.minCores(Double.parseDouble(instance.getHardware()
                                                                 .getMinCores()))).thenReturn(templateBuilder);

        when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

        when(templateBuilder.build()).thenReturn(template);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        when(computeService.createNodesInGroup(instance.getTag(),
                                               Integer.parseInt(instance.getNumber()),
                                               template)).thenReturn(nodes);

        TemplateOptions templateOptions = mock(TemplateOptions.class);
        when(template.getOptions()).thenReturn(templateOptions);

        when(templateOptions.runAsRoot(true)).thenReturn(templateOptions);

        when(templateOptions.as(AWSEC2TemplateOptions.class)).thenReturn(awsEC2TemplateOptions);

        Set<Instance> created = jcloudsProvider.createInstance(infratructure, instance);

        assertThat(created.size(), is(1));

        assertThat(created.stream().findAny().get().getId(), is("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592"));

        verify(computeService, times(1)).createNodesInGroup(instance.getTag(),
                                                            Integer.parseInt(instance.getNumber()),
                                                            template);

        verify(awsEC2TemplateOptions, times(1)).subnetId(instance.getOptions().getSubnetId());

    }

    @Test
    public void testDeleteInstance() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        jcloudsProvider.deleteInstance(infratructure, "instanceID");

        verify(computeService, times(1)).destroyNode("instanceID");

    }

    @Test
    public void testGetAllInfrastructureInstances() throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("someId");
        when(node.getName()).thenReturn(null);
        when(node.getHardware()).thenReturn(null);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        Set<Instance> allNodes = jcloudsProvider.getAllInfrastructureInstances(infratructure);

        assertThat(allNodes.iterator().next().getId(), is("someId"));
        assertThat(allNodes.iterator().next().getTag(), is(""));

    }

    @Test
    public void testGetAllInfrastructureInstancesMissingHardwareAndTag()
            throws NumberFormatException, RunNodesException {

        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("someId");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        Set<Instance> allNodes = jcloudsProvider.getAllInfrastructureInstances(infratructure);

        assertThat(allNodes.iterator().next().getId(), is("someId"));

    }

    @Test
    public void testGetAllImages() {
        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password",
                                                                                null,
                                                                                null,
                                                                                null);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        Set images = Sets.newHashSet();
        ImageImpl image = mock(ImageImpl.class);
        when(image.getId()).thenReturn("someId");
        when(image.getName()).thenReturn("someName");
        images.add(image);
        when(computeService.listImages()).thenReturn(images);

        Set<Image> allImages = jcloudsProvider.getAllImages(infrastructure);

        assertThat(allImages.iterator().next().getId(), is("someId"));
        assertThat(allImages.iterator().next().getName(), is("someName"));

    }

    @Test
    public void testGetAllImagesEmptySet() {
        Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

        Set images = Sets.newHashSet();
        when(computeService.listImages()).thenReturn(images);

        Set<Image> allImages = jcloudsProvider.getAllImages(infratructure);

        assertThat(allImages.isEmpty(), is(true));

    }

    @Test
    public void testExecuteScriptOnInstanceId() throws NumberFormatException, RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password",
                                                                                null,
                                                                                null,
                                                                                null);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        ExecResponse execResponse = mock(ExecResponse.class);

        when(execResponse.getOutput()).thenReturn("output");

        when(execResponse.getError()).thenReturn("error");

        when(computeService.runScriptOnNode(Mockito.anyString(),
                                            Mockito.anyString(),
                                            Mockito.any(RunScriptOptions.class))).thenReturn(execResponse);

        List<ScriptResult> scriptResults = jcloudsProvider.executeScriptOnInstanceId(infrastructure,
                                                                                     "instanceId",
                                                                                     InstanceScriptFixture.getInstanceScriptPrivateKey("privateKey"));

        assertTrue(scriptResults.size() == 1);
        assertThat(scriptResults.get(0).getInstanceId(), is("instanceId"));
        assertThat(scriptResults.get(0).getOutput(), is("output"));
        assertThat(scriptResults.get(0).getError(), is("error"));

    }

    @Test
    public void testExecuteScriptOnInstanceTag()
            throws NumberFormatException, RunNodesException, RunScriptOnNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                                "aws",
                                                                                "endPoint",
                                                                                "userName",
                                                                                "password",
                                                                                null,
                                                                                null,
                                                                                null);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        ExecResponse execResponse = mock(ExecResponse.class);

        when(execResponse.getOutput()).thenReturn("output");

        when(execResponse.getError()).thenReturn("error");

        String allScriptsToExecute = new ScriptBuilder().addStatement(exec("ls -lrt")).render(OsFamily.UNIX);

        when(computeService.runScriptOnNodesMatching(runningInGroup("instanceTag"),
                                                     allScriptsToExecute)).thenReturn(Maps.newHashMap());

        List<ScriptResult> scriptResults = jcloudsProvider.executeScriptOnInstanceTag(infrastructure,
                                                                                      "instanceTag",
                                                                                      InstanceScriptFixture.getInstanceScriptPrivateKey("privateKey"));

        assertThat(scriptResults.size(), is(0));

    }
}
