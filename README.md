![banner (1)](https://user-images.githubusercontent.com/72699445/231595290-94018ac1-8312-4df4-a3df-379093ffc0f4.png)

This project is made for load balancing the database traffic. The system is responsible for the actions such as inserting, updating, and deleting the data from each database. The select query is only executed on the one database pointed by the load-balancing algorithm. If the database connection is broken the queries are cached and sent to the database once the connection is back. The system assumes that during the start each database is either an empty database or the state of each database is the same. It cannot synchronize databases when their state is different at the system initialization.

## Motivation

The project was written as a base for other projects that require the connection to multiple databases. The whole architecture was designed to match different database frameworks available in Java.

## Build Status

The current architecture provides a read-to-use implementation for the Hibernate framework. There are created empty classes for pure JDBC. Moreover, support for the JPA framework may be added.

## Screenshots

The following screenshot presents the system during the work with the testing application

![Screenshot 2023-04-12 234327](https://user-images.githubusercontent.com/72699445/231592722-e191644b-646b-46a4-b9d1-779ab62a6b4a.png)

Below 2 of 4 databases are off (system detects that)

![Screenshot 2023-04-12 234558](https://user-images.githubusercontent.com/72699445/231593126-a2c087d4-077c-4e1f-b3c9-7e0eb4a681b9.png)

Once the connection is fixed the missing queries are sent in the proper order

![Screenshot 2023-04-12 234621](https://user-images.githubusercontent.com/72699445/231593294-d0a58f69-c22c-48a2-b9d3-88ce1a86ce7e.png)

## Tech/Framework used

The system uses:
- Java 17
- Java Hibernate 6.1.6
- PSQL 14 (used in the example app however, it works with other database engines)

## Features

The system is designed to be as flexible as possible. The only restriction is to use SQL database engines rather than Objective ones such as MongoDB. The key features of the architecture are:
- the possibility of quick change of the database engine
- ease in changing the connected databases
- flexible enough to implement each method for other frameworks
- ease in changing the load-balancing algorithm during the system work
- self-synchronizing mechanism with query caching
- self-fixation mechanism

The system architecture is shown in the below UML diagram

![load_balancer_architecture](https://user-images.githubusercontent.com/72699445/231595117-bca746a4-8a0b-4b38-bbb6-68480f7a42ff.png)

## Installation

1. First of all clone the repository:
```
mkdir load-balancer
cd load-balancer
git clone https://github.com/kbarszczak/DB_load_balancer .
```
2. The next step is to configure the docker-compose file in case you want to create local databases (if you already have those you may skip this step)
- open the file: Application/docker-compose.yml
- set up the docker images for the database engine (specify the docker image, localhost port, and the number of created servers)
Once the docker-compose is set up we create and run the containers:
```
cd Application
docker compose up
cd ..
```
Since now the specified database server are created and run as docker containers
3. The next step is to set up the configuration files for the used framework. In the case of the default framework (Hibernate) go to Application/src/main/resources/hibernate directory and create configuration files for each database independently. An example of such a file is the following:
```
<hibernate-configuration>
    <session-factory>
        <!-- Connection settings -->
        <property name="hibernate.connection.driver_class">org.postgresql.Driver</property>
        <property name="hibernate.connection.url">jdbc:postgresql://localhost:8080/application</property>
        <property name="hibernate.connection.username">postgres</property>
        <property name="hibernate.connection.password">test</property>

        <!-- SQL dialect -->
        <property name="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</property>

        <!-- Update database on startup -->
        <property name="hibernate.hbm2ddl.auto">update</property>

        <!-- Annotated entity classes -->
        <mapping class="model.User"/>
    </session-factory>
</hibernate-configuration>
```
4. The final step is to specify created configuration files in the LoadBalancer constructor eg.:
```
LoadBalancer<Session> loadBalancer = new HibernateLoadBalancer(List.of(
        "/hibernate/hibernate-psql-1.cfg.xml",
        "/hibernate/hibernate-psql-2.cfg.xml",
        "/hibernate/hibernate-psql-3.cfg.xml",
        "/hibernate/hibernate-psql-4.cfg.xml"
))
```
and run the command:
```
mvn clean install
```
to start the demo app:
```
java -jar Application/target/LoadBalancerApplication.jar
```

## How to Use?

Due to the implementation, the proper way of using the system is to call the connection() method every time there is a new task for the database. Do not keep the Session object returned by the connection method anywhere. It may cause problems once the connection is broken moreover, it results in executing the select queries to only one database. The correct way of using the system is shown in the following example:
```
while(true){
    Session session = loadBalancer.connection();
    // do the work with session
}
```
The incorrect way:
```
Session session = loadBalancer.connection();
while(true){
    // do the work with session
}
```
This causes to use of the same connection as the main connection in each iteration

## Contribute
- clone the repository
- either make the changes or implement missing code
- create the pull request with a detailed description of your changes
