package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.models.MarketModel;

import java.util.Comparator;

public class MarketDTO implements DTO<MarketDTO> {
    private Integer id;
    private String slug;
    private String fullName;
    private Double dailyVolumeUsd;
    private Double spotPercentCommission;
    private Double score;
    private Double trafficScore;
    private Integer liquidityScore;
    private Integer numberOfMarkets;
    private Integer numberOfCoins;
    private String dateLaunched;
    private String updatedAt;

    public MarketDTO() {}

    public MarketDTO(String slug) {
        this.slug = slug;
    }

    public MarketDTO(Integer id, String slug, String fullName) {
        this.id = id;
        this.slug = slug;
        this.fullName = fullName;
    }

    public MarketDTO(Integer id, String slug, String fullName, Double dailyVolumeUsd, Double spotPercentCommission,
                     Double score, Double trafficScore, Integer liquidityScore, Integer numberOfMarkets,
                     Integer numberOfCoins, String dateLaunched) {
        this.id = id;
        this.slug = slug;
        this.fullName = fullName;
        this.dailyVolumeUsd = dailyVolumeUsd;
        this.spotPercentCommission = spotPercentCommission;
        this.score = score;
        this.trafficScore = trafficScore;
        this.liquidityScore = liquidityScore;
        this.numberOfMarkets = numberOfMarkets;
        this.numberOfCoins = numberOfCoins;
        this.dateLaunched = dateLaunched;
    }

    public MarketDTO(MarketModel model) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        this.id = model.getId();
        this.slug = model.getSlug();
        this.fullName = model.getFullName();
        this.dailyVolumeUsd = model.getDailyVolumeUsd();
        this.spotPercentCommission = model.getSpotPercentCommission();
        this.score = model.getScore();
        this.trafficScore = model.getTrafficScore();
        this.liquidityScore = model.getLiquidityScore();
        this.numberOfMarkets = model.getNumberOfMarkets();
        this.numberOfCoins = model.getNumberOfCoins();
        this.dateLaunched = (model.getDateLaunched() == null) ? null : model.getDateLaunched().toString();
        this.updatedAt = (model.getUpdatedAt() == null) ? null : model.getUpdatedAt().toString();
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

    public String getDateLaunched() {
        return dateLaunched;
    }

    public void setDateLaunched(String dateLaunched) {
        this.dateLaunched = dateLaunched;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(MarketDTO o) {
        return Comparator.comparing(MarketDTO::getSlug).compare(this, o);
    }

    @Override
    public String toString() {
        return "MarketDTO{" +
                "id=" + id +
                ", slug='" + slug + '\'' +
                ", fullName='" + fullName + '\'' +
                ", dailyVolumeUsd=" + dailyVolumeUsd +
                ", spotPercentCommission=" + spotPercentCommission +
                ", score=" + score +
                ", trafficScore=" + trafficScore +
                ", liquidityScore=" + liquidityScore +
                ", numberOfMarkets=" + numberOfMarkets +
                ", numberOfCoins=" + numberOfCoins +
                ", dateLaunched='" + dateLaunched + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    @Override
    public void updateFields(MarketDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.slug != null)                     slug = newObject.slug;
        if (newObject.dailyVolumeUsd != null)           dailyVolumeUsd = newObject.dailyVolumeUsd;
        if (newObject.spotPercentCommission != null)    spotPercentCommission = newObject.spotPercentCommission;
        if (newObject.score != null)                    score = newObject.score;
        if (newObject.trafficScore != null)             trafficScore = newObject.trafficScore;
        if (newObject.liquidityScore != null)           liquidityScore = newObject.liquidityScore;
        if (newObject.fullName != null)                 fullName = newObject.fullName;
        if (newObject.numberOfMarkets != null)          numberOfMarkets = newObject.numberOfMarkets;
        if (newObject.numberOfCoins != null)            numberOfCoins = newObject.numberOfCoins;
        if (newObject.dateLaunched != null)             dateLaunched = newObject.dateLaunched;
        if (newObject.updatedAt != null)                updatedAt = newObject.updatedAt;
    }
}