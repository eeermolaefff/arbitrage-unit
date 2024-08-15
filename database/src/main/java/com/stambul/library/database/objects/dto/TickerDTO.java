package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.TickerModel;

import java.util.Comparator;

public class TickerDTO implements DTO<TickerDTO> {
    private Integer id;
    private String ticker;

    public TickerDTO() {}

    public TickerDTO(String ticker) { this.ticker = ticker; }

    public TickerDTO(TickerModel model) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        this.id = model.getId();
        this.ticker = model.getTicker();
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
        return "TickerDTO{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                "}";
    }

    @Override
    public int compareTo(TickerDTO o) {
        return Comparator.comparing(TickerDTO::getTicker).compare(this, o);
    }

    @Override
    public void updateFields(TickerDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.ticker != null)   ticker = newObject.ticker;
    }
}
