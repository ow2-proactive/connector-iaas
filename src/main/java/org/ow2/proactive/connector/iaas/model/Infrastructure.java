package org.ow2.proactive.connector.iaas.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@EqualsAndHashCode(of = { "id" })
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class Infrastructure {

    private String id;
    private String type;
    private String endpoint;
    private Credentials credentials;
    private boolean toBeRemovedOnShutdown;

}
