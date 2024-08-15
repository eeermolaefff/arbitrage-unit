package com.stambul.scanner.jobs.parsers.entities.interfaces;


import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.scanner.jobs.entities.interfaces.Job;

public interface Parser<J extends Job, D extends DTO<D>> extends Runnable {
    void updateExchangeInfo();
}
