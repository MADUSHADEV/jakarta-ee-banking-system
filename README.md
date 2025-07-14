# Jakarta EE Banking System (MySQL Version)

This project is a 4-module Enterprise Banking System using Jakarta EE 9, EJB, JPA, Servlets/JSP, and MySQL. It's designed for deployment on GlassFish 6.

## 👉 Modules

1.  **core**: Contains shared JPA entities (`Customer`, `Account`).
2.  **ejb**: Contains all business logic (Stateless Session Beans), timer services, and persistence configuration.
3.  **web**: Contains the Servlet/JSP front-end.
4.  **ear**: Packages all modules for deployment.

## 👉 Prerequisites

-   JDK 11
-   Apache Maven 3.6+
-   MySQL Server 8.x
-   GlassFish 6.x
-   MySQL Connector/J JAR file (e.g., `mysql-connector-java-8.0.28.jar`)

## 🛠️ Step 1: Database Setup

1.  **Start your MySQL server.**
2.  **Run the SQL script:** Use a MySQL client (like MySQL Workbench or the command-line client) to execute the `create_tables.sql` script included in the project root.
    ```sh
    mysql -u root -p < create_tables.sql
    ```
    This script will:
    -   Create a database named `bankdb`.
    -   Create `customer` and `account` tables.
    -   Create a user `bankuser` with password `bankpassword` and grant it permissions.

## 🛠️ Step 2: GlassFish Configuration

1.  **Copy MySQL Driver to GlassFish:**
    -   Locate your MySQL Connector/J `.jar` file.
    -   Copy this JAR file into the `glassfish/lib` directory of your GlassFish installation (e.g., `/path/to/glassfish6/glassfish/lib/`).
    -   **Restart GlassFish** if it is already running. This is crucial for GlassFish to recognize the new driver.

2.  **Create JDBC Connection Pool and Resource:**
    -   Start your GlassFish domain: `asadmin start-domain`
    -   Run the following commands in a terminal.

    **Command 1: Create Connection Pool**
    ```sh
    asadmin create-jdbc-connection-pool --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource --restype javax.sql.DataSource --property user=bankuser:password=bankpassword:serverName=localhost:portNumber=3306:databaseName=bankdb bankDBPool
    ```

    **Command 2: Create JDBC Resource**
    ```sh
    asadmin create-jdbc-resource --connectionpoolid bankDBPool jdbc/bankDB
    ```
    The JNDI name `jdbc/bankDB` must exactly match the `<jta-data-source>` in `ejb/src/main/resources/META-INF/persistence.xml`.

3.  **Verify with a Ping:**
    ```sh
    asadmin ping-connection-pool bankDBPool
    ```
    You should see "Command ping-connection-pool executed successfully."

## 🛠️ Step 3: Build and Deploy

1.  **Build the Project:**
    -   Open a terminal in the project's root directory.
    -   Run the Maven command to build all modules and package the EAR:
        ```sh
        mvn clean install
        ```
    -   The final deployable artifact will be located at `ear/target/ear-1.0-SNAPSHOT.ear`.

2.  **Deploy to GlassFish:**
    -   You can deploy using the GlassFish Admin Console (`http://localhost:4848`) or the command line:
        ```sh
        asadmin deploy ear/target/ear-1.0-SNAPSHOT.ear
        ```

## 🚀 Access the Application

Open your web browser and navigate to:

**[http://localhost:9001/banking/customers](http://localhost:9001/banking/customers)**

You should see the list of customers from the database and be able to interact with the application.