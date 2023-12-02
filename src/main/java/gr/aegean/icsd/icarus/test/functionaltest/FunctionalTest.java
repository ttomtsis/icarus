package gr.aegean.icsd.icarus.test.functionaltest;


import gr.aegean.icsd.icarus.test.Test;
import gr.aegean.icsd.icarus.util.enums.aws.AwsRegion;
import gr.aegean.icsd.icarus.util.enums.gcp.GcpRegion;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "functional_test")
public class FunctionalTest extends Test {

    @NotBlank(message = "Function URL cannot be blank")
    @URL(message = "Function URL is not a valid URL")
    private String functionURL;

    @NotNull(message = "Allocated memory cannot be null")
    @Positive(message = "Allocated memory must be positive")
    @Min(value = 128, message = "Minimum allocated memory must be at least 128MB")
    private Integer usedMemory;

    @NotBlank(message = "Region cannot be blank")
    private String region;


    public FunctionalTest(String functionURL, int memory, AwsRegion region) {
        this.functionURL = functionURL;
        this.usedMemory = memory;
        this.region = region.get();
    }

    public FunctionalTest(String functionURL, int memory, GcpRegion region) {
        this.functionURL = functionURL;
        this.usedMemory = memory;
        this.region = region.get();
    }

    public FunctionalTest() {}


}
