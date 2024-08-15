package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.models.ContractModel;

import java.util.Comparator;

public class ContractDTO implements DTO<ContractDTO> {
    private Integer id;
    private BlockchainDTO blockchain;
    private String address;
    private Integer currencyId;

    public ContractDTO(BlockchainDTO blockchain, int currencyId, String address) {
        this.blockchain = blockchain;
        this.currencyId = currencyId;
        this.address = address;
    }

    public ContractDTO(ContractModel contract, BlockchainDTO blockchain) {
        if (contract == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        this.blockchain = blockchain;
        this.id = contract.getId();
        this.address = contract.getAddress();
        this.currencyId = contract.getCurrencyId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BlockchainDTO getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(BlockchainDTO blockchain) {
        this.blockchain = blockchain;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "ContractDTO{" +
                "id=" + id +
                ", blockchain='" + blockchain + '\'' +
                ", currencyId=" + currencyId +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public int compareTo(ContractDTO o) {
        return Comparator.comparing(ContractDTO::getAddress)
                .thenComparing(ContractDTO::getBlockchain)
                .thenComparingInt(ContractDTO::getCurrencyId)
                .compare(this, o);
    }

    @Override
    public void updateFields(ContractDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.address != null)      address = newObject.address;
        if (newObject.currencyId != null)   currencyId = newObject.currencyId;
        if (newObject.blockchain != null)   blockchain.updateFields(newObject.blockchain);
    }
}
