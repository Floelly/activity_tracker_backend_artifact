package dev.floelly.activitytrackerapi.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EntityDeletionConflictException extends RuntimeException {

    public EntityDeletionConflictException(String message) {
        super(message);
    }
}
