package kr.ssok.notificationservice.domain.opensearch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@Slf4j
public class TestAlertController {

    @PostMapping
    public ResponseEntity<Void> receiveAlert(@RequestBody String raw) {
        log.warn("Raw body: >{}<", raw);
        return ResponseEntity.ok().build();
    }
}