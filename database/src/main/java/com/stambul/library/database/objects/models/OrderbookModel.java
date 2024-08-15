package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.OrderbookDTO;
import com.stambul.library.database.objects.dto.TradingPairDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.Comparator;

public class OrderbookModel implements DataObject<OrderbookModel> {
    private Integer id;
    private Integer tradingPairId;
    private Boolean isActive;
    private Double bidPrice;
    private Double askPrice;
    private Double bidQty;
    private Double askQty;
    private Double dailyVolumeUsd;
    private Double dailyVolumeBase;
    private Double dailyVolumeQuote;
    private Boolean isSpotTradingAllowed;
    private Boolean isMarginTradingAllowed;
    private String tradingType;
    private String updatedAt;

    public OrderbookModel() {}

    public OrderbookModel(OrderbookDTO dto, TradingPairDTO pair) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        id = dto.getId();
        tradingPairId = pair.getId();
        isActive = dto.getIsActive();
        bidPrice = dto.getBidPrice();
        askPrice = dto.getAskPrice();
        bidQty = dto.getBidQty();
        askQty = dto.getAskQty();
        dailyVolumeUsd = dto.getDailyVolumeUsd();
        dailyVolumeBase = dto.getDailyVolumeBase();
        dailyVolumeQuote = dto.getDailyVolumeQuote();
        isSpotTradingAllowed = dto.getIsSpotTradingAllowed();
        isMarginTradingAllowed = dto.getIsMarginTradingAllowed();
        tradingType = dto.getTradingType();
    }

    @Override
    public String toString() {
        return "OrderbookModel{" +
                "id=" + id +
                ", tradingPairId=" + tradingPairId +
                ", isActive=" + isActive +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                ", bidQty=" + bidQty +
                ", askQty=" + askQty +
                ", dailyVolumeUsd=" + dailyVolumeUsd +
                ", dailyVolumeBase=" + dailyVolumeBase +
                ", dailyVolumeQuote=" + dailyVolumeQuote +
                ", isSpotTradingAllowed=" + isSpotTradingAllowed +
                ", isMarginTradingAllowed=" + isMarginTradingAllowed +
                ", tradingType='" + tradingType + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Double getBidQty() {
        return bidQty;
    }

    public void setBidQty(Double bidQty) {
        this.bidQty = bidQty;
    }

    public Double getAskQty() {
        return askQty;
    }

    public void setAskQty(Double askQty) {
        this.askQty = askQty;
    }

    public Double getDailyVolumeUsd() {
        return dailyVolumeUsd;
    }

    public void setDailyVolumeUsd(Double dailyVolumeUsd) {
        this.dailyVolumeUsd = dailyVolumeUsd;
    }

    public Double getDailyVolumeBase() {
        return dailyVolumeBase;
    }

    public void setDailyVolumeBase(Double dailyVolumeBase) {
        this.dailyVolumeBase = dailyVolumeBase;
    }

    public Double getDailyVolumeQuote() {
        return dailyVolumeQuote;
    }

    public void setDailyVolumeQuote(Double dailyVolumeQuote) {
        this.dailyVolumeQuote = dailyVolumeQuote;
    }

    public Boolean getIsSpotTradingAllowed() {
        return isSpotTradingAllowed;
    }

    public void setIsSpotTradingAllowed(Boolean spotTradingAllowed) {
        isSpotTradingAllowed = spotTradingAllowed;
    }

    public Boolean getIsMarginTradingAllowed() {
        return isMarginTradingAllowed;
    }

    public void setIsMarginTradingAllowed(Boolean marginTradingAllowed) {
        isMarginTradingAllowed = marginTradingAllowed;
    }

    public String getTradingType() {
        return tradingType;
    }

    public void setTradingType(String tradingType) {
        this.tradingType = tradingType;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getTradingPairId() {
        return tradingPairId;
    }

    public void setTradingPairId(Integer tradingPairId) {
        this.tradingPairId = tradingPairId;
    }

    @Override
    public int compareTo(OrderbookModel o) {
        return Comparator.comparing(OrderbookModel::getTradingPairId)
                .compare(this, o);
    }
}