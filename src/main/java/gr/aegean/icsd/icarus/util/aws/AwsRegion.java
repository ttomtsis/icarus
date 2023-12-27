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
    US_EAST_1("us-east-1"),
    US_EAST_2("us-east-2"),
    US_WEST_1("us-west-1"),
    US_WEST_2("us-west-2"),


    // Canada
    CA_CENTRAL_1("ca-central-1"),


    // South America
    SA_EAST_1("sa-east-1"),


    // Asia Pacific Regions
    AP_SOUTH_1("ap-south-1"),
    AP_NORTHEAST_3("ap-northeast-3"),
    AP_NORTHEAST_2("ap-northeast-2"),
    AP_NORTHEAST_1("ap-northeast-1"),
    AP_SOUTHEAST_1("ap-southeast-1"),
    AP_SOUTHEAST_2("ap-southeast-2"),


    // Europe
    EU_CENTRAL_1("eu-central-1"),
    EU_WEST_1("eu-west-1"),
    EU_WEST_2("eu-west-2"),
    EU_WEST_3("eu-west-3"),
    EU_NORTH_1("eu-north-1");



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
