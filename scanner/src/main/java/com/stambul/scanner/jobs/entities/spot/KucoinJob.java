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
public class KucoinJob extends ParentJob<KucoinJob, OrderbookDTO> {
    @Autowired
    public KucoinJob(
            Parser<KucoinJob, OrderbookDTO> parser,
            @Qualifier("parsersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${kucoin.spot.launch}") boolean launchFlag,
            @Value("${kucoin.spot.start.delay.iso}") String startDelayISO,
            @Value("${kucoin.spot.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${kucoin.spot.update.delay.iso}") String updateDelayISO,
            @Value("${kucoin.spot.repeat.delay.iso}") String repeatDelayISO
    ) {
        super(parser, scheduler, launchFlag, startDelayISO, scheduleDelayISO, updateDelayISO, repeatDelayISO);
    }
}

