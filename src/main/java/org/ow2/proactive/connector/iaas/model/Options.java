package org.ow2.proactive.connector.iaas.model;

import java.util.List;
import java.util.Set;

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
public class Options {
    private String spotPrice;
    private Set<String> securityGroupNames;
    private String subnetId;
    private List<String> macAddresses;
}
