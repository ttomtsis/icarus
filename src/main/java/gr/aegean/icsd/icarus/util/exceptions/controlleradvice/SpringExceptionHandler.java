package gr.aegean.icsd.icarus.util.exceptions.controlleradvice;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class SpringExceptionHandler {


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleConstraintViolationException(ConstraintViolationException ex) {

        StringBuilder message = new StringBuilder();

        ex.getConstraintViolations().forEach(constraintViolation -> {
            message.append(constraintViolation.getPropertyPath()).append(" ");
            message.append(constraintViolation.getMessage()).append("\n");
        });

        return message.toString();
    }


}
