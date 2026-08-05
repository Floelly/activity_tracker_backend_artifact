package dev.floelly.activitytrackerapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Base entity providing common audit timestamp management.
 * <p>
 * Inherited by all entities that track {@code createdAt} and {@code updatedAt}.
 * The mapped columns remain {@code created_at} and {@code updated_at} for every
 * inheriting entity, keeping the database schema unchanged.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseAuditEntity {

    @NotNull
    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected BaseAuditEntity(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}