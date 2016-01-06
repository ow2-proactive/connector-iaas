package org.ow2.proactive.iaas.connector.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ScriptResult {

    private String output;
    private String error;
}
