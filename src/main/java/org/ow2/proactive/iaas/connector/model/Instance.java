package org.ow2.proactive.iaas.connector.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@ToString
public class Instance {

	private String infrastructure;
	private String name;
	private String image;
	private String number;
	private String ram;
	private String cpu;

	public Instance() {

	}

}
