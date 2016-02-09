package org.ow2.proactive.connector.iaas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@EqualsAndHashCode
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Builder
public class Hardware {

    private String type;
    private String minRam;
    private String minCores;

}
