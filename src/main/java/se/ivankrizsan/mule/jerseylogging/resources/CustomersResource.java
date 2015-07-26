package se.ivankrizsan.mule.jerseylogging.resources;

import se.ivankrizsan.mule.jerseylogging.entities.Customer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * REST customers resource implemented using Jersey.
 *
 * @author Ivan Krizsan
 */
@Path(CustomersResource.CUSTOMERS_RESOURCE_PATH)
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class CustomersResource {
    /* Constant(s): */
    public static final String CUSTOMERS_RESOURCE_PATH = "/customers";

    /* Instance variable(s): */
    /** Fake customer repository. */
    protected Map<Long, Customer> mCustomersRepository = new Hashtable<>();
    /** Customer id generator. */
    protected Random mCustomerIdGenerator = new Random();

    /**
     * Default constructor.
     * Creates one customer in the repository, as to have some data.
     */
    public CustomersResource() {
        final Customer theCustomer = new Customer();
        theCustomer.setId(1L);
        theCustomer.setFirstName("Dave");
        theCustomer.setLastName("Donkey");
        theCustomer.setCity("Paradise City");
        theCustomer.setStreetName("11 Downing Street");
        mCustomersRepository.put(1L, theCustomer);
    }

    /**
     * Retrieves all customers.
     *
     * @return Response containing all customers.
     */
    @GET
    public Response getAllCustomers() {
        final List<Customer> theCustomers = new ArrayList<>();
        theCustomers.addAll(mCustomersRepository.values());
        /*
         * Need to return an array of entities in the response in order for
         * JAXB to be able to create an XML representation of the list.
         */
        final Customer[] theCustomersArray = theCustomers.toArray(
            new Customer[theCustomers.size()]);
        final Response theResponse = Response.ok().entity(theCustomersArray).build();
        return theResponse;
    }

    /**
     * Retrieves the customer with supplied id.
     *
     * @param id Id of customer to retrieve.
     * @return Response containing requested customer, or 404 if no such customer.
     */
    @GET
    @Path("{id}")
    public Response getCustomerById(@PathParam("id") final Long id) {
        final Response theResponse;
        final Customer theCustomer = mCustomersRepository.get(id);
        if (theCustomer == null) {
            theResponse = Response.status(Response.Status.NOT_FOUND).build();
        } else {
            theResponse = Response.ok().entity(theCustomer).build();
        }

        return theResponse;
    }

    /**
     * Deletes the customer with supplied id.
     *
     * @param id Id of customer to delete.
     * @return Response OK if customer deleted, or 404 if no such customer.
     */
    @DELETE
    @Path("{id}")
    public Response deleteCustomerById(@PathParam("id") final Long id) {
        final Response theResponse;
        final Customer theCustomer = mCustomersRepository.get(id);
        if (mCustomersRepository.containsKey(id)) {
            mCustomersRepository.remove(id);
            theResponse = Response.ok().entity(theCustomer).build();
        } else {
            theResponse = Response.status(Response.Status.NOT_FOUND).build();
        }

        return theResponse;
    }

    /**
     * Creates a new customer using the supplied customer data.
     * New customers must not have an id assigned.
     *
     * @param inCustomer Customer to create.
     * @return Response containing new customer, or bad request
     * if supplied customer had an id.
     */
    @POST
    public Response createCustomer(final Customer inCustomer) {
        final Response theResponse;

        /* New customers must not have an id. */
        if (inCustomer.getId() != null) {
            theResponse = Response.status(Response.Status.BAD_REQUEST).entity(
                    "New customers must not have an id").build();
        } else {
            Long theNewCustomerId;
            do {
                theNewCustomerId = Math.abs(mCustomerIdGenerator.nextLong());
            } while (mCustomersRepository.containsKey(theNewCustomerId));
            inCustomer.setId(theNewCustomerId);

            mCustomersRepository.put(theNewCustomerId, inCustomer);

            theResponse = Response.ok().entity(inCustomer).build();
        }

        return theResponse;
    }

    /**
     * Updates supplied customer setting its id to supplied id, overwriting any
     * existing customers with same id.
     *
     * @param inCustomer Customer data to write.
     * @param id Id of customer to update.
     * @return Response containing updated customer, or bad request if no id supplied.
     */
    @PUT
    @Path("{id}")
    public Response updateCustomer(final Customer inCustomer, @PathParam("id") Long id) {
        final Response theResponse;
        inCustomer.setId(id);

        mCustomersRepository.put(inCustomer.getId(), inCustomer);
        theResponse = Response.ok().entity(inCustomer).build();

        return theResponse;
    }
}
