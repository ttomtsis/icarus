package gr.aegean.icsd.icarus.test.performancetest.loadprofile;

import org.springframework.hateoas.RepresentationModel;

public class LoadProfileModel extends RepresentationModel<LoadProfileModel> {


    private Long id;
    private Integer loadTime;
    private Integer rampUp;
    private Integer concurrentUsers;
    private Integer startDelay;
    private Integer thinkTime;
    private Long parentTest;



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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentTest() {
        return parentTest;
    }

    public void setParentTest(Long parentTest) {
        this.parentTest = parentTest;
    }


}
