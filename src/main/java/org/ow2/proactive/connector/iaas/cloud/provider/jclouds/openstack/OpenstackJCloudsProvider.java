package org.ow2.proactive.connector.iaas.cloud.provider.jclouds.openstack;

import lombok.Getter;
import org.jclouds.compute.ComputeService;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.ow2.proactive.connector.iaas.cloud.provider.jclouds.JCloudsProvider;
import org.ow2.proactive.connector.iaas.model.Hardware;
import org.ow2.proactive.connector.iaas.model.Infrastructure;
import org.ow2.proactive.connector.iaas.model.Instance;
import org.springframework.stereotype.Component;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class OpenstackJCloudsProvider extends JCloudsProvider {

    private final static String region = "RegionOne";

    @Getter
    private final String type = "openstack-nova";

    @Override
    public Set<Instance> createInstance(Infrastructure infrastructure, Instance instance) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        NovaApi novaApi = computeService.getContext().unwrapApi((NovaApi.class));

        ServerApi serverApi = novaApi.getServerApi(region);

        String script = buildScriptToExecuteString(instance.getInitScript());

        CreateServerOptions serverOptions = new CreateServerOptions()
                .keyPairName(instance.getCredentials().getPublicKeyName()).userData(script.getBytes());

        return IntStream.rangeClosed(1, Integer.valueOf(instance.getNumber()))
                .mapToObj(i -> createOpenstackInstance(instance, serverApi, serverOptions))
                .map(server -> instanceCreatorFromNodeMetadata.apply(server, infrastructure.getId()))
                .collect(Collectors.toSet());

    }

    @Override
    public String addToInstancePublicIp(Infrastructure infrastructure, String instanceId) {

        ComputeService computeService = getComputeServiceFromInfastructure(infrastructure);

        NovaApi novaApi = computeService.getContext().unwrapApi(NovaApi.class);
        FloatingIPApi api;
        if (!novaApi.getFloatingIPApi(region).isPresent()) {
            throw new NotSupportedException("Operation not supported for this Openstack cloud");
        } else {
            api = novaApi.getFloatingIPApi(region).get();
        }

        FloatingIP ip = api.list()
                .toList()
                .stream()
                .filter(floatingIP -> floatingIP.getFixedIp() == null)
                .findFirst()
                .orElseThrow(() -> new ClientErrorException("No Floating IP available in the floating IP pool",
                        Response.Status.BAD_REQUEST));


        try {
            api.addToServer(ip.getIp(), instanceId);
        } catch (Exception e) {
            if (e.getMessage().contains("Unable to associate floating ip")) {
                throw new ClientErrorException("A floating IP is already associated to the instance " + instanceId,
                        Response.Status.BAD_REQUEST);
            } else {
                throw new RuntimeException(e);
            }
        }

        return ip.getIp();

    }

    private Server createOpenstackInstance(Instance instance, ServerApi serverApi, CreateServerOptions serverOptions) {
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
