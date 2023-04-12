![banner](https://user-images.githubusercontent.com/72699445/231580094-ec95faa7-86e7-4657-8521-8e141540fede.png)

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

The system is designed to be as flexible as possible. The only restriction is to use SQL database engine rather than the Objective ones such as MongoDB. The key features of the architecture are:
- possibility of quick change of the database engine
- ease in changing the connected databases
- flexibile enough to implement each method for other frameworks
- ease in changing the load-balancing algoritm during the system work
- self-synchronizing mechanism with query chaching
- self-fixation mechanism

The system architecture is shown at the below UML diagram:

![load_balancer_architecture](https://user-images.githubusercontent.com/72699445/231595117-bca746a4-8a0b-4b38-bbb6-68480f7a42ff.png)

## Installation

## How to Use?

## Contribute
