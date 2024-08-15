package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.SwapModel;
import com.stambul.library.database.objects.models.TransferModel;

import java.util.Comparator;

public class TransferDTO implements DTO<TransferDTO> {
    private Integer id;
    private RelationDTO relation;
    private BlockchainDTO blockchain;
    private Boolean withdrawEnable, depositEnable;
    private Double withdrawalStaticCommission, depositStaticCommission, withdrawalPercentCommission, depositPercentCommission;
    private String updatedAt;

    public TransferDTO(TransferModel model, RelationDTO relation, BlockchainDTO blockchain) {
        validate(model, relation, blockchain);

        this.id = model.getId();
        this.relation = relation;
        this.blockchain = blockchain;
        this.withdrawEnable = model.getWithdrawEnable();
        this.depositEnable = model.getDepositEnable();
        this.withdrawalStaticCommission = model.getWithdrawalStaticCommission();
        this.depositStaticCommission = model.getDepositStaticCommission();
        this.withdrawalPercentCommission = model.getWithdrawalPercentCommission();
        this.depositPercentCommission = model.getDepositPercentCommission();
    }

    public TransferDTO(RelationDTO relation, BlockchainDTO blockchain, Boolean withdrawEnable, Boolean depositEnable,
                       Double withdrawalStaticCommission, Double depositStaticCommission,
                       Double withdrawalPercentCommission, Double depositPercentCommission) {
        validate(relation, blockchain);

        this.relation = relation;
        this.blockchain = blockchain;
        this.withdrawEnable = withdrawEnable;
        this.depositEnable = depositEnable;
        this.withdrawalStaticCommission = withdrawalStaticCommission;
        this.depositStaticCommission = depositStaticCommission;
        this.withdrawalPercentCommission = withdrawalPercentCommission;
        this.depositPercentCommission = depositPercentCommission;
    }

    private void validate(TransferModel model, RelationDTO relation, BlockchainDTO blockchain) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");
        validate(relation, blockchain);
    }

    private void validate(RelationDTO relation, BlockchainDTO blockchain) {
        if (relation == null)
            throw new IllegalArgumentException("Relation shouldn't be null");
        if (blockchain == null)
            throw new IllegalArgumentException("Blockchain base shouldn't be null");
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RelationDTO getRelation() {
        return relation;
    }

    public void setRelation(RelationDTO relation) {
        this.relation = relation;
    }

    public BlockchainDTO getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(BlockchainDTO blockchain) {
        this.blockchain = blockchain;
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
    public String toString() {
        return "TransferDTO{" +
                "id=" + id +
                ", relation=" + relation +
                ", blockchain=" + blockchain +
                ", withdrawEnable=" + withdrawEnable +
                ", depositEnable=" + depositEnable +
                ", withdrawalStaticCommission=" + withdrawalStaticCommission +
                ", depositStaticCommission=" + depositStaticCommission +
                ", withdrawalPercentCommission=" + withdrawalPercentCommission +
                ", depositPercentCommission=" + depositPercentCommission +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    @Override
    public void updateFields(TransferDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.relation != null)                     relation = newObject.relation;
        if (newObject.blockchain != null)                   blockchain = newObject.blockchain;
        if (newObject.withdrawEnable != null)               withdrawEnable = newObject.withdrawEnable;
        if (newObject.depositEnable != null)                depositEnable = newObject.depositEnable;
        if (newObject.withdrawalStaticCommission != null)   withdrawalStaticCommission = newObject.withdrawalStaticCommission;
        if (newObject.depositStaticCommission != null)      depositStaticCommission = newObject.depositStaticCommission;
        if (newObject.withdrawalPercentCommission != null)  withdrawalPercentCommission = newObject.withdrawalPercentCommission;
        if (newObject.depositPercentCommission != null)     depositPercentCommission = newObject.depositPercentCommission;
        if (newObject.updatedAt != null)                    updatedAt = newObject.updatedAt;
    }

    @Override
    public int compareTo(TransferDTO o) {
        return Comparator.comparing(TransferDTO::getRelation)
                .thenComparing(TransferDTO::getBlockchain)
                .compare(this, o);
    }
}
