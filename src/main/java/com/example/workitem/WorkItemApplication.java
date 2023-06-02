package com.example.workitem;

import com.example.workitem.messaging.RabbitMQConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@Import(RabbitMQConfig.class)
public class WorkItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkItemApplication.class, args);
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Set the maximum number of concurrent threads
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(1000);
        return executor;
    }

}
