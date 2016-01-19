package org.ow2.proactive.connector.iaas.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class Credentials {

	private String username;
	private String password;
	private String privateKey;
}
