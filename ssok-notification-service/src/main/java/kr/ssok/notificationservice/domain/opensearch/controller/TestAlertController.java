package kr.ssok.notificationservice.domain.opensearch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@Slf4j
public class TestAlertController {

    @PostMapping
    public ResponseEntity<Void> receiveAlert(@RequestBody Map<String, Object> body) {
        log.warn("전체 JSON: {}", body);
        return ResponseEntity.ok().build();
    }
}