package com.stambul.scanner.jobs.entities.spot;

import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.scanner.jobs.entities.interfaces.ParentJob;
import com.stambul.scanner.jobs.parsers.entities.interfaces.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class BybitJob extends ParentJob<BybitJob, OrderbookDTO> {
    @Autowired
    public BybitJob(
            Parser<BybitJob, OrderbookDTO> parser,
            @Qualifier("parsersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${bybit.spot.launch}") boolean launchFlag,
            @Value("${bybit.spot.start.delay.iso}") String startDelayISO,
            @Value("${bybit.spot.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${bybit.spot.update.delay.iso}") String updateDelayISO,
            @Value("${bybit.spot.repeat.delay.iso}") String repeatDelayISO
    ) {
        super(parser, scheduler, launchFlag, startDelayISO, scheduleDelayISO, updateDelayISO, repeatDelayISO);
    }
}

