package com.rackspacecloud.blueflood.elasticsearchconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MetricInformation {
    @JsonProperty("tenantId")
    String tenantId;

    @JsonProperty("metric_name")
    String metricName;
}
