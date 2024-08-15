package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.Comparator;

public class TickerModel implements DataObject<TickerModel> {
    private Integer id;
    private String ticker;

    public TickerModel() {}

    public TickerModel(TickerDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        this.id = dto.getId();
        this.ticker = dto.getTicker();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    @Override
    public String toString() {
        return "TickerModel {" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                "}";
    }

    @Override
    public int compareTo(TickerModel o) {
        return Comparator.comparing(TickerModel::getTicker).compare(this, o);
    }
}
