package dev.floelly.activitytrackerapi.repository;

import dev.floelly.activitytrackerapi.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {
    Optional<Activity> findByBusinessId(String businessId);
}
