package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.RelationModel;

import java.util.Comparator;

public class RelationDTO implements DTO<RelationDTO> {
    private Integer id;
    private Integer marketId;
    private Integer currencyId;
    private TickerDTO ticker;
    private String exchangeType;
    private String exchangeCategory;

    public RelationDTO() {}

    public RelationDTO(Integer marketId, String exchangeType, String exchangeCategory) {
        this.marketId = marketId;
        this.exchangeCategory = exchangeCategory;
        this.exchangeType = exchangeType;
    }

    public RelationDTO(RelationModel model, TickerDTO ticker) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        this.id = model.getId();
        this.marketId = model.getMarketId();
        this.currencyId = model.getCurrencyId();
        this.exchangeCategory = model.getExchangeCategory();
        this.exchangeType = model.getExchangeType();
        this.ticker = ticker;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMarketId() {
        return marketId;
    }

    public void setMarketId(Integer marketId) {
        this.marketId = marketId;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public TickerDTO getTicker() {
        return ticker;
    }

    public void setTicker(TickerDTO ticker) {
        this.ticker = ticker;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getExchangeCategory() {
        return exchangeCategory;
    }

    public void setExchangeCategory(String exchangeCategory) {
        this.exchangeCategory = exchangeCategory;
    }

    @Override
    public int compareTo(RelationDTO o) {
        return Comparator.comparingInt(RelationDTO::getMarketId)
                .thenComparingInt(RelationDTO::getCurrencyId)
                .thenComparing(RelationDTO::getExchangeType)
                .thenComparing(RelationDTO::getExchangeCategory)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "MarketCurrencyRelationDTO{" +
                "id=" + id +
                ", marketId=" + marketId +
                ", currencyId=" + currencyId +
                ", ticker='" + ticker + '\'' +
                ", exchangeType='" + exchangeType + '\'' +
                ", exchangeCategory='" + exchangeCategory + '\'' +
                '}';
    }

    @Override
    public void updateFields(RelationDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.marketId != null)         marketId = newObject.marketId;
        if (newObject.currencyId != null)       currencyId = newObject.currencyId;
        if (newObject.exchangeType != null)     exchangeType = newObject.exchangeType;
        if (newObject.exchangeCategory != null) exchangeCategory = newObject.exchangeCategory;
        if (newObject.ticker != null)           ticker.updateFields(newObject.ticker);
    }
}