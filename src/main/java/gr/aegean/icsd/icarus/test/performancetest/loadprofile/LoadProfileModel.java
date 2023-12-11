package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import gr.aegean.icsd.icarus.test.performancetest.PerformanceTest;
import org.springframework.hateoas.RepresentationModel;

public class LoadProfileModel extends RepresentationModel<LoadProfileModel> {


    private Integer loadTime;
    private Integer rampUp;
    private Integer concurrentUsers;
    private Integer startDelay;
    private Integer thinkTime;
    private PerformanceTest parentTest;



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

    public PerformanceTest getParentTest() {
        return parentTest;
    }

    public void setParentTest(PerformanceTest parentTest) {
        this.parentTest = parentTest;
    }


}
