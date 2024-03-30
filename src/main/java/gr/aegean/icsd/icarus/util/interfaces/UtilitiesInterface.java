package gr.aegean.icsd.icarus.util.interfaces;

import gr.aegean.icsd.icarus.util.security.UserUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.function.Consumer;

import static gr.aegean.icsd.icarus.IcarusConfiguration.FUNCTION_SOURCES_DIRECTORY;


public interface UtilitiesInterface {


    default String getFunctionSourceDirectory() {
        return FUNCTION_SOURCES_DIRECTORY + File.separator + UserUtils.getUsername() + File.separator + "Functions";
    }


    default void setIfNotBlank(Consumer<String> setter, String value) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }


    default <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }


}
