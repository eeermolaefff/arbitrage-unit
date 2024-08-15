package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.tools.PreparationTools;

import java.util.Comparator;

public class RelationModel implements DataObject<RelationModel> {
    private Integer id;
    private Integer marketId;
    private Integer currencyId;
    private Integer currencyTickerId;
    private String exchangeType;
    private String exchangeCategory;

    public RelationModel() {
    }

    public RelationModel(RelationDTO dto, TickerDTO ticker) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        this.id = dto.getId();
        this.marketId = dto.getMarketId();
        this.currencyId = dto.getCurrencyId();
        this.exchangeType = PreparationTools.exchangeTypePreparation(dto.getExchangeType());
        this.exchangeCategory = PreparationTools.exchangeCategoryPreparation(dto.getExchangeCategory());
        this.currencyTickerId = ticker.getId();
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

    public Integer getCurrencyTickerId() {
        return currencyTickerId;
    }

    public void setCurrencyTickerId(Integer currencyTickerId) {
        this.currencyTickerId = currencyTickerId;
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
    public String toString() {
        return "MarketCurrencyRelationModel{" +
                "id=" + id +
                ", marketId=" + marketId +
                ", currencyId=" + currencyId +
                ", currencyTickerId=" + currencyTickerId +
                ", exchangeType='" + exchangeType + '\'' +
                ", exchangeCategory='" + exchangeCategory + '\'' +
                '}';
    }

    @Override
    public int compareTo(RelationModel o) {
        return Comparator.comparingInt(RelationModel::getMarketId)
                .thenComparingInt(RelationModel::getCurrencyId)
                .thenComparing(RelationModel::getExchangeType)
                .thenComparing(RelationModel::getExchangeCategory)
                .compare(this, o);
    }
}