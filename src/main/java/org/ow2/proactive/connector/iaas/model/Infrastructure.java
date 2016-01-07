package org.ow2.proactive.connector.iaas.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@EqualsAndHashCode(exclude = { "type", "endPoint", "userName", "credential" })
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class Infrastructure {

    private String id;
    private String type;
    private String endPoint;
    private String userName;
    private String credential;

}
