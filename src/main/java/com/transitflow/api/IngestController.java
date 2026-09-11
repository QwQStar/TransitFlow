package com.transitflow.api;

import com.transitflow.domain.MobilityEvent;
import com.transitflow.ingest.EventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class IngestController {

    private final EventPublisher publisher;

    public IngestController(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@Valid @RequestBody MobilityEvent event) {
        publisher.publish(event);
    }
}
