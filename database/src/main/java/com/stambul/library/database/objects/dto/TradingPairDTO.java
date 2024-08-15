package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.TradingPairModel;

import java.util.Comparator;

public class TradingPairDTO implements DTO<TradingPairDTO> {
    private Integer id;
    private Integer marketId;
    private Integer baseAssetId;
    private Integer quoteAssetId;
    private TickerDTO ticker;
    private String updatedAt;

    public TradingPairDTO() {}

    public TradingPairDTO(TradingPairModel model, TickerDTO ticker) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        id = model.getId();
        marketId = model.getMarketId();
        baseAssetId = model.getBaseAssetId();
        quoteAssetId = model.getQuoteAssetId();
        this.ticker = ticker;
        updatedAt = model.getUpdatedAt();
    }

    public TradingPairDTO(Integer marketId, Integer baseAssetId, Integer quoteAssetId, TickerDTO ticker) {
        this.marketId = marketId;
        this.baseAssetId = baseAssetId;
        this.quoteAssetId = quoteAssetId;
        this.ticker = ticker;
    }

    @Override
    public String toString() {
        return "TradingPairDTO{" +
                "id=" + id +
                ", marketId=" + marketId +
                ", baseAssetId=" + baseAssetId +
                ", quoteAssetId=" + quoteAssetId +
                ", ticker=" + ticker +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
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

    public Integer getBaseAssetId() {
        return baseAssetId;
    }

    public void setBaseAssetId(Integer baseAssetId) {
        this.baseAssetId = baseAssetId;
    }

    public Integer getQuoteAssetId() {
        return quoteAssetId;
    }

    public void setQuoteAssetId(Integer quoteAssetId) {
        this.quoteAssetId = quoteAssetId;
    }

    public TickerDTO getTicker() {
        return ticker;
    }

    public void setTicker(TickerDTO ticker) {
        this.ticker = ticker;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(TradingPairDTO o) {
        return Comparator.comparing(TradingPairDTO::getMarketId)
                .thenComparingInt(TradingPairDTO::getBaseAssetId)
                .thenComparingInt(TradingPairDTO::getQuoteAssetId)
                .compare(this, o);
    }

    @Override
    public void updateFields(TradingPairDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.marketId != null)     marketId = newObject.marketId;
        if (newObject.baseAssetId != null)  baseAssetId = newObject.baseAssetId;
        if (newObject.quoteAssetId != null) quoteAssetId = newObject.quoteAssetId;
        if (newObject.ticker != null)       ticker.updateFields(newObject.ticker);
        if (newObject.updatedAt != null)    updatedAt = newObject.updatedAt;
    }
}