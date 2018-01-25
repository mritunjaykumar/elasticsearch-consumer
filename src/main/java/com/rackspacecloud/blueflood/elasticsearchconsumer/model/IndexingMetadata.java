package com.rackspacecloud.blueflood.elasticsearchconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IndexingMetadata {
    @JsonProperty("index")
    Index index;
}
