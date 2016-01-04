package org.ow2.proactive.iaas.connector.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.ComputeMetadataImpl;
import org.jclouds.compute.domain.internal.ImageImpl;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.iaas.connector.cache.ComputeServiceCache;
import org.ow2.proactive.iaas.connector.fixtures.InfrastructureFixture;
import org.ow2.proactive.iaas.connector.fixtures.InstanceFixture;
import org.ow2.proactive.iaas.connector.model.Infrastructure;
import org.ow2.proactive.iaas.connector.model.Instance;

import com.beust.jcommander.internal.Lists;

import jersey.repackaged.com.google.common.collect.Sets;

public class InstanceServiceTest {

	@InjectMocks
	private InstanceService instanceService;

	@Mock
	private InfrastructureService infrastructureService;

	@Mock
	private ComputeServiceCache computeServiceCache;

	@Mock
	private ComputeService computeService;

	@Mock
	private TemplateBuilder templateBuilder;

	@Mock
	private Template template;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

	}

	@Test
	public void testCreateInstance() throws NumberFormatException, RunNodesException {

		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws","aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		when(computeService.templateBuilder()).thenReturn(templateBuilder);

		Instance instance = InstanceFixture.getInstance( "instance-id", "instance-name", "image", "2", "512",
				"cpu","running", infratructure.getName());

		when(templateBuilder.minRam(Integer.parseInt(instance.getRam()))).thenReturn(templateBuilder);

		when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

		when(templateBuilder.build()).thenReturn(template);
		
		
		Set nodesMetaData = Sets.newHashSet();
		NodeMetadataImpl nodeMetadataImpl = mock(NodeMetadataImpl.class);
		when(nodeMetadataImpl.getId()).thenReturn("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592");
		nodesMetaData.add(nodeMetadataImpl);
		
		
		when(computeService.createNodesInGroup(instance.getName(), Integer.parseInt(instance.getNumber()), template)).thenReturn(nodesMetaData);

		Instance created = instanceService.createInstance(instance);
		
		assertThat(created.getId(), is("RegionOne/1cde5a56-27a6-46ce-bdb7-8b01b8fe2592"));

		verify(computeService, times(1)).createNodesInGroup(instance.getName(), Integer.parseInt(instance.getNumber()),
				template);

	}

	@Test
	public void testDeleteInstance() throws NumberFormatException, RunNodesException {

		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws","aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		instanceService.deleteInstance(infratructure.getName(), "instanceID");

		verify(computeService, times(1)).destroyNode("instanceID");

	}

	@Test
	public void testGetAllInstances() throws NumberFormatException, RunNodesException {

		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("id-aws","aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		Set nodes = Sets.newHashSet();
		NodeMetadataImpl node = mock(NodeMetadataImpl.class);
		when(node.getId()).thenReturn("someId");
		when(node.getName()).thenReturn("someName");
		Hardware hardware = mock(Hardware.class);
		when(hardware.getProcessors()).thenReturn(Lists.newArrayList());
		when(node.getHardware()).thenReturn(hardware);
		when(node.getStatus()).thenReturn(Status.RUNNING);
		nodes.add(node);
		when(computeService.listNodes()).thenReturn(nodes);

		Set<Instance> allNodes = instanceService.getAllInstances(infratructure.getName());

		assertThat(allNodes.iterator().next().getId(), is("someId"));

	}
}
