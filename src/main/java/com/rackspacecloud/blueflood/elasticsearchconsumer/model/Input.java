package com.rackspacecloud.blueflood.elasticsearchconsumer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "collectionTime", "metricValue" })
public class Input {
    @JsonProperty("metricName")
    String metricName;

    public String getTenantId(){
        String[] strArr = metricName.split("\\.");
        return strArr[0];
    }

    public String getMetricString(){
        String[] strArr = metricName.split("\\.", 2);
        return strArr[1];
    }
}
