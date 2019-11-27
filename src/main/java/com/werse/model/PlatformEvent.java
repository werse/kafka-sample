package com.werse.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlatformEvent {

  @JsonProperty("@id") private UUID id;
  @JsonProperty("org") private String organization;
  @JsonProperty("system") private String system;
  @JsonProperty("@type") private String eventType;
  @JsonProperty("version") private String version;
  @JsonProperty("cause") private String cause;
  @JsonProperty("username") private String username;

  private Map<String, Object> properties = Collections.emptyMap();
}
