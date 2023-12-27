package gr.aegean.icsd.icarus.util.gcp;

/**
 * Contains a list of all supported GCP regions <br>
 * All regions that support Cloud Functions are supported
 */
public enum GcpRegion {


    /*
    GCP Region codes contain dashes, which are not valid symbols to define enum
    constants, hence as a workaround each region code is associated with an enum constant.

    The actual region code is fetched by using the 'get' method implemented below.

    This approach was chosen because of the type safety offered by Java enums
     */


    // Americas Regions
    US_WEST_1("us-west1"),
    US_WEST_2("us-west2"),
    US_WEST_3("us-west3"),
    US_WEST_4("us-west4"),
    US_CENTRAL_1("us-central1"),
    US_EAST_1("us-east1"),
    US_EAST_4("us-east4"),
    US_EAST_5("us-east5"),
    US_SOUTH_1("us-south1"),
    NORTHAMERICA_NORTHEAST_1("northamerica-northeast1"),
    NORTHAMERICA_NORTHEAST_2("northamerica-northeast2"),
    SOUTHAMERICA_WEST_1("southamerica-west1"),
    SOUTHAMERICA_EAST_1("southamerica-east1"),


    // Europe Regions
    EUROPE_WEST_1("europe-west1"),
    EUROPE_WEST_2("europe-west2"),
    EUROPE_WEST_3("europe-west3"),
    EUROPE_WEST_4("europe-west4"),
    EUROPE_WEST_6("europe-west6"),
    EUROPE_NORTH_1("europe-north1"),
    EUROPE_CENTRAL_2("europe-central2"),


    // Asia Pacific
    ASIA_SOUTH_1("asia-south1"),
    ASIA_SOUTH_2("asia-south2"),
    ASIA_SOUTHEAST_1("asia-southeast1"),
    ASIA_SOUTHEAST_2("asia-southeast2"),
    ASIA_EAST_1("asia-east1"),
    ASIA_EAST_2("asia-east2"),
    ASIA_NORTHEAST_1("asia-northeast1"),
    ASIA_NORTHEAST_2("asia-northeast2"),
    ASIA_NORTHEAST_3("asia-northeast3"),
    AUSTRALIA_SOUTHEAST_1("australia-southeast1"),
    AUSTRALIA_SOUTHEAST_2("australia-southeast2");



    private final String gcpRegionCode;



    GcpRegion(String regionCode) {
        this.gcpRegionCode = regionCode;
    }



    /**
     * Used to get the actual gcpRegionCode that will be used by Terraform
     * @return GCP Region code
     */
    public String get() {
        return gcpRegionCode;
    }


}
