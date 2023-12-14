package gr.aegean.icsd.icarus.util.annotations.GithubUrl;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = GithubUrlValidator.class)
@Documented
public @interface GithubUrl {
    String message() default "URL must be a github.com URL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

