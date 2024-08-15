package com.stambul.scanner;

import javax.sql.DataSource;

import com.stambul.scanner.jobs.exceptions.UncaughtExceptionHandler;
import com.stambul.library.tools.TimeTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({
        "com.stambul"
})
@PropertySources({
    @PropertySource("classpath:parsers.properties")
})
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class ScannerConfig {

    @Bean("parsersScheduler")
    public ThreadPoolTaskScheduler parsersScheduler(
            @Value("${parsers.scheduler.await.termination.iso}") String awaitTermination,
            @Value("${parsers.scheduler.pool.size}") int poolSize,
            @Value("${parsers.scheduler.name.prefix}") String namePrefix,
            @Value("${parsers.scheduler.remove.policy}") boolean removeOnCancelPolicy,
            @Value("${parsers.scheduler.wait.on.shutdown}") boolean waitOnShutdown
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
            @Value("${parsers.listeners.executor.await.termination.iso}") String awaitTermination,
            @Value("${parsers.listeners.executor.pool.core.size}") int corePoolSize,
            @Value("${parsers.listeners.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${parsers.listeners.executor.name.prefix}") String namePrefix,
            @Value("${parsers.listeners.executor.wait.on.shutdown}") boolean waitOnShutdown
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
