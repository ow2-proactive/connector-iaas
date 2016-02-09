package org.ow2.proactive.connector.iaas.cloud.provider.jclouds;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jclouds.compute.ComputeService;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.stereotype.Component;

import lombok.Getter;


@Component
public class OpenstackJCloudsProvider extends JCloudsProvider {

    @Getter
    private final String type = "openstack-nova";

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        NovaApi novaApi = computeService.getContext().unwrapApi((NovaApi.class));

        ServerApi serverApi = novaApi.getServerApi("RegionOne");

        String script = buildScriptToExecuteString(instance.getInstanceScript());

        CreateServerOptions serverOptions = new CreateServerOptions()
                .keyPairName(instance.getCredentials().getPublicKeyName()).userData(script.getBytes());

        return IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                .mapToObj(i -> createOpenstackInstance(instance, serverApi, serverOptions))
                .map(server -> instanceCreatorFromNodeMetadata.apply(server, infrastructure.getId()))
                .collect(Collectors.toSet());

    }

    private Server createOpenstackInstance(Instance instance, ServerApi serverApi,
            CreateServerOptions serverOptions) {
        ServerCreated serverCreated = serverApi.create(instance.getTag(), instance.getImage(),
                instance.getHardware().getType(), serverOptions);

        return serverApi.get(serverCreated.getId());
    }

    protected final BiFunction<Server, String, Instance> instanceCreatorFromNodeMetadata = (server,
            infrastructureId) -> {
        return Instance.builder().id(server.getId()).tag(server.getName()).image(server.getImage().getName())
                .number("1").hardware(Hardware.builder().type(server.getFlavor().getName()).build())
                .status(server.getStatus().name()).build();
    };

}
