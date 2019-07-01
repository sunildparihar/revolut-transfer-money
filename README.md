# revolut-transfer-money
A Java based Funds Transfer Engine with an in-memory DB and remote interface exposed over Rest Services 

* <B>Java Version Used:</B> Java 8
* <B>In-Memory DB:</B> H2
* <B>Web Server:</B> Jetty
* <B>Frameworks Used:</B> No Framework is used, libraries for web server and Rest interface are used.
* <B>Common libraries Used:</B> jersery libraries for supporting REST, jetty library, apache http client for testing REST APIs, H2 DB libraries, jackson for json binding, apache commons lang, apache commons collection.
* <B>Common concerns addressed:</B> Transaction Management at business layer, Concurrency control at persistence layer.

## APIs
* Get All Accounts
* Get Account By Id
* Withdraw Money
* Deposit Money
* Transfer Funds
#### The In-Memory H2 DB is prepopulated with 5 accounts. For simplicity APIs for create and delete account feature have not been provided.

## Architechture of Application
This is a multi layered application with clear separation between individual layers. Following are the layers

1. Database : In-Memory H2 DB
2. Data Access (Persistence) layer : Written in plain JDBC. Refer module ft-persistence-services
3. Core Business Layer: Written in Core Java.
4. Service Gateway : A Gateway to invoke core business services from any application interface e.g. Rest Interface, SOAP Interface or from any presentation layer application. The purpose of this service gateway to perform common checks and tasks before and after invoking core business services, similar to AOP. In this project i am only putting transaction management in this gateway but this gateway is open to be enhanced for logging, security checks etc..
5. Application Layer (Rest Services): Written with JAX-RS specs.

Each and every layer can independently be modified and enhanced without affecting any other layer. 

The application flow goes as below:

Rest Client ==> Rest Service ==> Service Gateway ==> Cose Business Service ==> Data Access Layer ==> DB

### Brief Description about each layer

Data Access layer
```
Written in plain JDBC. Refer module ft-persistence-services. This layer can be changed to use any ORM framework or to change 

DB vendor without affecting any other layer.
```

Core Business Services
```
This contains 
1. sdk bundle with availble service interfaces. Services can be annotated with standard javax @Transactional annotation to include transactions with the specified propagation level. Refer module ft-sdk-bundle.
2. Core business implementation for the service interfaces. Refer ft-core-services.
3. A Service Locator module that locates service interface implementation class to be invoked by service gateway. The purpose of this locator interface is to hide any core implementation level classes to outside layer i.e. service gateway, rest services. This locator has been written in way that adding a new service interface doesn't require any changes here untill and unless we choose to write implementation class not ending with <ServiceInterfaceName>Impl.java and not in the base core impl package i.e. "com.revolut.core.fundstransfer.impl". In case we want to write a Implementation class with different naming convention or outside the base package, we can write a Service Locator implementing ServiceLocator.java interface. However this service locator can be modified to locate service implementation in more elegant way e.g. using annotation based scanning same as Spring's component scan.
4. Transaction Management Module : A module to handle transaction management based on the standard transaction propagation levels. It supports starting and ending transactions with also support for nested transaction. This is thread safe i.e. multiple threads can start or end transactions in parallel without affecting each other. refer module ft-transaction-management
5. Connection Manager Module: A module to manage database connections i.e. pooling of connections. As of now connection pooling is not implementated in this project due to time constraint but a clear support is there by adding this module. Adding a connection pool library will only require changes in this module without affecting any other layer.
```

