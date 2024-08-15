package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.BlockchainDTO;
import com.stambul.library.database.objects.dto.ContractDTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.tools.PreparationTools;

import java.util.Comparator;

public class ContractModel implements DataObject<ContractModel> {
    private Integer id;
    private Integer blockchainId;
    private Integer currencyId;
    private String address;

    public ContractModel() {}

    public ContractModel(ContractDTO dto, BlockchainDTO blockchain) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        this.id = dto.getId();
        this.blockchainId = blockchain.getId();
        this.currencyId = dto.getCurrencyId();
        this.address = PreparationTools.addressPreparation(dto.getAddress());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBlockchainId() {
        return blockchainId;
    }

    public void setBlockchainId(int blockchainId) {
        this.blockchainId = blockchainId;
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
        return "ContractModel{" +
                "id=" + id +
                ", blockchainId=" + blockchainId +
                ", currencyId=" + currencyId +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public int compareTo(ContractModel o) {
        return Comparator.comparing(ContractModel::getAddress)
                .thenComparingInt(ContractModel::getBlockchainId)
                .thenComparingInt(ContractModel::getCurrencyId)
                .compare(this, o);
    }
}
