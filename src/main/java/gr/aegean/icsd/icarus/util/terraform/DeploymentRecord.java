package gr.aegean.icsd.icarus.util.terraform;

import gr.aegean.icsd.icarus.provideraccount.ProviderAccount;
import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;
import gr.aegean.icsd.icarus.util.enums.Platform;

import java.util.Optional;

public class DeploymentRecord {


    public final String deployedFunctionName;

    public ResourceConfiguration configurationUsed;

    public ProviderAccount accountUsed;

    public final String deployedRegion;

    public final Integer deployedMemory;

    public Integer deployedCpu;

    public final String deploymentGuid;

    public String deployedUrl;

    public final Platform deployedPlatform;



    public DeploymentRecord(String deployedFunctionName, String deployedRegion, Integer deployedMemory,
                            String deploymentGuid, Platform platform) {

        this.deployedFunctionName = deployedFunctionName;
        this.deployedRegion = deployedRegion;
        this.deployedMemory = deployedMemory;
        this.deploymentGuid = deploymentGuid;
        this.deployedPlatform = platform;
    }



    @Override
    public String toString() {
        return "DeploymentRecord{" +
                "deployedFunctionName='" + deployedFunctionName + '\'' +
                ", configurationUsed=" + configurationUsed +
                ", accountUsed=" + accountUsed +
                ", deployedRegion='" + deployedRegion + '\'' +
                ", deployedMemory=" + deployedMemory +
                ", deploymentGuid='" + deploymentGuid + '\'' +
                ", deployedUrl='" + deployedUrl + '\'' +
                '}';
    }


}
