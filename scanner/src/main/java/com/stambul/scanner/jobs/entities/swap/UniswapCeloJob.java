package com.stambul.scanner.jobs.entities.swap;

import com.stambul.library.database.objects.dto.SwapDTO;
import com.stambul.scanner.jobs.entities.interfaces.ParentJob;
import com.stambul.scanner.jobs.parsers.entities.interfaces.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class UniswapCeloJob extends ParentJob<UniswapCeloJob, SwapDTO> {
    @Autowired
    public UniswapCeloJob(
            Parser<UniswapCeloJob, SwapDTO> parser,
            @Qualifier("parsersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${uniswap.swap.launch}") boolean launchFlag,
            @Value("${uniswap.swap.start.delay.iso}") String startDelayISO,
            @Value("${uniswap.swap.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${uniswap.swap.update.delay.iso}") String updateDelayISO,
            @Value("${uniswap.swap.repeat.delay.iso}") String repeatDelayISO
    ) {
        super(parser, scheduler, launchFlag, startDelayISO, scheduleDelayISO, updateDelayISO, repeatDelayISO);
    }
}

