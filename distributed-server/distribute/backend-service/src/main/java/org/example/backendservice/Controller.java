package org.example.backendservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    @Value("${server.instance-id}")
    private String instanceId;

    @GetMapping("/request")
    public String response() {
        log.info("server id is : {} ", instanceId);
        return instanceId;
    }

}
