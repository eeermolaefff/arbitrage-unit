package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.models.SwapModel;

import java.util.Comparator;

public class SwapDTO implements DTO<SwapDTO> {
    private Integer id;
    private TradingPairDTO tradingPair;
    private ContractDTO baseContract;
    private ContractDTO quoteContract;
    private Boolean isActive;
    private Double basePrice;
    private Double quotePrice;
    private Double feePercentage;
    private Double liquidity;
    private String hash;
    private Double dailyVolumeUsd;
    private Double dailyVolumeBase;
    private Double dailyVolumeQuote;
    private Double tvlUsd;
    private Double tvlBase;
    private Double tvlQuote;
    private String tradingType;
    private String updatedAt;

    public SwapDTO(SwapModel model, TradingPairDTO pair, ContractDTO base, ContractDTO quote) {
        validate(model, pair, base, quote);

        id = model.getId();
        tradingPair = pair;
        baseContract = base;
        quoteContract = quote;
        isActive = model.getIsActive();
        basePrice = model.getBasePrice();
        quotePrice = model.getQuotePrice();
        feePercentage = model.getFeePercentage();
        liquidity = model.getLiquidity();
        hash = model.getHash();
        dailyVolumeUsd = model.getDailyVolumeUsd();
        dailyVolumeBase = model.getDailyVolumeBase();
        dailyVolumeQuote = model.getDailyVolumeQuote();
        tvlUsd = model.getTvlUsd();
        tvlBase = model.getTvlBase();
        tvlQuote = model.getTvlQuote();
        tradingType = model.getTradingType();
        updatedAt = model.getUpdatedAt();
    }

    public SwapDTO(TradingPairDTO tradingPair, ContractDTO baseContract, ContractDTO quoteContract, Boolean isActive,
                   Double basePrice, Double quotePrice, Double feePercentage, Double liquidity,
                   String hash, Double tvlUsd, Double tvlBase, Double tvlQuote, Double dailyVolumeUsd,
                   Double dailyVolumeBase, Double dailyVolumeQuote, String tradingType) {
        validate(tradingPair, baseContract, quoteContract);

        this.tradingPair = tradingPair;
        this.baseContract = baseContract;
        this.quoteContract = quoteContract;
        this.isActive = isActive;
        this.basePrice = basePrice;
        this.quotePrice = quotePrice;
        this.feePercentage = feePercentage;
        this.liquidity = liquidity;
        this.hash = hash;
        this.tvlUsd = tvlUsd;
        this.tvlBase = tvlBase;
        this.tvlQuote = tvlQuote;
        this.dailyVolumeUsd = dailyVolumeUsd;
        this.dailyVolumeBase = dailyVolumeBase;
        this.dailyVolumeQuote = dailyVolumeQuote;
        this.tradingType = tradingType;
    }

    private void validate(SwapModel model, TradingPairDTO pair, ContractDTO base, ContractDTO quote) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");
        validate(pair, base, quote);
    }

    private void validate(TradingPairDTO pair, ContractDTO base, ContractDTO quote) {
        if (pair == null)
            throw new IllegalArgumentException("Pair shouldn't be null");
        if (base == null)
            throw new IllegalArgumentException("Contract base shouldn't be null");
        if (quote == null)
            throw new IllegalArgumentException("Contract quote shouldn't be null");
    }

    public ContractDTO getBaseContract() {
        return baseContract;
    }

    public void setBaseContract(ContractDTO baseContract) {
        this.baseContract = baseContract;
    }

    public ContractDTO getQuoteContract() {
        return quoteContract;
    }

    public void setQuoteContract(ContractDTO quoteContract) {
        this.quoteContract = quoteContract;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public TradingPairDTO getTradingPair() {
        return tradingPair;
    }

    public void setTradingPair(TradingPairDTO tradingPair) {
        this.tradingPair = tradingPair;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getQuotePrice() {
        return quotePrice;
    }

    public void setQuotePrice(Double quotePrice) {
        this.quotePrice = quotePrice;
    }

    public Double getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(Double liquidity) {
        this.liquidity = liquidity;
    }

    public Double getTvlUsd() {
        return tvlUsd;
    }

    public void setTvlUsd(Double tvlUsd) {
        this.tvlUsd = tvlUsd;
    }

    public Double getTvlBase() {
        return tvlBase;
    }

    public void setTvlBase(Double tvlBase) {
        this.tvlBase = tvlBase;
    }

    public Double getTvlQuote() {
        return tvlQuote;
    }

    public void setTvlQuote(Double tvlQuote) {
        this.tvlQuote = tvlQuote;
    }

    public Double getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(Double feePercentage) {
        this.feePercentage = feePercentage;
    }

    @Override
    public int compareTo(SwapDTO o) {
        return Comparator.comparing(SwapDTO::getTradingPair)
                .thenComparing(SwapDTO::getBaseContract)
                .thenComparing(SwapDTO::getQuoteContract)
                .thenComparingDouble(SwapDTO::getFeePercentage)
                .compare(this, o);
    }

    @Override
    public void updateFields(SwapDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.isActive != null)         isActive = newObject.isActive;
        if (newObject.basePrice != null)        basePrice = newObject.basePrice;
        if (newObject.quotePrice != null)       quotePrice = newObject.quotePrice;
        if (newObject.liquidity != null)        liquidity = newObject.liquidity;
        if (newObject.hash != null)             hash = newObject.hash;
        if (newObject.feePercentage != null)    feePercentage = newObject.feePercentage;
        if (newObject.dailyVolumeUsd != null)   dailyVolumeUsd = newObject.dailyVolumeUsd;
        if (newObject.dailyVolumeBase != null)  dailyVolumeBase = newObject.dailyVolumeBase;
        if (newObject.dailyVolumeQuote != null) dailyVolumeQuote = newObject.dailyVolumeQuote;
        if (newObject.tvlUsd != null)           tvlUsd = newObject.tvlUsd;
        if (newObject.tvlBase != null)          tvlBase = newObject.tvlBase;
        if (newObject.tvlQuote != null)         tvlQuote = newObject.tvlQuote;
        if (newObject.tradingType != null)      tradingType = newObject.tradingType;
        if (newObject.tradingPair != null)      tradingPair.updateFields(newObject.tradingPair);
        if (newObject.baseContract != null)     baseContract.updateFields(newObject.baseContract);
        if (newObject.quoteContract != null)    quoteContract.updateFields(newObject.quoteContract);
        if (newObject.updatedAt != null)        updatedAt = newObject.updatedAt;
    }

    @Override
    public String toString() {
        return "SwapDTO{" +
                "id=" + id +
                ", tradingPair=" + tradingPair +
                ", baseContract=" + baseContract +
                ", quoteContract=" + quoteContract +
                ", isActive=" + isActive +
                ", basePrice=" + basePrice +
                ", quotePrice=" + quotePrice +
                ", feePercentage=" + feePercentage +
                ", liquidity=" + liquidity +
                ", hash='" + hash + '\'' +
                ", dailyVolumeUsd=" + dailyVolumeUsd +
                ", dailyVolumeBase=" + dailyVolumeBase +
                ", dailyVolumeQuote=" + dailyVolumeQuote +
                ", tvlUsd=" + tvlUsd +
                ", tvlBase=" + tvlBase +
                ", tvlQuote=" + tvlQuote +
                ", tradingType='" + tradingType + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}