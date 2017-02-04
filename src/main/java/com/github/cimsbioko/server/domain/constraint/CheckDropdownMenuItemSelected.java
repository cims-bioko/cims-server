package com.github.cimsbioko.server.domain.constraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.github.cimsbioko.server.domain.constraint.impl.CheckDropdownMenuItemSelectedImpl;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CheckDropdownMenuItemSelectedImpl.class)
@Documented
public @interface CheckDropdownMenuItemSelected {

    String message() default "Please select an option";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

