package de.hamburg.university.api.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.hamburg.university.api.validation.ValidationGroups;
import de.hamburg.university.api.validation.constraints.Zero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.Date;

@Data
public abstract class BaseDTO {


    @Null(groups = ValidationGroups.Post.class)
    @NotNull(groups = {ValidationGroups.Put.class, ValidationGroups.Patch.class, ValidationGroups.Delete.class})
    @PositiveOrZero(message = "Id has to be positive", groups = {ValidationGroups.Put.class, ValidationGroups.Patch.class, ValidationGroups.Delete.class})
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date updatedAt;

    @Zero(groups = ValidationGroups.Post.class)
    @NotNull(groups = {ValidationGroups.Put.class, ValidationGroups.Patch.class, ValidationGroups.Delete.class})
    @PositiveOrZero(message = "Version has to be positive", groups = {ValidationGroups.Put.class, ValidationGroups.Patch.class, ValidationGroups.Delete.class})
    private Long version = 0L;
}
