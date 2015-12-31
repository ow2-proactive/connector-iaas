package org.ow2.proactive.iaas.connector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@EqualsAndHashCode(exclude = { "name", "image", "number" , "ram", "cpu", "status" })
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Wither
@Builder
public class Instance {

	private String id;
	private String name;
	private String image;
	private String number;
	private String ram;
	private String cpu;
	private String status;
	private String infrastructureName;

}
