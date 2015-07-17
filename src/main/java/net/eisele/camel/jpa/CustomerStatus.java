package net.eisele.camel.jpa;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Customer Status Entity
 *
 * @author Markus Eisele <markus@jboss.org>
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "CustomerStatus.findAll",
            query = "SELECT c FROM CustomerStatus c"),
    @NamedQuery(name = "CustomerStatus.findByID",
            query = "SELECT c FROM CustomerStatus c WHERE c.customerID = :customerID"),})
public class CustomerStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String customerID;

    private String vipStatus;

    private Integer balance;

    public CustomerStatus() {
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getVipStatus() {
        return vipStatus;
    }

    public void setVipStatus(String vipStatus) {
        this.vipStatus = vipStatus;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "CustomerStatus{" + "customerID=" + customerID + ", vipStatus=" + vipStatus + ", balance=" + balance + '}';
    }

}
