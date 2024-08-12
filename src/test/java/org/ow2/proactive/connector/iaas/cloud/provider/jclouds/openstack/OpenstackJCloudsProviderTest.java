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

import static org.hamcrest.Matchers.is;
import static org.jclouds.compute.predicates.NodePredicates.runningInGroup;
import static org.jclouds.scriptbuilder.domain.Statements.exec;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.*;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.internal.ImageImpl;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.v2_0.domain.Resource;
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
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceScriptFixture;
import org.ow2.proactive.connector.iaas.model.*;
import org.ow2.proactive.connector.iaas.model.Image;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import jersey.repackaged.com.google.common.collect.Sets;


public class OpenstackJCloudsProviderTest {

    @InjectMocks
    private OpenstackJCloudsProvider jcloudsProvider;

    @Mock
    private JCloudsComputeServiceCache computeServiceCache;

    @Mock
    private ComputeService computeService;

    @Mock
    private Template template;

    @Mock
    private ComputeServiceContext contextMock;

    @Mock
    private NovaApi novaApi;

    @Mock
    private ServerApi serverApi;

    @Mock
    private ServerCreated serverCreated;

    @Mock
    private Server server;

    @Mock
    private Resource resource;

    @Mock
    private OpenstackUtil openstackUtil;

    @Mock
    private TagManager tagManager;

