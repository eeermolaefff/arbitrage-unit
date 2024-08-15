package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.CurrencyDTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.tools.PreparationTools;

import java.sql.Timestamp;
import java.util.Comparator;

public class CurrencyModel implements DataObject<CurrencyModel> {
    private Integer id;
    private String slug;
    private String fullName;
    private String category;
    private Double cexVolumeUsd;
    private Double dexVolumeUsd;
    private Double marketCapUsd;
    private Boolean isActive;
    private Timestamp updatedAt;
    
    public CurrencyModel() {}

    public CurrencyModel(CurrencyDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        this.id = dto.getId();
        this.slug = PreparationTools.slugPreparation(dto.getSlug());
        this.category = PreparationTools.currencyCategoryPreparation(dto.getCategory());
        this.cexVolumeUsd = dto.getCexVolumeUsd();
        this.dexVolumeUsd = dto.getDexVolumeUsd();
        this.marketCapUsd = dto.getMarketCapUsd();
        this.fullName = dto.getFullName();
        this.isActive = dto.getIsActive();
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getCexVolumeUsd() {
        return cexVolumeUsd;
    }

    public void setCexVolumeUsd(Double cexVolumeUsd) {
        this.cexVolumeUsd = cexVolumeUsd;
    }

    public Double getDexVolumeUsd() {
        return dexVolumeUsd;
    }

    public void setDexVolumeUsd(Double dexVolumeUsd) {
        this.dexVolumeUsd = dexVolumeUsd;
    }

    public Double getMarketCapUsd() {
        return marketCapUsd;
    }

    public void setMarketCapUsd(Double marketCapUsd) {
        this.marketCapUsd = marketCapUsd;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CurrencyModel{" +
                "id=" + id +
                ", slug='" + slug + '\'' +
                ", category='" + category + '\'' +
                ", cexVolumeUsd=" + cexVolumeUsd +
                ", dexVolumeUsd=" + dexVolumeUsd +
                ", marketCapUsd=" + marketCapUsd +
                ", fullName='" + fullName + '\'' +
                ", isActive=" + isActive +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    @Override
    public int compareTo(CurrencyModel o) {
        return Comparator.comparing(CurrencyModel::getSlug).compare(this, o);
    }
}