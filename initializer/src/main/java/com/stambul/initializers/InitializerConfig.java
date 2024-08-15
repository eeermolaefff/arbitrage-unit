package com.stambul.initializers;

import com.stambul.initializers.jobs.exceptions.UncaughtExceptionHandler;
import com.stambul.library.tools.TimeTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan({
        "com.stambul"
})
@PropertySources({
        @PropertySource("classpath:general.properties"),
        @PropertySource("classpath:dynamic.properties")
})
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class InitializerConfig {

    @Bean
    public RestTemplate restTemplate(
            @Value("${coinmarketcap.connection.timeout.iso}") String connectionTimeoutISO,
            @Value("${coinmarketcap.connection.read.timeout.iso}") String readTimeoutISO
    ) {
        return new RestTemplateBuilder()
                .setConnectTimeout(TimeTools.toDuration(connectionTimeoutISO))
                .setReadTimeout(TimeTools.toDuration(readTimeoutISO))
                .build();
    }

    @Bean("initializersScheduler")
    public ThreadPoolTaskScheduler initializersScheduler(
            @Value("${coinmarketcap.initialization.scheduler.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.scheduler.pool.size}") int poolSize,
            @Value("${coinmarketcap.initialization.scheduler.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.scheduler.remove.policy}") boolean removeOnCancelPolicy,
            @Value("${coinmarketcap.initialization.scheduler.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(namePrefix);
        scheduler.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        scheduler.setRemoveOnCancelPolicy(removeOnCancelPolicy);
        scheduler.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        scheduler.setErrorHandler(new UncaughtExceptionHandler());

        return scheduler;
    }

    @Bean("listenersExecutor")
    public ThreadPoolTaskExecutor listenersExecutor(
            @Value("${coinmarketcap.initialization.listeners.executor.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.listeners.executor.pool.core.size}") int corePoolSize,
            @Value("${coinmarketcap.initialization.listeners.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${coinmarketcap.initialization.listeners.executor.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.listeners.executor.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setAllowCoreThreadTimeOut(allowTimeout);
        executor.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        executor.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        executor.initialize();

        return executor;
    }

    @Bean("transfersParsersExecutor")
    public ThreadPoolTaskExecutor transferParsersExecutor(
            @Value("${coinmarketcap.initialization.transfers.executor.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.transfers.executor.pool.core.size}") int corePoolSize,
            @Value("${coinmarketcap.initialization.transfers.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${coinmarketcap.initialization.transfers.executor.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.transfers.executor.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setAllowCoreThreadTimeOut(allowTimeout);
        executor.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        executor.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        executor.initialize();

        return executor;
    }

    @Bean("contractParsersExecutor")
    public ThreadPoolTaskExecutor contractParsersExecutor(
            @Value("${coinmarketcap.initialization.contracts.executor.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.contracts.executor.pool.core.size}") int corePoolSize,
            @Value("${coinmarketcap.initialization.contracts.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${coinmarketcap.initialization.contracts.executor.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.contracts.executor.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setAllowCoreThreadTimeOut(allowTimeout);
        executor.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        executor.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        executor.initialize();

        return executor;
    }

    @Bean("relationsParsersExecutor")
    public ThreadPoolTaskExecutor relationsParsersExecutor(
            @Value("${coinmarketcap.initialization.relations.executor.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.relations.executor.pool.core.size}") int corePoolSize,
            @Value("${coinmarketcap.initialization.relations.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${coinmarketcap.initialization.relations.executor.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.relations.executor.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setAllowCoreThreadTimeOut(allowTimeout);
        executor.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        executor.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        executor.initialize();

        return executor;
    }

    @Bean("marketsParsersExecutor")
    public ThreadPoolTaskExecutor marketsParsersExecutor(
            @Value("${coinmarketcap.initialization.markets.executor.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.markets.executor.pool.core.size}") int corePoolSize,
            @Value("${coinmarketcap.initialization.markets.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${coinmarketcap.initialization.markets.executor.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.markets.executor.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setAllowCoreThreadTimeOut(allowTimeout);
        executor.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        executor.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        executor.initialize();

        return executor;
    }

    @Bean("currenciesParsersExecutor")
    public ThreadPoolTaskExecutor currenciesParsersExecutor(
            @Value("${coinmarketcap.initialization.currencies.executor.await.termination.iso}") String awaitTermination,
            @Value("${coinmarketcap.initialization.currencies.executor.pool.core.size}") int corePoolSize,
            @Value("${coinmarketcap.initialization.currencies.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${coinmarketcap.initialization.currencies.executor.name.prefix}") String namePrefix,
            @Value("${coinmarketcap.initialization.currencies.executor.wait.on.shutdown}") boolean waitOnShutdown
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setAllowCoreThreadTimeOut(allowTimeout);
        executor.setAwaitTerminationSeconds(TimeTools.toSeconds(awaitTermination).intValue());
        executor.setWaitForTasksToCompleteOnShutdown(waitOnShutdown);
        executor.initialize();

        return executor;
    }
}
