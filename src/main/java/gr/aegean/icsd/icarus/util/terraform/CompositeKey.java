package gr.aegean.icsd.icarus.util.terraform;

import gr.aegean.icsd.icarus.resourceconfiguration.ResourceConfiguration;

public record CompositeKey(String outputName, ResourceConfiguration configurationUsed) {


    @Override
    public String toString() {
        return "CompositeKey{" +
                "outputName='" + outputName + '\'' +
                ", configurationUsed=" + configurationUsed.toString() +
                '}';
    }


}
