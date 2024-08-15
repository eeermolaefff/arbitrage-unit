package com.stambul.initializers.jobs.parsers.results;

import com.stambul.initializers.jobs.parsers.results.interfaces.LinearResults;
import com.stambul.library.database.objects.dto.TransferDTO;

public class TransferParsersResults extends LinearResults<TransferDTO> {
    public TransferParsersResults() {
        super(TransferDTO.class);
    }
}
