package com.nemo.deploy;

import com.nemo.deploy.service.RedisQueueListenerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DeploymentServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DeploymentServiceApplication.class, args);
        RedisQueueListenerService redisService = context.getBean(RedisQueueListenerService.class);
        redisService.startListening();
    }

}
