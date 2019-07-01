# revolut-transfer-money
A Java based Funds Transfer Engine with an in-memory DB and remote interface exposed over Rest Services 

* Java Version Used Java 8
* In-Memory DB: H2
* Web Server : Jetty
* Frameworks Used : No Framework is used, libraries for web server and Rest interface are used.
* Common libraries Used: jersery libraries for supporting REST, jetty library, apache http client for testing REST APIs, H2 DB libraries, jackson for json binding, apache commons lang, apache commons collection
* Common concern addressed : Transaction Management at business layer, Concurrency control at persistence layer

## APIs
* Get All Accounts
* Get Account By Id
* Withdraw Money
* Deposit Money
* Transfer Funds
