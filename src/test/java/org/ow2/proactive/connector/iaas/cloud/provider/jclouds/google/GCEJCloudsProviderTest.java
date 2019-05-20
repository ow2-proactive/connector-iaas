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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.googlecomputeengine.compute.options.GoogleComputeEngineTemplateOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.cloud.TagManager;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsComputeServiceCache;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.fixtures.InstanceFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.ow2.proactive.connector.iaas.model.Options;
import org.ow2.proactive.connector.iaas.model.Tag;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * @author ActiveEon Team
 * @since Apr 9, 2019
 */
public class GCEJCloudsProviderTest {
    private static final String TYPE = "google-compute-engine";

    private static final String INFRA_ID = "id-google-compute-engine";

    private static final String INSTANCE_GROUP = "instance-group";

    private static final String IMAGE = "image";

    private static final int INSTANCE_NUM = 1;

    private static final int MIN_RAM = 1024;

    private static final int MIN_CORES = 1;

    private static final String USERNAME = "username";

    private static final String PUBLIC_KEY = "publicKey";

    private static final String PRIVATE_KEY = "privateKey";

    private static final String REGION = "region";

    private static final String SCRIPT_CMD_1 = "cmd 1";

    private static final String SCRIPT_CMD_2 = "cmd 2";

    private static final String[] INIT_SCRIPT = new String[] { SCRIPT_CMD_1, SCRIPT_CMD_2 };

    private static final String TAG_KEY = "connector-iaas-tag-key";

    private static final String TAG_VALUE = "default-value";

    private static final Tag CONNECTOR_IAAS_TAG = Tag.builder().key(TAG_KEY).value(TAG_VALUE).build();

    private static final String STATUS = "RUNNING";

    private static final String ID_PREFIX = "https://www.googleapis.com/compute/v1/projects/project/zones/region/instances/";

    @Mock
    TemplateOptions templateOptions;

    @InjectMocks
    private GCEJCloudsProvider gceJCloudsProvider;

    @Mock
    private JCloudsComputeServiceCache computeServiceCache;

    @Mock
    private ComputeServiceTest computeService;

    @Mock(answer = RETURNS_SELF)
    private TemplateBuilder templateBuilder;

    @Mock
    private Template template;

    @Mock
    private GoogleComputeEngineTemplateOptions gceTemplateOptions;

    @Mock
    private TagManager tagManager;

    @Captor
    private ArgumentCaptor<Map<String, String>> argumentCaptor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateInstance() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure(INFRA_ID, TYPE);