    private Tag connectorIaasTag = Tag.builder().key("connector-iaas-tag-key").value("default-value").build();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        jcloudsProvider.setVmUserLogin("user");
    }

    @Test
    public void testCreateInstance() throws RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               new InfrastructureScope("project",
                                                                                                       "admin"),
                                                                               "RegionOne",
                                                                               "3");

        Instance instance = InstanceFixture.getInstanceWithKeyName("instance-id",
                                                                   "instance-name",
                                                                   "image",
                                                                   "2",
                                                                   "512",
                                                                   "2",
                                                                   "network_id_1",
                                                                   "77.154.227.148",
                                                                   "1.0.0.2",
                                                                   "running");

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        when(computeService.getContext()).thenReturn(contextMock);

        when(contextMock.unwrapApi(NovaApi.class)).thenReturn(novaApi);

        when(openstackUtil.getInfrastructureRegion(infrastructure)).thenReturn("RegionOne");

        when(novaApi.getServerApi("RegionOne")).thenReturn(serverApi);

        when(novaApi.getSecurityGroupApi("RegionOne")).thenReturn(com.google.common.base.Optional.absent());

        when(serverApi.create(anyString(), anyString(), anyString(), anyObject())).thenReturn(serverCreated);

        when(serverCreated.getId()).thenReturn("1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");

        when(serverApi.get("1cde5a56-27a6-46ce-bdb7-8b01b8fe2592")).thenReturn(server);

        when(server.getId()).thenReturn("1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");

        when(server.getImage()).thenReturn(resource);

        when(resource.getName()).thenReturn("resource-name");

        when(server.getFlavor()).thenReturn(org.jclouds.openstack.v2_0.domain.Resource.builder()
                                                                                      .id("id")
                                                                                      .name("same name")
                                                                                      .build());

        when(server.getStatus()).thenReturn(org.jclouds.openstack.nova.v2_0.domain.Server.Status.BUILD);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(node.getHardware()).thenReturn(hardware);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        when(computeService.createNodesInGroup(instance.getTag(),
                                               Integer.parseInt(instance.getNumber()),
                                               template)).thenReturn(nodes);

        TemplateOptions templateOptions = mock(TemplateOptions.class);
        when(template.getOptions()).thenReturn(templateOptions);

        when(templateOptions.runAsRoot(true)).thenReturn(templateOptions);

        Set<Instance> created = jcloudsProvider.createInstance(infrastructure, instance);

        assertThat(created.size(), is(1));

        assertThat(created.stream().findAny().get().getId(), is("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592"));

    }

    @Test(expected = RuntimeException.class)
    public void testCreateInstanceWithFailure() throws NumberFormatException, RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        Instance instance = InstanceFixture.getInstance("instance-id",
                                                        "instance-name",
                                                        "image",
                                                        "2",
                                                        "512",
                                                        "2",
                                                        "77.154.227.148",
                                                        "1.0.0.2",
                                                        "running");

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        when(computeService.getContext()).thenReturn(contextMock);

        when(contextMock.unwrapApi(NovaApi.class)).thenReturn(novaApi);

        when(novaApi.getServerApi("RegionOne")).thenReturn(serverApi);

        when(serverApi.create(anyString(), anyString(), anyString(), anyObject())).thenReturn(serverCreated);

        // Tags
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(connectorIaasTag));

        Set nodesMetaData = Sets.newHashSet();
        NodeMetadataImpl nodeMetadataImpl = mock(NodeMetadataImpl.class);
        when(nodeMetadataImpl.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
        nodesMetaData.add(nodeMetadataImpl);

        when(serverApi.create(anyString(), anyString(), anyString(), anyObject())).thenThrow(new RuntimeException());

        jcloudsProvider.createInstance(infrastructure, instance);

    }

    @Test
    public void testDeleteInfrastructure() throws RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        Set nodes = Sets.newHashSet();
        NodeMetadataImpl node = mock(NodeMetadataImpl.class);
        when(node.getId()).thenReturn("someId");
        when(node.getName()).thenReturn("someName");
        Hardware hardware = mock(Hardware.class);
        when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
        when(hardware.getType()).thenReturn(ComputeType.HARDWARE);
        when(node.getHardware()).thenReturn(hardware);
        when(node.getStatus()).thenReturn(Status.RUNNING);
        nodes.add(node);
        when(computeService.listNodes()).thenReturn(nodes);

        jcloudsProvider.deleteInfrastructure(infrastructure);

        verify(computeServiceCache, times(1)).removeComputeService(infrastructure);

    }

    @Test
    public void testDeleteInstance() throws RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

        jcloudsProvider.deleteInstance(infrastructure, "instanceID");

        verify(computeService, times(1)).destroyNode("instanceID");

    }

    @Test
    public void testGetAllInfrastructureInstances() throws RunNodesException {

        Infrastructure infrastructure = InfrastructureFixture.getInfrastructure("id-aws",
                                                                               "aws",
                                                                               "endPoint",
                                                                               "userName",
                                                                               "password",
                                                                               null,
                                                                               null,
                                                                               null);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);

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

        Set<Instance> allNodes = jcloudsProvider.getAllInfrastructureInstances(infrastructure);

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
        OperatingSystem os = OperatingSystem.builder().description("An Operating System").build();
        when(image.getId()).thenReturn("someId");
        when(image.getName()).thenReturn("someName");
        when(image.getOperatingSystem()).thenReturn(os);
        when(image.getLocation()).thenReturn(new LocationBuilder().id("idLoc")
                                                                  .description("Adescription")
                                                                  .scope(LocationScope.REGION)
                                                                  .build());
        images.add(image);
        when(computeService.listImages()).thenReturn(images);

        Set<Image> allImages = jcloudsProvider.getAllImages(infrastructure);

        assertThat(allImages.iterator().next().getId(), is("someId"));
        assertThat(allImages.iterator().next().getName(), is("someName"));

    }

    @Test
    public void testGetAllImagesEmptySet() {
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
        when(computeService.listImages()).thenReturn(images);

        Set<Image> allImages = jcloudsProvider.getAllImages(infrastructure);

        assertThat(allImages.isEmpty(), is(true));

    }

    @Test
    public void testExecuteScriptOnInstanceId() throws RunNodesException {

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
                                                                                     InstanceScriptFixture.simpleInstanceScriptNoscripts());

        assertTrue(scriptResults.size() == 1);
        assertThat(scriptResults.get(0).getInstanceId(), is("instanceId"));
        assertThat(scriptResults.get(0).getOutput(), is("output"));
        assertThat(scriptResults.get(0).getError(), is("error"));

    }

    @Test
    public void testExecuteScriptOnInstanceTag() throws RunNodesException, RunScriptOnNodesException {

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
                                                                                      InstanceScriptFixture.simpleInstanceScriptNoscripts());

        assertThat(scriptResults.size(), is(0));

    }
}
