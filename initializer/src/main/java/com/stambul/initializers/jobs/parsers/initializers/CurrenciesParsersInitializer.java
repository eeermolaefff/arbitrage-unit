package com.stambul.initializers.jobs.parsers.initializers;

import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.initializers.jobs.entities.CurrenciesJob;

import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import com.stambul.initializers.jobs.parsers.initializers.interfaces.PrimaryInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrenciesParsersInitializer extends PrimaryInitializer<CurrencyDTO> {
    @Autowired
    public CurrenciesParsersInitializer(
           SimpleParser<CurrencyDTO> parser
    ) {
        super(CurrenciesJob.class, parser);
    }
}



