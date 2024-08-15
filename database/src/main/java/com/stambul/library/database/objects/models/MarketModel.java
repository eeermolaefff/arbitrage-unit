package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.MarketDTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.tools.PreparationTools;
import com.stambul.library.tools.TimeTools;

import java.sql.Timestamp;
import java.util.Comparator;

public class MarketModel implements DataObject<MarketModel> {
    private Integer id;
    private String slug;
    private String fullName;
    private Double dailyVolumeUsd;
    private Double score;
    private Double trafficScore;
    private Double spotPercentCommission;
    private Integer liquidityScore;
    private Integer numberOfMarkets;
    private Integer numberOfCoins;
    private Timestamp dateLaunched;
    private Timestamp updatedAt;

    public MarketModel() {}

    public MarketModel(MarketDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        this.id = dto.getId();
        this.slug = PreparationTools.slugPreparation(dto.getSlug());
        this.fullName =  dto.getFullName();
        this.dailyVolumeUsd = dto.getDailyVolumeUsd();
        this.score = dto.getScore();
        this.spotPercentCommission = dto.getSpotPercentCommission();
        this.trafficScore = dto.getTrafficScore();
        this.liquidityScore = dto.getLiquidityScore();
        this.numberOfMarkets = dto.getNumberOfMarkets();
        this.numberOfCoins = dto.getNumberOfCoins();
        this.dateLaunched = TimeTools.toPsqlTimestamp(dto.getDateLaunched());
    }

    @Override
    public String toString() {
        return "MarketModel{" +
                "id=" + id +
                ", slug='" + slug + '\'' +
                ", fullName='" + fullName + '\'' +
                ", dailyVolumeUsd=" + dailyVolumeUsd +
                ", score=" + score +
                ", spotPercentCommission=" + spotPercentCommission +
                ", trafficScore=" + trafficScore +
                ", liquidityScore=" + liquidityScore +
                ", numberOfMarkets=" + numberOfMarkets +
                ", numberOfCoins=" + numberOfCoins +
                ", dateLaunched='" + dateLaunched + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    public Double getSpotPercentCommission() {
        return spotPercentCommission;
    }

    public void setSpotPercentCommission(Double spotPercentCommission) {
        this.spotPercentCommission = spotPercentCommission;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Double getDailyVolumeUsd() {
        return dailyVolumeUsd;
    }

    public void setDailyVolumeUsd(Double dailyVolumeUsd) {
        this.dailyVolumeUsd = dailyVolumeUsd;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getTrafficScore() {
        return trafficScore;
    }

    public void setTrafficScore(Double trafficScore) {
        this.trafficScore = trafficScore;
    }

    public Integer getLiquidityScore() {
        return liquidityScore;
    }

    public void setLiquidityScore(Integer liquidityScore) {
        this.liquidityScore = liquidityScore;
    }

    public Integer getNumberOfMarkets() {
        return numberOfMarkets;
    }

    public void setNumberOfMarkets(Integer numberOfMarkets) {
        this.numberOfMarkets = numberOfMarkets;
    }

    public Integer getNumberOfCoins() {
        return numberOfCoins;
    }

    public void setNumberOfCoins(Integer numberOfCoins) {
        this.numberOfCoins = numberOfCoins;
    }

    public Timestamp getDateLaunched() {
        return dateLaunched;
    }

    public void setDateLaunched(Timestamp dateLaunched) {
        this.dateLaunched = dateLaunched;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(MarketModel o) {
        return Comparator.comparing(MarketModel::getSlug).compare(this, o);
    }
}