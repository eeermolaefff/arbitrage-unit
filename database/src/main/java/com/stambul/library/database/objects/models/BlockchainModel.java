package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.BlockchainDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.util.Comparator;

public class BlockchainModel implements DataObject<BlockchainModel> {
    private Integer id;
    private String name;
    private Integer baseCoinId;
    private Double gas;

    public BlockchainModel() {}

    public BlockchainModel(BlockchainDTO dto) {
        if (dto == null)
            throw new IllegalArgumentException("DTO shouldn't be null");

        this.id = dto.getId();
        this.name = dto.getName();
        this.baseCoinId = dto.getBaseCoinId();
        this.gas =  dto.getGas();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBaseCoinId() {
        return baseCoinId;
    }

    public void setBaseCoinId(Integer baseCoinId) {
        this.baseCoinId = baseCoinId;
    }

    public Double getGas() {
        return gas;
    }

    public void setGas(Double gas) {
        this.gas = gas;
    }

    @Override
    public String toString() {
        return "BlockchainModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", baseCoinId=" + baseCoinId +
                ", gas=" + gas +
                '}';
    }

    @Override
    public int compareTo(BlockchainModel o) {
        return Comparator.comparing(BlockchainModel::getName).compare(this, o);
    }
}
