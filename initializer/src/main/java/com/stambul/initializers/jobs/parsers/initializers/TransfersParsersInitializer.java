package com.stambul.initializers.jobs.parsers.initializers;

import com.stambul.initializers.jobs.entities.TransfersJob;
import com.stambul.initializers.jobs.parsers.entities.interfaces.SimpleParser;
import com.stambul.initializers.jobs.parsers.initializers.interfaces.PrimaryInitializer;
import com.stambul.library.database.objects.dto.TransferDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransfersParsersInitializer extends PrimaryInitializer<TransferDTO> {
    @Autowired
    public TransfersParsersInitializer(
            SimpleParser<TransferDTO> parser
    ) {
        super(TransfersJob.class, parser);
    }
}


