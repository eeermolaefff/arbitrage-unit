package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.models.BlockchainModel;

import java.util.Comparator;

public class BlockchainDTO implements DTO<BlockchainDTO> {
    private Integer id;
    private String name;
    private Integer baseCoinId;
    private Double gas;

    public BlockchainDTO(BlockchainModel model) {
        if (model == null)
            throw new IllegalArgumentException("Model shouldn't be null");

        this.id = model.getId();
        this.name = model.getName();
        this.baseCoinId = model.getBaseCoinId();
        this.gas = model.getGas();
    }

    public BlockchainDTO(Integer id, String name, Integer baseCoinId) {
        this.id = id;
        this.name = name;
        this.baseCoinId = baseCoinId;
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
        return "BlockchainDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", baseCoinId=" + baseCoinId +
                ", gas=" + gas +
                '}';
    }

    @Override
    public int compareTo(BlockchainDTO o) {
        return Comparator.comparing(BlockchainDTO::getName).compare(this, o);
    }

    @Override
    public void updateFields(BlockchainDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.name != null)         name = newObject.name;
        if (newObject.baseCoinId != null)   baseCoinId = newObject.baseCoinId;
        if (newObject.gas != null)          gas = newObject.gas;
    }
}