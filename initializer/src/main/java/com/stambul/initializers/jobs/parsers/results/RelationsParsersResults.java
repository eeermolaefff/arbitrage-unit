package com.stambul.initializers.jobs.parsers.results;

import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.initializers.jobs.parsers.results.interfaces.SecondaryResults;

public class RelationsParsersResults extends SecondaryResults<CurrencyDTO, RelationDTO> {

    public RelationsParsersResults() {
        super(CurrencyDTO.class, RelationDTO.class);
    }
}