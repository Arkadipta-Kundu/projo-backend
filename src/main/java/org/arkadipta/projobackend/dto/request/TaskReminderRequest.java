package org.arkadipta.projobackend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskReminderRequest {

    @NotNull(message = "Reminder time is required")
    @Future(message = "Reminder time must be in the future")
    private LocalDateTime reminderTime;
}
