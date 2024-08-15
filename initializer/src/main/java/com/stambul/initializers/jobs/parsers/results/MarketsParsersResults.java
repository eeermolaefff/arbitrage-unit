package com.stambul.initializers.jobs.parsers.results;

import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.initializers.jobs.parsers.results.interfaces.PrimaryResults;

public class MarketsParsersResults extends PrimaryResults<MarketDTO> {
    public MarketsParsersResults() {
        super(MarketDTO.class);
    }
}
