package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.BlockchainDTO;
import com.stambul.library.database.objects.dto.RelationDTO;
import com.stambul.library.database.objects.dto.SwapDTO;
import com.stambul.library.database.objects.dto.TransferDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.Comparator;

public class TransferModel implements DataObject<TransferModel> {
    private Integer id;
    private Integer marketCurrencyRelationId, blockchainId;
    private Boolean withdrawEnable, depositEnable;
    private Double withdrawalStaticCommission, depositStaticCommission, withdrawalPercentCommission, depositPercentCommission;
    private String updatedAt;

    public TransferModel() {}

    public TransferModel(TransferDTO dto) {
        validate(dto);

        id = dto.getId();
        marketCurrencyRelationId = dto.getRelation().getId();
        blockchainId = dto.getBlockchain().getId();
        withdrawEnable = dto.getWithdrawEnable();
        depositEnable = dto.getDepositEnable();
        withdrawalStaticCommission = dto.getWithdrawalStaticCommission();
        depositStaticCommission = dto.getDepositStaticCommission();
        withdrawalPercentCommission = dto.getWithdrawalPercentCommission();
        depositPercentCommission = dto.getDepositPercentCommission();
    }

    private void validate(TransferDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        if (dto.getRelation() == null)
            throw new IllegalArgumentException("Null relation received: dto=" + dto);
        if (dto.getRelation().getId() == null)
            throw new IllegalArgumentException("Null relation id received: dto=" + dto);

        if (dto.getBlockchain() == null)
            throw new IllegalArgumentException("Null blockchain received: dto=" + dto);
        if (dto.getBlockchain().getId() == null)
            throw new IllegalArgumentException("Null blockchain id received: dto=" + dto);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMarketCurrencyRelationId() {
        return marketCurrencyRelationId;
    }

    public void setMarketCurrencyRelationId(Integer marketCurrencyRelationId) {
        this.marketCurrencyRelationId = marketCurrencyRelationId;
    }

    public Integer getBlockchainId() {
        return blockchainId;
    }

    public void setBlockchainId(Integer blockchainId) {
        this.blockchainId = blockchainId;
    }

    public Boolean getWithdrawEnable() {
        return withdrawEnable;
    }

    public void setWithdrawEnable(Boolean withdrawEnable) {
        this.withdrawEnable = withdrawEnable;
    }

    public Boolean getDepositEnable() {
        return depositEnable;
    }

    public void setDepositEnable(Boolean depositEnable) {
        this.depositEnable = depositEnable;
    }

    public Double getWithdrawalStaticCommission() {
        return withdrawalStaticCommission;
    }

    public void setWithdrawalStaticCommission(Double withdrawalStaticCommission) {
        this.withdrawalStaticCommission = withdrawalStaticCommission;
    }

    public Double getDepositStaticCommission() {
        return depositStaticCommission;
    }

    public void setDepositStaticCommission(Double depositStaticCommission) {
        this.depositStaticCommission = depositStaticCommission;
    }

    public Double getWithdrawalPercentCommission() {
        return withdrawalPercentCommission;
    }

    public void setWithdrawalPercentCommission(Double withdrawalPercentCommission) {
        this.withdrawalPercentCommission = withdrawalPercentCommission;
    }

    public Double getDepositPercentCommission() {
        return depositPercentCommission;
    }

    public void setDepositPercentCommission(Double depositPercentCommission) {
        this.depositPercentCommission = depositPercentCommission;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(TransferModel o) {
        return Comparator.comparingInt(TransferModel::getMarketCurrencyRelationId)
                .thenComparingInt(TransferModel::getBlockchainId)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return "TransferModel{" +
                "id=" + id +
                ", marketCurrencyRelationId=" + marketCurrencyRelationId +
                ", blockchainId=" + blockchainId +
                ", withdrawEnable=" + withdrawEnable +
                ", depositEnable=" + depositEnable +
                ", withdrawalStaticCommission=" + withdrawalStaticCommission +
                ", depositStaticCommission=" + depositStaticCommission +
                ", withdrawalPercentCommission=" + withdrawalPercentCommission +
                ", depositPercentCommission=" + depositPercentCommission +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
