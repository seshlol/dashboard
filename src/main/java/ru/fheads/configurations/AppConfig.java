package ru.fheads.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    @Value("${app.poolSize}")
    private int poolSize;

    @Bean(name = "executorService")
    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(poolSize);
    }
}
