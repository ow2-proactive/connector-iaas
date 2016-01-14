package org.ow2.proactive.connector.iaas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@EqualsAndHashCode(exclude = { "image", "number", "ram", "cpu", "status", "postBootScript" })
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Wither
@Builder
public class Instance {

	private String id;
	private String tag;
	private String image;
	private String number;
	private String ram;
	private String cpu;
	private String status;
	private InstanceScript postBootScript;

}
