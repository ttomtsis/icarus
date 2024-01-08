package gr.aegean.icsd.icarus.util.exceptions.controlleradvice;

import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class SqlExceptionHandler {


    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        String errorMessage = ex.getMostSpecificCause().getMessage();
        String responseMessage = "An error occurred: ";

        if (errorMessage.contains("duplicate key value violates unique constraint")) {
            String field = errorMessage.split("\\(")[1].split("\\)")[0];
            String value = errorMessage.split("=")[1].split(" ")[0];
            responseMessage += "Duplicate field: " + field + "\n With value: " + value + ", already exists in the database.";
        }

        else if (errorMessage.contains("null value in column")) {
            String field = errorMessage.split("null value in column \"")[1].split("\"")[0];
            responseMessage += "Null value violates not-null constraint for field: " + field;

        } else if (errorMessage.contains("not-null property references a null or transient value")) {
            responseMessage += "A required field is null.";

        } else if (errorMessage.contains("could not execute statement")) {
            responseMessage += "There was a problem executing a statement.";
            LoggerFactory.getLogger(SqlExceptionHandler.class).error(ex.getMessage());
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);

        } else {
            responseMessage += errorMessage;
            LoggerFactory.getLogger(SqlExceptionHandler.class).error("SQL Error occurred: {}", responseMessage);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }


}
