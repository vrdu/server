package com.example.server.config;

import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration
public class QueueConfig {

    @Bean
    @Qualifier("promptingQueue")
    public Queue<Map<Triple<String, String, String>, List<String>>> promptingQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    @Bean
    @Qualifier("promptGenerationQueue")
    public Queue<Map<Triple<String, String, String>, List<String>>> promptGenerationQueue() {
        return new ConcurrentLinkedQueue<>();
    }
}

