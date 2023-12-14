package gr.aegean.icsd.icarus.util.aws;

/**
 * Contains a list of all supported AWS regions <br>
 * The supported regions are the same as those supported BY DEFAULT for
 * a new AWS account ( Regions like af-south-1 were left out )
 */
public enum AwsRegion {

    /*
    AWS Region codes contain dashes, which are not valid symbols to define enum
    constants, hence as a workaround each region code is associated with an enum constant.

    The actual region code is fetched by using the 'get' method implemented below.

    This approach was chosen because of the type safety offered by Java enums
     */

    // US Regions
    us_east_1("us-east-1"),
    us_east_2("us-east-2"),
    us_west_1("us-west-1"),
    us_west_2("us-west-2"),

    // Canada
    ca_central_1("ca-central-1"),

    // South America
    sa_east_1("sa-east-1"),

    // Asia Pacific Regions
    ap_south_1("ap-south-1"),
    ap_northeast_3("ap-northeast-3"),
    ap_northeast_2("ap-northeast-2"),
    ap_northeast_1("ap-northeast-1"),
    ap_southeast_1("ap-southeast-1"),
    ap_southeast_2("ap-southeast-2"),

    // Europe
    eu_central_1("eu-central-1"),
    eu_west_1("eu-west-1"),
    eu_west_2("eu-west-2"),
    eu_west_3("eu-west-3"),
    eu_north_1("eu-north-1");


    private final String awsRegionCode;


    AwsRegion(String regionCode) {
        this.awsRegionCode = regionCode;
    }

    /**
     * Used to get the actual awsRegionCode that will be used by Terraform
     * @return AWS Region code
     */
    public String get() {
        return awsRegionCode;
    }

}
