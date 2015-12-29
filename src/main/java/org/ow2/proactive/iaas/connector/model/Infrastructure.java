package org.ow2.proactive.iaas.connector.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(exclude = { "credential" })
@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class Infrastructure {

	private String name;
	private String endPoint;
	private String userName;
	private String credential;

}
