package gr.aegean.icsd.icarus.util.annotations.GithubUrl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubUrlValidator implements ConstraintValidator<GithubUrl, String> {
    private Pattern pattern = Pattern.compile("^(https|http)://github\\.com/.*$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}
