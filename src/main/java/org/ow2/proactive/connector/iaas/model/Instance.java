package org.ow2.proactive.connector.iaas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@EqualsAndHashCode(exclude = { "image", "number", "hardware", "status", "credentials", "initScript" })
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
	private String status;
	private Hardware hardware;
	private Network network;
	private Credentials credentials;
	private InstanceScript initScript;

}
