# Budget Tracker Application
A budget tracking application created using Java and JSwing with the maven framework.
Makes use of MySQL (currently supports on-premise databases...working on providing authorization for people to utilize the AWS-RDS endpoint)
to help visualize user-specific data.

## Configuration
Prior to launching the application, please ensure that you run the provided bash script file, **intialRun.sh**, as it will guide you through the configuration process
of the on-premise database and its associated tables, components of the application that are crucial for its operation.
1. **Run the Bash Script**
   - Navigate to the root directory of the cloned repository.
   - Execute the provided bash script file, **initialRun.sh**.
2. **Permission Issues**
All prospective users of the application should be able to run the bash script file upon cloning the repository onto their own local machines.
However, should you encounter permission issues, you can manually add the permission to execute the script with the following command:
```bash
chmod +x initialRun.sh
```
3. **Maven Clean Install**
The script will automatically perform a Maven clean install (mvn clean install) to build and configure the necessary components.

4. **Launching the Application (First Run)**
The script will automatically execute the application JAR file after successful configuration.

### Subsequent Application Runs  
   For future usage:
 1. **Navigate to the Target Directory**
   ```bash
   cd path/to/target
   ```
2. **Execute the JAR File**
  ```bash
  java -jar your-application.jar
  ```

## Features
As of the current release, the application encompasses the following key features:
1. **User Management**: Empowers administrators to effectively manage and modify account information for application users. This functionality is exclusively accessible to administrator accounts, which are created during the initial configuration process executed by running the provided bash script.
2. **Category Management**: Enables users to create personalized categories, enhancing the flexibility and precision of transaction categorization within the application.
3. **Transaction Management**: Empowers users to securely store and organize transactions, seamlessly handling both income and expense entries.
4. **Account Balance**: Enables users to review their complete transaction history, offering additional filtering options for a more detailed account overview.
5. **Visualizations**: Incorporates charts to provide users with a graphical representation of their spending and savings, facilitating a clearer understanding of their financial trends.
6. **Currency Conversion**: Facilitates users in viewing the equivalent amount of one currency in another, aiding in understanding the financial impact and facilitating cross-currency comparisons.

## Technologies Used
The application makes use of the following technologies, libraries, and frameworks:
1. **Build and Dependency Management**: Maven
2. **Database**: MySQL, AWS-RDS
3. **User Interface**: JSwing
4. **API Calls**: Apache HTTPClient (https://www.exchangerate-api.com/)https://www.exchangerate-api.com/)
5. **JSON Handling**: org.json
6. **Web Scraping**: jsoup
7. **Visualiztion**: JFreeChart
8. **Testing**: JUnit, Mockito, PowerMock
   
