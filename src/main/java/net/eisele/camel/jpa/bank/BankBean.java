package net.eisele.camel.jpa.bank;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import net.eisele.camel.jpa.CustomerStatus;
import org.apache.camel.Exchange;

/**
 * The Banking Logic
 *
 * @author Markus Eisele <markus@jboss.org>
 */
@Named
public class BankBean {

    private final static Logger LOGGER = Logger.getLogger(BankBean.class.getName());
    public static final int DIAMOND_FEE = 1;
    public static final int GOLD_FEE = 2;
    public static final int NORMAL_FEE = 3;

    public void transfer(Exchange oldExchange, Exchange newExchange) throws Exception {
        Integer amtDeduct = (newExchange.getIn().getHeader("amt", Integer.class));
        newExchange.getIn().setHeader("amt", amtDeduct * -1);
    }

    public void doBalance(Exchange oldExchange, Exchange newExchange) throws Exception {

        LOGGER.log(Level.INFO, ">> doBalance {0}", newExchange.getIn().getBody(String.class));

        CustomerStatus customerStatus = newExchange.getIn().getBody(CustomerStatus.class);

        Integer amount = newExchange.getIn().getHeader("amt", Integer.class);

        Integer newBalance = customerStatus.getBalance() + amount;

        if ("Diamond".equalsIgnoreCase(customerStatus.getVipStatus())) {
            newBalance -= DIAMOND_FEE;
        } else if ("Gold".equalsIgnoreCase(customerStatus.getVipStatus())) {
            newBalance -= GOLD_FEE;
        } else {
            newBalance -= NORMAL_FEE;
        }

        if (newBalance < 0) {
            throw new Exception("NOT ENOUGH BALANCE");
        }

        customerStatus.setBalance(newBalance);

        newExchange.getIn().setBody(customerStatus, CustomerStatus.class);

        LOGGER.log(Level.INFO, "<< doBalance {0}", newBalance);

    }

    public void doBalanceWithoutFee(Exchange oldExchange, Exchange newExchange) throws Exception {

        LOGGER.log(Level.INFO, ">> doBalanceWithoutFee {0}", newExchange.getIn().getBody(String.class));

        CustomerStatus customerStatus = newExchange.getIn().getBody(CustomerStatus.class);

        Integer amount = newExchange.getIn().getHeader("amt", Integer.class);

        Integer newBalance = customerStatus.getBalance() + amount;

        if (newBalance < 0) {
            throw new Exception("NOT ENOUGH BALANCE");
        }

        customerStatus.setBalance(newBalance);

        newExchange.getIn().setBody(customerStatus, CustomerStatus.class);

        LOGGER.log(Level.INFO, "<< doBalanceWithoutFee {0}", newBalance);

    }

}
