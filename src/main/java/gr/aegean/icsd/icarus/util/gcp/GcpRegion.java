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
    us_west_1("us-west1"),
    us_west_2("us-west2"),
    us_west_3("us-west3"),
    us_west_4("us-west4"),
    us_central_1("us-central1"),
    us_east_1("us-east1"),
    us_east_4("us-east4"),
    us_east_5("us-east5"),
    us_south_1("us-south1"),
    northamerica_northeast_1("northamerica-northeast1"),
    northamerica_northeast_2("northamerica-northeast2"),
    southamerica_west_1("southamerica-west1"),
    southamerica_east_1("southamerica-east1"),

    // Europe Regions
    europe_west_1("europe-west1"),
    europe_west_2("europe-west2"),
    europe_west_3("europe-west3"),
    europe_west_4("europe-west4"),
    europe_west_6("europe-west6"),
    europe_north_1("europe-north1"),
    europe_central_2("europe-central2"),

    // Asia Pacific
    asia_south_1("asia-south1"),
    asia_south_2("asia-south2"),
    asia_southeast_1("asia-southeast1"),
    asia_southeast_2("asia-southeast2"),
    asia_east_1("asia-east1"),
    asia_east_2("asia-east2"),
    asia_northeast_1("asia-northeast1"),
    asia_northeast_2("asia-northeast2"),
    asia_northeast_3("asia-northeast3"),
    australia_southeast_1("australia-southeast1"),
    australia_southeast_2("australia-southeast2");


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
