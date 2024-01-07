package gr.aegean.icsd.icarus.util.exceptions.controlleradvice;

import gr.aegean.icsd.icarus.util.exceptions.InvalidPasswordException;
import gr.aegean.icsd.icarus.util.exceptions.entity.EntityNotFoundException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidResourceConfigurationConfigurationException;
import gr.aegean.icsd.icarus.util.exceptions.entity.InvalidTestConfigurationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class CustomExceptionHandler {


    @ExceptionHandler({
            InvalidTestConfigurationException.class,
            InvalidResourceConfigurationConfigurationException.class,
            InvalidTestConfigurationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleInvalidTestConfigurationException(RuntimeException ex) {
        return ex.getMessage();
    }


    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleEntityNotFoundException(EntityNotFoundException ex) {
        return ex.getMessage();
    }


    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleInvalidPasswordException(InvalidPasswordException ex) {
        return ex.getMessage();
    }


}
