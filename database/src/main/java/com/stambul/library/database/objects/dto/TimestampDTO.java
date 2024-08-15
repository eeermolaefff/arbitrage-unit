package com.stambul.library.database.objects.dto;

import com.stambul.library.database.objects.interfaces.DTO;
import com.stambul.library.database.objects.interfaces.DataObject;
import com.stambul.library.database.objects.models.TimestampModel;

import java.sql.Timestamp;
import java.util.Comparator;

public class TimestampDTO implements DTO<TimestampDTO> {
    private Integer id;
    private Integer parentId;
    private Timestamp updatedAt;
    public TimestampDTO() {}

    public TimestampDTO(TimestampModel model) {
        this.id = model.getId();
        this.parentId = model.getParentId();
        this.updatedAt = model.getUpdatedAt();
    }

    public TimestampDTO(Integer currencyId) {
        this.parentId = currencyId;
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
        return "RelationTimestampDTO{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public int compareTo(TimestampDTO o) {
        return Comparator.comparing(TimestampDTO::getParentId).compare(this, o);
    }

    @Override
    public void updateFields(TimestampDTO newObject) {
        if (id != null && newObject.id != null && !id.equals(newObject.getId())) {
            String message = "Ids mismatch while updating: this=" + this + ", newObject=" + newObject;
            throw new IllegalArgumentException(message);
        }

        if (newObject.parentId != null)     parentId = newObject.parentId;
        if (newObject.updatedAt != null)    updatedAt = newObject.updatedAt;
    }
}
