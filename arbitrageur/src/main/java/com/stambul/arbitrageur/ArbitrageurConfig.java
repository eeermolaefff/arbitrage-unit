package com.stambul.arbitrageur;

import com.stambul.arbitrageur.exceptions.UncaughtExceptionHandler;
import com.stambul.library.tools.TimeTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({
        "com.stambul"
})
@PropertySources({
    @PropertySource("classpath:arbitrageur.properties")
})
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class ArbitrageurConfig {

    @Bean("arbitrageScheduler")
    public ThreadPoolTaskScheduler arbitrageScheduler(
            @Value("${scheduler.await.termination.iso}") String awaitTermination,
            @Value("${scheduler.pool.size}") int poolSize,
            @Value("${scheduler.name.prefix}") String namePrefix,
            @Value("${scheduler.remove.policy}") boolean removeOnCancelPolicy,
            @Value("${scheduler.wait.on.shutdown}") boolean waitOnShutdown
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

    @Bean("arbitrageExecutor")
    public ThreadPoolTaskExecutor arbitrageExecutor(
            @Value("${listeners.executor.await.termination.iso}") String awaitTermination,
            @Value("${listeners.executor.pool.core.size}") int corePoolSize,
            @Value("${listeners.executor.pool.threads.allow.timeout}") boolean allowTimeout,
            @Value("${listeners.executor.name.prefix}") String namePrefix,
            @Value("${listeners.executor.wait.on.shutdown}") boolean waitOnShutdown
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
