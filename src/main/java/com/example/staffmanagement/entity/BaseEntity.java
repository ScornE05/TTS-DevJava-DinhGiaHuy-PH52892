package com.example.staffmanagement.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "status")
    private Byte status;

    @Column(name = "created_date")
    private Long createdDate;

    @Column(name = "last_modified_date")
    private Long lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = System.currentTimeMillis();
        lastModifiedDate = System.currentTimeMillis();
        if (status == null) {
            status = 1; // Default status is active (1)
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = System.currentTimeMillis();
    }
}