package net.eisele.camel.jpa.bank;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import net.eisele.camel.jpa.CustomerStatus;
import org.apache.camel.Exchange;

/**
 * CustomerStatus Database Access Bean
 *
 * @author Markus Eisele <markus@jboss.org>
 */
@Named("customerService")
public class CustomerStatusService {

    @PersistenceContext(unitName = "customer-pu", type = PersistenceContextType.EXTENDED)
    EntityManager em;

    private final static Logger LOGGER = Logger.getLogger(CustomerStatusService.class.getName());

    /**
     * Load a customer from the database. Expects a Header with name "CustId"
     * @param oldExchange
     * @param newExchange
     * @throws Exception 
     */
    public void loadCustomer(Exchange oldExchange, Exchange newExchange) throws Exception {
        String customerId = (newExchange.getIn().getHeader("CustId", String.class));
        try {
            TypedQuery<CustomerStatus> query
                    = em.createNamedQuery("CustomerStatus.findByID", CustomerStatus.class);
            query.setParameter("customerID", customerId);
            CustomerStatus customer = query.getSingleResult();
            newExchange.getIn().setBody(customer, CustomerStatus.class);
        } catch (Exception e) {
            throw new Exception("CUSTOMER NOT FOUND");
        }
    }

    /**
     * Updates a Customer in the Database. Expects a CustomerStatus.class in the body.
     * @param oldExchange
     * @param newExchange
     * @throws Exception
     */
    @Transactional
    public void updateCustomer(Exchange oldExchange, Exchange newExchange) throws Exception {
        CustomerStatus customerStatus = newExchange.getIn().getBody(CustomerStatus.class);
        try {
            em.merge(customerStatus);
            newExchange.getIn().setBody(customerStatus, CustomerStatus.class);
        } catch (Exception e) {
            throw new Exception("CUSTOMER NOT FOUND", e);
        }
    }

    /**
     * Logs all Customers in the Database. For information and debugging.
     * @param oldExchange
     * @param newExchange
     * @throws Exception
     */
    public void logCustomerStatus(Exchange oldExchange, Exchange newExchange) throws Exception {
        try {
            TypedQuery<CustomerStatus> query
                    = em.createNamedQuery("CustomerStatus.findAll", CustomerStatus.class);
            List<CustomerStatus> customers = query.getResultList();
            customers.stream().forEach((c) -> {
                LOGGER.log(Level.INFO, "Customer: {0}", c.toString());
            });

        } catch (Exception e) {
            throw new Exception("NO CUSTOMERS FOUND");
        }
    }

}
