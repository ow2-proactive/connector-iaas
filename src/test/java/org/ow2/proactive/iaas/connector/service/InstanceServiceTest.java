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
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.ComputeMetadataImpl;
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

		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		when(computeService.templateBuilder()).thenReturn(templateBuilder);

		Instance instance = InstanceFixture.getInstance(infratructure.getName(), "instance-name", "image", "2", "512",
				"cpu");

		when(templateBuilder.minRam(Integer.parseInt(instance.getRam()))).thenReturn(templateBuilder);

		when(templateBuilder.imageId(instance.getImage())).thenReturn(templateBuilder);

		when(templateBuilder.build()).thenReturn(template);

		instanceService.createInstance(instance);

		verify(computeService, times(1)).createNodesInGroup(instance.getName(), Integer.parseInt(instance.getNumber()),
				template);

	}

	@Test
	public void testDeleteInstance() throws NumberFormatException, RunNodesException {

		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		instanceService.deleteInstance(infratructure.getName(), "instanceID");

		verify(computeService, times(1)).destroyNode("instanceID");

	}

	@Test
	public void testGetAllInstances() throws NumberFormatException, RunNodesException {

		Infrastructure infratructure = InfrastructureFixture.getInfrastructure("aws", "endPoint", "userName",
				"credential");
		when(infrastructureService.getInfrastructurebyName(infratructure.getName())).thenReturn(infratructure);

		when(computeServiceCache.getComputeService(infratructure)).thenReturn(computeService);

		Set nodes = Sets.newHashSet();
		ComputeMetadataImpl node = mock(ComputeMetadataImpl.class);
		when(node.getId()).thenReturn("someId");
		nodes.add(node);
		when(computeService.listNodes()).thenReturn(nodes);

		Set<String> allNodes = instanceService.getAllInstances(infratructure.getName());

		assertThat(allNodes.iterator().next(), is("someId"));

	}
}
