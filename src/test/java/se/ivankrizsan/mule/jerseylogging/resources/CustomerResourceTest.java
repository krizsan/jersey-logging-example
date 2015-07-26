package se.ivankrizsan.mule.jerseylogging.resources;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;

/**
 * Tests the REST {@code CustomerResource}.
 *
 * @author Ivan Krizsan
 */
public class CustomerResourceTest extends FunctionalTestCase
{
    /* Constant(s): */
    protected static final int RESOURCE_ENDPOINT_PORT = 8083;
    protected static final String RESOURCES_BASE_PATH = "/resources/v100";
    protected final static String NEW_CUSTOMER_JSON = "{\"firstName\":\"Banana\",\"lastName\":\"Chippy\"," +
                                                      "\"streetName\":\"Jolly Street 42\",\"city\":\"Monkey City\"}";

    @Override
    protected String getConfigFile()
    {
        return "src/main/app/mule-config.xml";
    }

    /**
     * Sets up before each test method.
     */
    @Before
    public void setUp() {
        RestAssured.reset();
        RestAssured.port = RESOURCE_ENDPOINT_PORT;
        RestAssured.basePath = RESOURCES_BASE_PATH;
    }

    /**
     * Tests creation of a new customer.
     * Expected result: HTTP status 200 and a response containing JSON data should be returned.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testCreateNewCustomer() throws Exception
    {
        RestAssured.
            given().
            contentType("application/json").
            accept("application/json").
            body(NEW_CUSTOMER_JSON).
            when().
            post(CustomersResource.CUSTOMERS_RESOURCE_PATH).
            then().
            statusCode(200).
            contentType(ContentType.JSON);
    }
}
