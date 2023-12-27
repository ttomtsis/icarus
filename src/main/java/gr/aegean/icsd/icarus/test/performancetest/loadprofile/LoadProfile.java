package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;


@Entity
public class LoadProfile {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Load Profile's load time cannot be null")
    @Positive(message = "Load time cannot be less than or equal to zero")
    private Integer loadTime;

    @NotNull(message = "Load Profile's ramp up cannot be null")
    @PositiveOrZero(message = "Ramp up cannot be less than zero")
    private Integer rampUp;

    @NotNull(message = "Load Profile's number of users cannot be null")
    @Positive(message = "Concurrent users cannot be less than or equal to zero")
    private Integer concurrentUsers;

    @NotNull(message = "Load Profile's start delay cannot be null")
    @PositiveOrZero(message = "Start delay cannot be less than zero")
    private Integer startDelay;

    @NotNull(message = "Load Profile's think time cannot be null")
    @PositiveOrZero(message = "Think time cannot be less than zero")
    private Integer thinkTime;

    @ManyToOne(targetEntity = PerformanceTest.class, optional = false)
    private PerformanceTest parentTest;



    public static LoadProfile createLoadProfileFromModel(LoadProfileModel model) {

        return new LoadProfile(null, model.getLoadTime(),
                model.getRampUp(), model.getConcurrentUsers(),
                model.getStartDelay(), model.getThinkTime());

    }



    public LoadProfile(PerformanceTest parentTest, Integer loadTime, Integer rampUp, Integer concurrentUsers,
                       Integer startDelay, Integer thinkTime) {
        this.loadTime = loadTime;
        this.rampUp = rampUp;
        this.concurrentUsers = concurrentUsers;
        this.startDelay = startDelay;
        this.thinkTime = thinkTime;
        this.parentTest = parentTest;
    }

    public LoadProfile() {}



    public void setId(Long id) {
        this.id = id;
    }

    public void setParentTest(PerformanceTest parentTest) {
        this.parentTest = parentTest;
    }

    public Integer getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(Integer loadTime) {
        this.loadTime = loadTime;
    }

    public Integer getRampUp() {
        return rampUp;
    }

    public void setRampUp(Integer rampUp) {
        this.rampUp = rampUp;
    }

    public Integer getConcurrentUsers() {
        return concurrentUsers;
    }

    public void setConcurrentUsers(Integer concurrentUsers) {
        this.concurrentUsers = concurrentUsers;
    }

    public Integer getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(Integer startDelay) {
        this.startDelay = startDelay;
    }

    public Integer getThinkTime() {
        return thinkTime;
    }

    public void setThinkTime(Integer thinkTime) {
        this.thinkTime = thinkTime;
    }

    public Long getId() {
        return id;
    }

    public PerformanceTest getParentTest() {
        return parentTest;
    }


}
