package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.CurrencyModel;

import java.util.Comparator;

public class CurrencyDTO implements DTO<CurrencyDTO> {
    private Integer id;
    private String slug;
    private String category;
    private Double cexVolumeUsd;
    private Double dexVolumeUsd;
    private Double marketCapUsd;
    private String fullName;
    private Boolean isActive;
    private String updatedAt;
    private TimestampDTO contractsUpdatedAt;
    private TimestampDTO relationsUpdatedAt;

    public CurrencyDTO() {}

    public CurrencyDTO(String slug) {
        this.slug = slug;
    }

    public CurrencyDTO(CurrencyModel model, TimestampDTO contractsUpdatedAt, TimestampDTO relationsUpdatedAt) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        this.id = model.getId();
        this.slug = model.getSlug();
        this.category = model.getCategory();
        this.cexVolumeUsd = model.getCexVolumeUsd();
        this.dexVolumeUsd = model.getDexVolumeUsd();
        this.marketCapUsd = model.getMarketCapUsd();
        this.fullName = model.getFullName();
        this.isActive = model.getIsActive();
        this.updatedAt = model.getUpdatedAt().toString();
        this.contractsUpdatedAt = contractsUpdatedAt;
        this.relationsUpdatedAt = relationsUpdatedAt;
    }

    public CurrencyDTO(Integer id, String slug, String fullName, Boolean isActive, String category) {
        this.id = id;
        this.slug = slug;
        this.fullName = fullName;
        this.isActive = isActive;
        this.category = category;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TimestampDTO getContractsUpdatedAt() {
        return contractsUpdatedAt;
    }

    public TimestampDTO getRelationsUpdatedAt() {
        return relationsUpdatedAt;
    }

    public void setContractsUpdatedAt(TimestampDTO contractsUpdatedAt) {
        this.contractsUpdatedAt = contractsUpdatedAt;
    }

    public void setRelationsUpdatedAt(TimestampDTO relationsUpdatedAt) {
        this.relationsUpdatedAt = relationsUpdatedAt;
    }

    @Override
    public void updateFields(CurrencyDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.slug != null)                 slug = newObject.slug;
        if (newObject.category != null)             category = newObject.category;
        if (newObject.cexVolumeUsd != null)         cexVolumeUsd = newObject.cexVolumeUsd;
        if (newObject.dexVolumeUsd != null)         dexVolumeUsd = newObject.dexVolumeUsd;
        if (newObject.marketCapUsd != null)         marketCapUsd = newObject.marketCapUsd;
        if (newObject.fullName != null)             fullName = newObject.fullName;
        if (newObject.isActive != null)             isActive = newObject.isActive;
        if (newObject.contractsUpdatedAt != null)   contractsUpdatedAt.updateFields(newObject.contractsUpdatedAt);
        if (newObject.relationsUpdatedAt != null)   relationsUpdatedAt.updateFields(newObject.relationsUpdatedAt);
    }

    @Override
    public String toString() {
        return "CurrencyDTO{" +
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
    public int compareTo(CurrencyDTO o) {
        return Comparator.comparing(CurrencyDTO::getSlug).compare(this, o);
    }
}