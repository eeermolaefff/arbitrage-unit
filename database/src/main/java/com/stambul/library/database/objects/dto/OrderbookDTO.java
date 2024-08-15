package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.OrderbookModel;

import java.util.Comparator;

public class OrderbookDTO implements DTO<OrderbookDTO> {
    private Integer id;
    private TradingPairDTO tradingPair;
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

    public OrderbookDTO() {}

    public OrderbookDTO(OrderbookModel model, TradingPairDTO tradingPair) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        id = model.getId();
        this.tradingPair = tradingPair;
        isActive = model.getIsActive();
        bidPrice = model.getBidPrice();
        askPrice = model.getAskPrice();
        bidQty = model.getBidQty();
        askQty = model.getAskQty();
        dailyVolumeUsd = model.getDailyVolumeUsd();
        dailyVolumeBase = model.getDailyVolumeBase();
        dailyVolumeQuote = model.getDailyVolumeQuote();
        isSpotTradingAllowed = model.getIsSpotTradingAllowed();
        isMarginTradingAllowed = model.getIsMarginTradingAllowed();
        tradingType = model.getTradingType();
        updatedAt = model.getUpdatedAt();
    }

    public OrderbookDTO(TradingPairDTO tradingPair, Boolean isActive, Double bidPrice, Double askPrice, Double bidQty,
                        Double askQty, Double dailyVolumeUsd, Double dailyVolumeBase, Double dailyVolumeQuote,
                        Boolean isSpotTradingAllowed, Boolean isMarginTradingAllowed, String tradingType) {
        this.tradingPair = tradingPair;
        this.isActive = isActive;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.bidQty = bidQty;
        this.askQty = askQty;
        this.dailyVolumeUsd = dailyVolumeUsd;
        this.dailyVolumeBase = dailyVolumeBase;
        this.dailyVolumeQuote = dailyVolumeQuote;
        this.isSpotTradingAllowed = isSpotTradingAllowed;
        this.isMarginTradingAllowed = isMarginTradingAllowed;
        this.tradingType = tradingType;
    }

    public TradingPairDTO getTradingPair() {
        return tradingPair;
    }

    public void setTradingPair(TradingPairDTO tradingPair) {
        this.tradingPair = tradingPair;
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

    @Override
    public int compareTo(OrderbookDTO o) {
        return Comparator.comparing(OrderbookDTO::getTradingPair)
                .compare(this, o);
    }

    @Override
    public void updateFields(OrderbookDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.isActive != null)                  isActive = newObject.isActive;
        if (newObject.bidPrice != null)                  bidPrice = newObject.bidPrice;
        if (newObject.askPrice != null)                  askPrice = newObject.askPrice;
        if (newObject.bidQty != null)                    bidQty = newObject.bidQty;
        if (newObject.askQty != null)                    askQty = newObject.askQty;
        if (newObject.dailyVolumeUsd != null)            dailyVolumeUsd = newObject.dailyVolumeUsd;
        if (newObject.dailyVolumeBase != null)           dailyVolumeBase = newObject.dailyVolumeBase;
        if (newObject.dailyVolumeQuote != null)          dailyVolumeQuote = newObject.dailyVolumeQuote;
        if (newObject.isSpotTradingAllowed != null)      isSpotTradingAllowed = newObject.isSpotTradingAllowed;
        if (newObject.isMarginTradingAllowed != null)    isMarginTradingAllowed = newObject.isMarginTradingAllowed;
        if (newObject.tradingType != null)               tradingType = newObject.tradingType;
        if (newObject.tradingPair != null)               tradingPair.updateFields(newObject.tradingPair);
        if (newObject.updatedAt != null)                 updatedAt = newObject.updatedAt;
    }

    @Override
    public String toString() {
        return "OrderbookDTO{" +
                "id=" + id +
                ", pair=" + tradingPair +
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
}