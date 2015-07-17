package net.eisele.camel.jpa.bank;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

/**
 * Camel Route Builder for the Bank Demo
 * @author Markus Eisele <markus@jboss.org>
 */
@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class BankRouteBuilder extends RouteBuilder {

    /**
     * Configure the Routes 
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {

        /**
         * <onException>
         * <exception>java.lang.Exception</exception>
         * <redeliveryPolicy maximumRedeliveries="1"/>
         * <handled>
         * <constant>true</constant>
         * </handled>
         * <to uri="direct:handleFail"/>
         * </onException>
         */
        onException(Exception.class).handled(true).maximumRedeliveries(1).to("direct:handleFail");

        /**
         * <route id="readFile">
         * <from uri="file://inputdir?delete=true"/>
         * <split>
         * <xpath>//Bank/Transaction</xpath>
         * <to uri="direct:processTransaction"/>
         * </split>
         * </route>
         */
        //String vmname = System.getProperty("jboss.server.data.dir");
        from("file://{{sys:jboss.server.data.dir}}/inbox?delete=true").routeId("readFile").split(xpath("//Bank/Transaction")).to("direct:processTransaction");

        /**
         * .autoStartup(false)
         * <route id="processTransaction">
         * <from uri="direct:processTransaction"/>
         * <setHeader headerName="CustId">
         * <xpath>/Transaction/CustId/text()</xpath>
         * </setHeader>
         * <setHeader headerName="VipStatus">
         * <simple>/Transaction/VipStatus/text()</simple>
         * </setHeader>
         * <setHeader headerName="amt">
         * <xpath>/Transaction/Detail/amount/text()</xpath>
         * </setHeader>
         * <setHeader headerName="transacontent">
         * <simple>${body}</simple>
         * </setHeader>
         * <choice>
         * <when>
         * <xpath>/Transaction[@type='Cash']</xpath>
         * <to uri="direct:processCash"/>
         * </when>
         * <when>
         * <xpath>/Transaction[@type='Transfer']</xpath>
         * <setHeader headerName="receiverId">
         * <xpath>/Transaction/Detail/CustId/text()</xpath>
         * </setHeader>
         * <to uri="direct:doTransfer"/>
         * </when>
         * </choice>
         * </route>
         */
        from("direct:processTransaction")
                .routeId("processTransaction")
                .setHeader("CustId", xpath("/Transaction/CustId/text()"))
                // No need to set the vipStatus
                // .setHeader("VipStatus", xpath("/Transaction/VipStatus/text()"))
                .setHeader("amt", xpath("/Transaction/Detail/amount/text()"))
                .setHeader("transacontent", simple("${body}"))
                .choice().when().xpath("/Transaction[@type='Cash']").to("direct:processCash")
                .when().xpath("/Transaction[@type='Transfer']").setHeader("receiverId", xpath("/Transaction/Detail/CustId/text()")).to("direct:doTransfer");

        /**
         * <route id="failedRoute">
         * <from uri="direct:handleFail"/>
         * <log message="ERROR => ${header.CamelExceptionCaught}"/>
         * <setBody>
         * <simple>${header.transacontent}
         * &lt;exceptionInfo&gt;${header.CamelExceptionCaught}&lt;/exceptionInfo&gt;</simple>
         * </setBody>
         * <to uri="file://errordir?fileName=error-$simple{date:now:yyyyMMddhhmmss}.xml"/>
         * </route>
         */
        from("direct:handleFail")
                .routeId("failedRoute")
                .log("ERROR => ${header.CamelExceptionCaught}")
                .setBody().simple("${header.transacontent}\n"
                        + "         * &lt;exceptionInfo&gt;${header.CamelExceptionCaught}&lt;/exceptionInfo&gt;")
                .to("file://errordir?fileName=error-$simple{date:now:yyyyMMddhhmmss}.xml");

        /**
         * <route id="processCash">
         * <from uri="direct:processCash"/>
         * <setBody>
         * <simple>SELECT balance from customerdemo where customerID =
         * '${header.CustId}';</simple>
         * </setBody>
         * <to uri="jdbc:dataSourcePS"/>
         * <bean method="doBalanceWithoutFee" ref="bankbean"/>
         * <setBody>
         * <simple>UPDATE customerdemo SET balance = ${body} where customerID =
         * '${header.CustId}';</simple>
         * </setBody>
         * <to uri="jdbc:dataSourcePS"/>
         * </route>
         * from("direct:processCash").routeId("processCash").from("jpa://net.eisele.camel.jpa?persistenceUnit=customer-pu&consumer.namedQuery=Country.findByID&consumer.parameters=#"
         * + simple("${header.CustId})")) .beanRef("bankBean",
         * "doBalanceWithoutFee").log(">> Response : ${body}");
         */
        from("direct:processCash").routeId("processCash").startupOrder(1)
                .to("bean:customerService?method=loadCustomer")
                .to("bean:bankBean?method=doBalanceWithoutFee")
                .to("bean:customerService?method=updateCustomer")
                .to("log:net.eisele.camel.jpa.bank?showAll=true");

        //from("direct:start").to("mock:foo").log("ERROR => ${jboss.server.data.dir}" + vmname);
        /**
         * <route id="doTransfer">
         * <from uri="direct:doTransfer"/>
         * <setBody>
         * <simple>SELECT balance from customerdemo where customerID =
         * '${header.CustId}';</simple>
         * </setBody>
         * <to uri="jdbc:dataSourcePS"/>
         * <bean method="transfer" ref="bankbean"/>
         * <bean method="doBalance" ref="bankbean"/>
         * <setHeader headerName="transfersql">
         * <simple>UPDATE customerdemo SET balance = ${body} where customerID =
         * '${header.CustId}';</simple>
         * </setHeader>
         * <setBody>
         * <simple>SELECT balance from customerdemo where customerID =
         * '${header.receiverId}';</simple>
         * </setBody>
         * <to uri="jdbc:dataSourcePS"/>
         * <bean method="transfer" ref="bankbean"/>
         * <bean method="doBalanceWithoutFee" ref="bankbean"/>
         * <setBody>
         * <simple>${header.transfersql} UPDATE customerdemo SET balance =
         * ${body} where customerID = '${header.receiverId}';</simple>
         * </setBody>
         * <to uri="jdbc:dataSourcePS"/>
         * </route>
         */
        from("direct:doTransfer").routeId("doTransfer").startupOrder(2)
                .to("bean:customerService?method=logCustomerStatus")
                .to("bean:customerService?method=loadCustomer")
                .to("bean:bankBean?method=transfer")
                .to("bean:bankBean?method=doBalance")
                .to("bean:customerService?method=updateCustomer")
                .to("log:net.eisele.camel.jpa.bank?showAll=true")
                .setHeader("CustId", simple("${header.receiverId}"))
                .to("bean:customerService?method=loadCustomer")
                .to("bean:bankBean?method=transfer")
                .to("bean:bankBean?method=doBalanceWithoutFee")
                .to("bean:customerService?method=updateCustomer")
                .to("bean:customerService?method=logCustomerStatus");
    }
}
