package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.SwapDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.Comparator;

public class SwapModel implements DataObject<SwapModel> {
    private Integer id;
    private Integer tradingPairId;
    private Integer baseContractId;
    private Integer quoteContractId;
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

    public SwapModel() {}

    public SwapModel(SwapDTO dto) {
        validate(dto);

        id = dto.getId();
        tradingPairId = dto.getTradingPair().getId();
        baseContractId = dto.getBaseContract().getId();
        quoteContractId = dto.getQuoteContract().getId();
        isActive = dto.getIsActive();
        basePrice = dto.getBasePrice();
        quotePrice = dto.getQuotePrice();
        feePercentage = dto.getFeePercentage();
        liquidity = dto.getLiquidity();
        hash = dto.getHash();
        dailyVolumeUsd = dto.getDailyVolumeUsd();
        dailyVolumeBase = dto.getDailyVolumeBase();
        dailyVolumeQuote = dto.getDailyVolumeQuote();
        tvlUsd = dto.getTvlUsd();
        tvlBase = dto.getTvlBase();
        tvlQuote = dto.getTvlQuote();
        tradingType = dto.getTradingType();
    }

    private void validate(SwapDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        if (dto.getTradingPair() == null)
            throw new IllegalArgumentException("Null trading pair received: dto=" + dto);
        if (dto.getTradingPair().getId() == null)
            throw new IllegalArgumentException("Null trading pair id received: dto=" + dto);

        if (dto.getBaseContract() == null)
            throw new IllegalArgumentException("Null base contract received: dto=" + dto);
        if (dto.getBaseContract().getId() == null)
            throw new IllegalArgumentException("Null base contract id received: dto=" + dto);
        if (dto.getQuoteContract() == null)
            throw new IllegalArgumentException("Null quote contract received: dto=" + dto);
        if (dto.getQuoteContract().getId() == null)
            throw new IllegalArgumentException("Null quote contract id received: dto=" + dto);
    }

    public Integer getBaseContractId() {
        return baseContractId;
    }

    public void setBaseContractId(Integer baseContractId) {
        this.baseContractId = baseContractId;
    }

    public Integer getQuoteContractId() {
        return quoteContractId;
    }

    public void setQuoteContractId(Integer quoteContractId) {
        this.quoteContractId = quoteContractId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Double getFeePercentage() {
        return feePercentage;
    }

    public void setFeePercentage(Double feePercentage) {
        this.feePercentage = feePercentage;
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

    public Integer getTradingPairId() {
        return tradingPairId;
    }

    public void setTradingPairId(Integer tradingPairId) {
        this.tradingPairId = tradingPairId;
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

    @Override
    public String toString() {
        return "SwapModel{" +
                "id=" + id +
                ", tradingPairId=" + tradingPairId +
                ", baseContractId=" + baseContractId +
                ", quoteContractId=" + quoteContractId +
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

    @Override
    public int compareTo(SwapModel o) {
        return Comparator.comparing(SwapModel::getTradingPairId)
                .thenComparingInt(SwapModel::getBaseContractId)
                .thenComparingInt(SwapModel::getQuoteContractId)
                .thenComparingDouble(SwapModel::getFeePercentage)
                .compare(this, o);
    }
}