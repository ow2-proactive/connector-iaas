package org.ow2.proactive.connector.iaas;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.connector.iaas.fixtures.InfrastructureFixture;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.service.InfrastructureService;

import com.google.common.collect.Maps;


public class ShutdownHandlerTest {

    @InjectMocks
    private ShutdownHandler shutdownHandler;

    @Mock
    private InfrastructureService infrastructureService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testRemoveAllInfrastructures() {
        Map<String, Infrastructure> supportedInfrastructureMap = Maps.newHashMap();
        supportedInfrastructureMap.put(InfrastructureFixture.getSimpleInfrastructure("type1", true).getId(),
                InfrastructureFixture.getSimpleInfrastructure("type1", true));
        supportedInfrastructureMap.put(InfrastructureFixture.getSimpleInfrastructure("type2", true).getId(),
                InfrastructureFixture.getSimpleInfrastructure("type2", true));
        supportedInfrastructureMap.put(InfrastructureFixture.getSimpleInfrastructure("type3", false).getId(),
                InfrastructureFixture.getSimpleInfrastructure("type3", false));

        when(infrastructureService.getAllSupportedInfrastructure()).thenReturn(supportedInfrastructureMap);

        shutdownHandler.removeAllInfrastructures();

        verify(infrastructureService, times(1))
                .deleteInfrastructure(InfrastructureFixture.getSimpleInfrastructure("type1"));
        verify(infrastructureService, times(1))
                .deleteInfrastructure(InfrastructureFixture.getSimpleInfrastructure("type2"));
        verify(infrastructureService, times(0))
                .deleteInfrastructure(InfrastructureFixture.getSimpleInfrastructure("type3"));

    }

}
