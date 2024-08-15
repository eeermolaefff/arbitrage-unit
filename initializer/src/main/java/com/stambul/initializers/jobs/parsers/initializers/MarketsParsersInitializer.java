package com.stambul.initializers.jobs.parsers.initializers;

import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.initializers.jobs.entities.MarketsJob;
import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import com.stambul.initializers.jobs.parsers.initializers.interfaces.PrimaryInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketsParsersInitializer extends PrimaryInitializer<MarketDTO> {
    @Autowired
    public MarketsParsersInitializer(
            SimpleParser<MarketDTO> parser
    ) {
        super(MarketsJob.class, parser);
    }
}


