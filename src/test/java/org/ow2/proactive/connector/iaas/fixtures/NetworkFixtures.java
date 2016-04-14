package org.ow2.proactive.connector.iaas.fixtures;

import org.ow2.proactive.connector.iaas.model.Network;

import com.google.common.collect.Sets;


public class NetworkFixtures {

    public static Network getNetwork(String publicAddress, String privateAddress) {
        return new Network(Sets.newHashSet(publicAddress), Sets.newHashSet(privateAddress));
    }

    

    public static Network simpleNetwork() {
        return new Network(Sets.newHashSet("publicAddress"), Sets.newHashSet("privateAddress"));
    }


}
