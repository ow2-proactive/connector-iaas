package org.ow2.proactive.connector.iaas.fixtures;

import org.ow2.proactive.connector.iaas.model.Credentials;


public class CredentialsFixtures {

    public static Credentials getCredentials(String username, String password) {
        return new Credentials(username, password, "");
    }

    public static Credentials getCredentials(String publicKeyName) {
        return new Credentials("", "", publicKeyName);
    }

}