        Instance instance = InstanceFixture.getInstanceWithInitScript(INSTANCE_GROUP,
                                                                      IMAGE,
                                                                      String.valueOf(INSTANCE_NUM),
                                                                      String.valueOf(MIN_RAM),
                                                                      String.valueOf(MIN_CORES),
                                                                      USERNAME,
                                                                      PUBLIC_KEY,
                                                                      PRIVATE_KEY,
                                                                      REGION,
                                                                      INIT_SCRIPT);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);
        when(computeService.templateBuilder()).thenReturn(templateBuilder);
        when(templateBuilder.build()).thenReturn(template);
        when(template.getOptions()).thenReturn(templateOptions);
        when(templateOptions.as(GoogleComputeEngineTemplateOptions.class)).thenReturn(gceTemplateOptions);
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(CONNECTOR_IAAS_TAG));

        Set<Instance> createdInstances = gceJCloudsProvider.createInstance(infrastructure, instance);

        verify(templateBuilder).minRam(MIN_RAM);
        verify(templateBuilder).minCores(MIN_CORES);
        verify(templateBuilder).imageNameMatches(IMAGE);
        verify(templateBuilder).locationId(REGION);
        verify(gceTemplateOptions).userMetadata(argumentCaptor.capture());
        assertEquals(TAG_VALUE, argumentCaptor.getValue().get(TAG_KEY));
        verify(gceTemplateOptions).overrideLoginUser(USERNAME);
        verify(gceTemplateOptions).authorizePublicKey(PUBLIC_KEY);
        verify(gceTemplateOptions).overrideLoginPrivateKey(PRIVATE_KEY);
        verify(gceTemplateOptions).runScript(contains(SCRIPT_CMD_1));
        verify(gceTemplateOptions).runScript(contains(SCRIPT_CMD_2));

        assertThat(createdInstances.size(), is(INSTANCE_NUM));
        Instance createdInstance = createdInstances.stream().findAny().orElse(null);
        assertNotNull(createdInstance);
        assertThat(createdInstance.getTag(), is(INSTANCE_GROUP + "-suffix-0"));
        assertThat(createdInstance.getId(), is(ID_PREFIX + INSTANCE_GROUP + "-suffix-0"));
        assertThat(createdInstance.getStatus(), is(STATUS));
    }

    @Test
    public void testCreateInstanceWithNullParameters() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure(TYPE);

        Instance instance = InstanceFixture.getInstanceWithNullArgs(INSTANCE_GROUP, String.valueOf(INSTANCE_NUM));

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);
        when(computeService.templateBuilder()).thenReturn(templateBuilder);
        when(templateBuilder.build()).thenReturn(template);
        when(template.getOptions()).thenReturn(templateOptions);
        when(templateOptions.as(GoogleComputeEngineTemplateOptions.class)).thenReturn(gceTemplateOptions);
        when(tagManager.retrieveAllTags(anyString(), any())).thenReturn(Lists.newArrayList(CONNECTOR_IAAS_TAG));

        Set<Instance> createdInstances = gceJCloudsProvider.createInstance(infrastructure, instance);

        // null parameters should not be set
        verify(templateBuilder, never()).minRam(MIN_RAM);
        verify(templateBuilder, never()).minCores(MIN_CORES);
        verify(templateBuilder, never()).imageNameMatches(IMAGE);
        verify(templateBuilder, never()).locationId(REGION);
        verify(gceTemplateOptions, never()).overrideLoginUser(USERNAME);
        verify(gceTemplateOptions, never()).authorizePublicKey(PUBLIC_KEY);
        verify(gceTemplateOptions, never()).overrideLoginPrivateKey(PRIVATE_KEY);
        verify(gceTemplateOptions, never()).runScript(contains(SCRIPT_CMD_1));
        verify(gceTemplateOptions, never()).runScript(contains(SCRIPT_CMD_2));

        // default jClouds tag parameter should still be set
        verify(gceTemplateOptions).userMetadata(argumentCaptor.capture());
        assertEquals(TAG_VALUE, argumentCaptor.getValue().get(TAG_KEY));

        assertThat(createdInstances.size(), is(INSTANCE_NUM));
        Instance createdInstance = createdInstances.stream().findAny().orElse(null);
        assertNotNull(createdInstance);
        assertThat(createdInstance.getTag(), is(INSTANCE_GROUP + "-suffix-0"));
        assertThat(createdInstance.getId(), is(ID_PREFIX + INSTANCE_GROUP + "-suffix-0"));
        assertThat(createdInstance.getStatus(), is(STATUS));
    }

    @Test
    public void testCreateInstanceWithEmptyParameters() {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure(TYPE);

        Instance instance = InstanceFixture.getInstanceWithInitScript(INSTANCE_GROUP,
                                                                      "",
                                                                      String.valueOf(INSTANCE_NUM),
                                                                      "",
                                                                      "",
                                                                      "",
                                                                      "",
                                                                      "",
                                                                      "",
                                                                      INIT_SCRIPT);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);
        when(computeService.templateBuilder()).thenReturn(templateBuilder);
        when(templateBuilder.build()).thenReturn(template);
        when(template.getOptions()).thenReturn(templateOptions);
        when(templateOptions.as(GoogleComputeEngineTemplateOptions.class)).thenReturn(gceTemplateOptions);
        when(tagManager.retrieveAllTags(anyString(), any())).thenReturn(Lists.newArrayList(CONNECTOR_IAAS_TAG));

        Set<Instance> createdInstances = gceJCloudsProvider.createInstance(infrastructure, instance);

        verify(templateBuilder, never()).minRam(MIN_RAM);
        verify(templateBuilder, never()).minCores(MIN_CORES);
        verify(templateBuilder, never()).imageNameMatches(IMAGE);
        verify(templateBuilder, never()).locationId(REGION);
        verify(gceTemplateOptions).userMetadata(argumentCaptor.capture());
        assertEquals(TAG_VALUE, argumentCaptor.getValue().get(TAG_KEY));
        verify(gceTemplateOptions, never()).overrideLoginUser(USERNAME);
        verify(gceTemplateOptions, never()).authorizePublicKey(PUBLIC_KEY);
        verify(gceTemplateOptions, never()).overrideLoginPrivateKey(PRIVATE_KEY);
        verify(gceTemplateOptions).runScript(contains(SCRIPT_CMD_1));
        verify(gceTemplateOptions).runScript(contains(SCRIPT_CMD_2));

        assertThat(createdInstances.size(), is(INSTANCE_NUM));
        Instance createdInstance = createdInstances.stream().findAny().orElse(null);
        assertNotNull(createdInstance);
        assertThat(createdInstance.getTag(), is(INSTANCE_GROUP + "-suffix-0"));
        assertThat(createdInstance.getId(), is(ID_PREFIX + INSTANCE_GROUP + "-suffix-0"));
        assertThat(createdInstance.getStatus(), is(STATUS));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateInstanceWithFailure() throws NumberFormatException {
        Infrastructure infrastructure = InfrastructureFixture.getSimpleInfrastructure(TYPE);

        Instance instance = InstanceFixture.getInstanceWithInitScript(INSTANCE_GROUP,
                                                                      IMAGE,
                                                                      String.valueOf(INSTANCE_NUM),
                                                                      String.valueOf(MIN_RAM),
                                                                      String.valueOf(MIN_CORES),
                                                                      USERNAME,
                                                                      PUBLIC_KEY,
                                                                      PRIVATE_KEY,
                                                                      REGION,
                                                                      INIT_SCRIPT);

        when(computeServiceCache.getComputeService(infrastructure)).thenReturn(computeService);
        when(computeService.templateBuilder()).thenReturn(templateBuilder);
        when(templateBuilder.build()).thenReturn(template);
        when(template.getOptions()).thenReturn(templateOptions);
        when(templateOptions.as(GoogleComputeEngineTemplateOptions.class)).thenReturn(gceTemplateOptions);
        when(tagManager.retrieveAllTags(anyString(),
                                        any(Options.class))).thenReturn(Lists.newArrayList(CONNECTOR_IAAS_TAG));

        when(computeService.createNodesInGroup(INSTANCE_GROUP,
                                               INSTANCE_NUM,
                                               template)).thenThrow(RunNodesException.class);

        gceJCloudsProvider.createInstance(infrastructure, instance);
    }

    abstract class ComputeServiceTest implements ComputeService {
        public Set<? extends NodeMetadata> createNodesInGroup(String group, int count, Template template) {
            Set<NodeMetadata> nodes = Sets.newHashSet();
            for (int i = 0; i < count; i++) {
                NodeMetadataImpl node = mock(NodeMetadataImpl.class);
                // JClouds is supposed to add suffix at the end of group name as the instance tag
                String tag = group + "-suffix-" + i;
                // JClouds google-compute-engine instance id format
                String id = ID_PREFIX + tag;

                when(node.getId()).thenReturn(id);
                when(node.getName()).thenReturn(tag);
                when(node.getStatus()).thenReturn(NodeMetadata.Status.RUNNING);

                nodes.add(node);
            }
            return nodes;
        }
    }

}
