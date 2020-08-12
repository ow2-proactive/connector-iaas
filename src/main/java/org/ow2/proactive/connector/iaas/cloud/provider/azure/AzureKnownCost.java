package org.ow2.proactive.connector.iaas.cloud.provider.azure;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AzureKnownCost {

    String meterCategory;
    String meterRegion;
    double meterRatesZero;
}
