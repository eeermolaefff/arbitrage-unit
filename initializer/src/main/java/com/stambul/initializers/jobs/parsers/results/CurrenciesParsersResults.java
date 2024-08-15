package com.stambul.initializers.jobs.parsers.results;


import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.initializers.jobs.parsers.results.interfaces.PrimaryResults;

public class CurrenciesParsersResults extends PrimaryResults<CurrencyDTO> {
    public CurrenciesParsersResults() {
        super(CurrencyDTO.class);
    }
}
