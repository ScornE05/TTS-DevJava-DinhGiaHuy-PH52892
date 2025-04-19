package com.example.staffmanagement.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Getter
@Setter
public abstract class BaseEntity {

    private UUID id;

    private Byte status;

    private Long createdDate;

    private Long lastModifiedDate;

    public void initializeEntity() {
        createdDate = System.currentTimeMillis();
        lastModifiedDate = System.currentTimeMillis();
        if (status == null) {
            status = 1; // Default status is active (1)
        }
    }

    public void updateEntity() {
        lastModifiedDate = System.currentTimeMillis();
    }
}