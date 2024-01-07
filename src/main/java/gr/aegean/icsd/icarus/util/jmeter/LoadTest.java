package gr.aegean.icsd.icarus.util.jmeter;

import gr.aegean.icsd.icarus.util.exceptions.async.TestExecutionFailedException;
import io.micrometer.common.util.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.UniformRandomTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.util.UUID;

import static gr.aegean.icsd.icarus.util.jmeter.JMeterConfiguration.JMETER_HOME_DIRECTORY;
import static gr.aegean.icsd.icarus.util.jmeter.JMeterConfiguration.JMETER_PROPERTIES_FILE;


/**
 * Represents a JMeter Load Test <br><br>
 *
 * By default, a load test consists of a single load profile
 * that is created upon class instantiation. <br><br>
 *
 * A single Load Test may consist of several Load Profiles, each of which can be added later by
 * invoking {@link LoadTest#addLoadProfile(int, int, int, int, int) addLoadProfile} <br><br>
 */
public class LoadTest {

    private final String testName;

    private final String functionURL;
    private final RequestMethod functionMethod;

    private final StandardJMeterEngine jmeter;

    private final File jmeterHome;
    private final File jmeterProperties;

    private final HashTree testPlanTree;
    private final TestPlan testPlan;



    /**
     * Default constructor, used to instantiate the class and generate the default load profile
     *
     * @param name Name of the Load Test
     *
     * @param url url that invokes the target function
     * @param path Path that the function exposes ( optional )
     * @param pathVariable Path variable that a function exposes ( optional )
     * @param pathVariableValue Value that will be associated with the path variable
     *                          when invoking the function
     *
     * @param invokeHTTPMethod HTTP Method that invokes the function
     */
    public LoadTest(String name, String url, String path, String pathVariable, String pathVariableValue,
                    RequestMethod invokeHTTPMethod) {

        this.testName = name;

        if (StringUtils.isBlank(path)) {
            this.functionURL = url;
        }
        else {
            path = path.replace("{" + pathVariable + "}", pathVariableValue);
            this.functionURL = url + path;
        }

        this.functionMethod = RequestMethod.valueOf(invokeHTTPMethod.toString());

        this.jmeterHome = new File(JMETER_HOME_DIRECTORY);
        this.jmeterProperties = new File(JMETER_PROPERTIES_FILE);

        this.jmeter = initJMeter();

        this.testPlan = createTestPlan();
        this.testPlanTree = new ListedHashTree();
    }



    /**
     * Creates additional Load profiles for this test
     *
     * @param concurrentUsers Number of concurrent users that will generate requests
     * @param rampUp Ramp up period for the users
     * @param loadDuration Duration where the load will be applied
     * @param thinkTime Think time per user
     * @param startTime Initial delay before this profile begins executing
     */
    public void addLoadProfile(int concurrentUsers, int rampUp, int loadDuration, int thinkTime, int startTime) {

        LoopController baseLoopController = createLoopController();
        HTTPSampler request = createSampler();

        ThreadGroup threadGroup = createThreadGroup(baseLoopController, concurrentUsers, rampUp,
                loadDuration, startTime, thinkTime);

        createTestPlanTree(threadGroup, request);
    }


    /**
     * Creates a Standard JMeter Engine and initializes it
     *
     * @return Fully configured JMeter Engine
     */
    private StandardJMeterEngine initJMeter() {

        StandardJMeterEngine standardJMeterEngine = new StandardJMeterEngine();

        JMeterUtils.setJMeterHome(jmeterHome.getPath());
        JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
        JMeterUtils.initLocale();

        return standardJMeterEngine;
    }


    /**
     * Creates the Test Plan that will be used
     *
     * @return Test Plan that will contain the Test
     */
    private TestPlan createTestPlan() {

        TestPlan newTestPlan = new TestPlan(testName + "-TestPlan");

        newTestPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        newTestPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        newTestPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        return newTestPlan;
    }


    /**
     * Creates a Test Plan Tree that will contain the initialized elements of the Test
     *
     * @param threadGroup Thread Group of the Test
     * @param request HTTP Sampler of the Test
     */
    private void createTestPlanTree(ThreadGroup threadGroup, HTTPSampler request) {

        HashTree threadGroupTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupTree.add(request);
    }


    /**
     * Creates a Loop Controller for a Thread Group
     *
     * @return A fully configured Loop Controller
     */
    private LoopController createLoopController() {

        LoopController loopController = new LoopController();
        String guid = UUID.randomUUID().toString().substring(0, 8);

        loopController.setName(testName + "Loop Controller-" + guid);
        loopController.setLoops(-1);
        loopController.setContinueForever(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();

        return loopController;
    }


    /**
     * Creates an HTTP Sampler for a Thread Group
     *
     * @return Fully configured HTTP Sampler
     */
    private HTTPSampler createSampler() {

        HTTPSampler request = new HTTPSampler();
        String guid = UUID.randomUUID().toString().substring(0, 8);

        request.setName(testName + "-Function Request-" + guid);
        request.setEnabled(true);
        request.setMethod(String.valueOf(functionMethod));
        request.setPath(functionURL);
        request.setUseKeepAlive(true);
        request.setFollowRedirects(true);
        request.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        request.setProperty(TestElement.GUI_CLASS, HTTPSampler.class.getName());

        return request;
    }


    /**
     * Creates a Thread Group
     *
     * @param loopController Associated Loop Controller
     * @param desiredUsers Concurrent users
     * @param rampUp Ramp up period ( in seconds )
     * @param loadDuration Duration that the load will be applied ( in seconds )
     * @param startTime Initial delay before the Thread group starts ( in seconds )
     * @param thinkTime User think time ( in seconds )
     *
     * @return A fully configured Thread Group
     */
    private ThreadGroup createThreadGroup(LoopController loopController, int desiredUsers,
                                          int rampUp, int loadDuration, int startTime, int thinkTime) {

        ThreadGroup threadGroup = new ThreadGroup();
        String guid = UUID.randomUUID().toString().substring(0, 8);

        threadGroup.setName(testName + "-Thread Group-" + guid);
        threadGroup.setNumThreads(desiredUsers);
        threadGroup.setDelay(startTime);
        threadGroup.setRampUp(rampUp);
        threadGroup.setDuration(rampUp + loadDuration);
        threadGroup.setScheduler(true);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

        UniformRandomTimer timer = new UniformRandomTimer();
        timer.setDelay(String.valueOf(thinkTime * 1000));  // thinkTime in milliseconds
        threadGroup.addTestElement(timer);

        return threadGroup;
    }


    /**
     * Runs the entire test
     */
    public void runTest() {

        if (testPlanTree.isEmpty()) {
            throw new TestExecutionFailedException("Load Test failed to execute," +
                    " no load profiles are associated with it");
        }

        jmeter.configure(testPlanTree);
        jmeter.run();
    }


    public String getName() {

        return this.testName;
    }


}
