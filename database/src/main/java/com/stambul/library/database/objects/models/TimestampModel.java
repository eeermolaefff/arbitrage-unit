package com.stambul.library.database.objects.models;

import com.stambul.library.database.objects.dto.TimestampDTO;
import com.stambul.library.database.objects.interfaces.DataObject;

import java.sql.Timestamp;
import java.util.Comparator;

public class TimestampModel implements DataObject<TimestampModel> {
    private Integer id;
    private Integer parentId;
    private Timestamp updatedAt;

    public TimestampModel() {}

    public TimestampModel(TimestampDTO dto) {
        this.id = dto.getId();
        this.parentId = dto.getParentId();
        this.updatedAt = dto.getUpdatedAt();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TimestampModel{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public int compareTo(TimestampModel o) {
        return Comparator.comparing(TimestampModel::getParentId).compare(this, o);
    }
}
