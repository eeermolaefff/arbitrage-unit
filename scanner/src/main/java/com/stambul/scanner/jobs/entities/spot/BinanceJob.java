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
public class BinanceJob extends ParentJob<BinanceJob, OrderbookDTO> {
    @Autowired
    public BinanceJob(
            Parser<BinanceJob, OrderbookDTO> parser,
            @Qualifier("parsersScheduler") ThreadPoolTaskScheduler scheduler,
            @Value("${binance.spot.launch}") boolean launchFlag,
            @Value("${binance.spot.start.delay.iso}") String startDelayISO,
            @Value("${binance.spot.schedule.delay.iso}") String scheduleDelayISO,
            @Value("${binance.spot.update.delay.iso}") String updateDelayISO,
            @Value("${binance.spot.repeat.delay.iso}") String repeatDelayISO
    ) {
        super(parser, scheduler, launchFlag, startDelayISO, scheduleDelayISO, updateDelayISO, repeatDelayISO);
    }
}

