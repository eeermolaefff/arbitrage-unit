package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.TickerDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.Comparator;

public class TradingPairModel implements DataObject<TradingPairModel> {
    private Integer id;
    private Integer marketId;
    private Integer baseAssetId;
    private Integer quoteAssetId;
    private Integer tickerId;
    private String updatedAt;

    public TradingPairModel() {}

    public TradingPairModel(TradingPairDTO dto, TickerDTO ticker) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        id = dto.getId();
        marketId = dto.getMarketId();
        baseAssetId = dto.getBaseAssetId();
        quoteAssetId = dto.getQuoteAssetId();
        tickerId = ticker.getId();
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

    public Integer getTickerId() {
        return tickerId;
    }

    public void setTickerId(Integer tickerId) {
        this.tickerId = tickerId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(TradingPairModel o) {
        return Comparator.comparing(TradingPairModel::getMarketId)
                .thenComparingInt(TradingPairModel::getBaseAssetId)
                .thenComparingInt(TradingPairModel::getQuoteAssetId)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "TradingPairModel{" +
                "id=" + id +
                ", marketId=" + marketId +
                ", baseAssetId=" + baseAssetId +
                ", quoteAssetId=" + quoteAssetId +
                ", tickerId=" + tickerId +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}