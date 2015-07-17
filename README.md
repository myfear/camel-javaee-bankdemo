File & JPA & Java EE Bank DEMO
======================================================
This demo demonstrates the use of File connector in Camel, also added the use of Spilt pattern and Exception handling method. 

It originated in Christina's "File & JDBC Bank DEMO"
https://github.com/jbossdemocentral/jboss-fuse-component-file-jdbc/

The scenario of the demo is to mimic the transaction process between bank accounts, where it takes in XML file from different branch in a directory, each contains cash deposit, cash withdraw and transfer information of bank accounts, depending on the type of transaction, spilt up each transaction retrieve balance from a database, does the transaction and calculate the transaction fee and then place the balance back to the database storage. 

It uses the WildFly Camel Subsystem and you need to make sure to have this setup as described here: http://blog.eisele.net/2015/07/using-camel-routes-in-java-ee-components.html