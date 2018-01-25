package com.rackspacecloud.blueflood.elasticsearchconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Index {
    @JsonProperty("_index")
    String index;

    @JsonProperty("_type")
    String type;

    @JsonProperty("_id")
    String id;

    @JsonProperty("routing")
    String routing;
}
