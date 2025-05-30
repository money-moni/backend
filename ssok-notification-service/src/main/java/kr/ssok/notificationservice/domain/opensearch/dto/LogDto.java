package kr.ssok.notificationservice.domain.opensearch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogDto {
    private String id;
    private String level;
    private String app;
    private String timestamp;
    private String message;
}