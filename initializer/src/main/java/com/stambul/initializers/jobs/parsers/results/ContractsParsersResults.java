package com.stambul.initializers.jobs.parsers.results;

import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.initializers.jobs.parsers.results.interfaces.SecondaryResults;

public class ContractsParsersResults extends SecondaryResults<CurrencyDTO, ContractDTO> {
    public ContractsParsersResults() {
        super(CurrencyDTO.class, ContractDTO.class);
    }
}