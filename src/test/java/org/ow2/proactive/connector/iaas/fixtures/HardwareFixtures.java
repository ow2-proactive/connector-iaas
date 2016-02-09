package org.ow2.proactive.connector.iaas.fixtures;

import org.ow2.proactive.connector.iaas.model.Hardware;


public class HardwareFixtures {

    public static Hardware getHardware(String type, String minRam, String minCores) {
        return new Hardware(type, minRam, minCores);
    }

    public static Hardware getHardware(String minRam, String minCores) {
        return new Hardware("", minRam, minCores);
    }

    public static Hardware simpleHardware() {
        return new Hardware("type", "minRam", "minCores");
    }

    public static Hardware getHardware(String type) {
        return new Hardware(type, "", "");
    }

}
