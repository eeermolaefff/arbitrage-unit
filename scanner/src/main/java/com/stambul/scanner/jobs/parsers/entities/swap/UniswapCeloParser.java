package com.stambul.scanner.jobs.parsers.entities.swap;


import com.stambul.library.tools.IOService;
import com.stambul.scanner.jobs.entities.swap.UniswapCeloJob;
import com.stambul.scanner.jobs.parsers.entities.swap.interfaces.UniswapParentParser;
import com.stambul.scanner.services.ConfigService;
import com.stambul.scanner.services.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class UniswapCeloParser extends UniswapParentParser<UniswapCeloJob> {

    @Autowired
    public UniswapCeloParser(
            IOService ioService,
            ConfigService configService,
            DatabaseService databaseService,
            ApplicationEventPublisher eventPublisher,
            @Value("${uniswap.swap.update.delay.iso}") String updateDelayISO
    ) {
        super(UniswapCeloJob.class, ioService, configService, databaseService, eventPublisher, updateDelayISO);
    }}
