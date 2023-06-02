package com.example.workitem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WorkItemProducer {

    private static final String QUEUE_NAME = "work-item-queue";

    private static final Logger logger = LoggerFactory.getLogger(WorkItemProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public WorkItemProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendWorkItem(String id, int value) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", id);
        message.put("value", value);
        rabbitTemplate.convertAndSend(QUEUE_NAME, message);

        // Log the sent work item
        logger.info("Work item sent. ID: {}, Value: {}", id, value);
    }

}
