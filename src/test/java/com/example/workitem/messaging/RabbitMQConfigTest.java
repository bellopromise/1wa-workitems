package com.example.workitem.messaging;

import com.example.workitem.config.RabbitMQProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMQConfigTest {

    @Mock
    private ConnectionFactory connectionFactoryMock;

    @Mock
    private ThreadPoolTaskExecutor taskExecutorMock;


    @InjectMocks
    private RabbitMQConfig rabbitMQConfig;

    @Mock
    private AmqpAdmin amqpAdminMock;


    @Test
    public void rabbitListenerContainerFactory_Configuration_Success() throws Exception {
        // Act
        SimpleRabbitListenerContainerFactory factory = rabbitMQConfig.rabbitListenerContainerFactory(connectionFactoryMock, taskExecutorMock);

        // Assert
        assertNotNull(factory);
        assertEquals(2, getFieldValue(factory, "concurrentConsumers"));
        assertEquals(2, getFieldValue(factory, "maxConcurrentConsumers"));
    }


    @Test
    public void template_Configuration_Success() {

        // Act
        RabbitTemplate rabbitTemplate = rabbitMQConfig.template(connectionFactoryMock);

        // Assert
        assertNotNull(rabbitTemplate);
        assertEquals(connectionFactoryMock, rabbitTemplate.getConnectionFactory());
        assertTrue(rabbitTemplate.getMessageConverter() instanceof Jackson2JsonMessageConverter);
    }

    @Test
    public void workItemQueue_Configuration_Success() {
        // Arrange
        RabbitAdmin rabbitAdminMock = mock(RabbitAdmin.class);
        rabbitMQConfig.amqpAdmin(connectionFactoryMock);

        // Act
        Queue queue = rabbitMQConfig.workItemQueue(rabbitAdminMock);

        // Assert
        assertNotNull(queue);
        assertEquals("work-item-queue", queue.getName());
        verify(rabbitAdminMock).declareQueue(queue);
    }

    private Object getFieldValue(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

}

