package gr.aegean.icsd.icarus.util.exceptions.test;

import gr.aegean.icsd.icarus.util.enums.TestState;

import java.util.Arrays;

public class InvalidTestStateException extends RuntimeException {

    public InvalidTestStateException(Long testId, TestState invalidState, String... expectedStates) {

        super("Test with id: " + testId + " is not in a valid state for this operation. \n" +
                "Current State: " + invalidState.toString() + " , Expected States: " + Arrays.toString(expectedStates));
    }


}
