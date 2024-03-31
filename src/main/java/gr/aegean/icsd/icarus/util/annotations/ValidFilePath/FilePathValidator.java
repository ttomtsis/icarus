package gr.aegean.icsd.icarus.util.annotations.ValidFilePath;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.io.File;

public class FilePathValidator implements ConstraintValidator<ValidFilePath, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        File file = new File(value);
        return file.exists() && file.isDirectory();
    }
}
