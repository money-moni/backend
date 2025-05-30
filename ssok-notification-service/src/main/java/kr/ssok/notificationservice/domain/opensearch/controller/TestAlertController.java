package kr.ssok.notificationservice.domain.opensearch.controller;

import kr.ssok.notificationservice.domain.opensearch.dto.LogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Slf4j
public class TestAlertController {

    @PostMapping
    public ResponseEntity<Void> receiveAlerts(@RequestBody List<LogDto> logs) {
        for (LogDto logDto : logs) {
            log.info("Received log: id={}, level={}, app={}, timestamp={}, msg={}",
                    logDto.getId(), logDto.getLevel(), logDto.getApp(),
                    logDto.getTimestamp(), logDto.getMessage());
        }
        return ResponseEntity.ok().build();
    }

}
