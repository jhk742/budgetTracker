# Budget Tracker Application
A budget tracking application created using Java and JSwing with the maven framework.
Makes use of a MySQL database (currently supports on-premise databases...working on providing authorization for people to utilize the AWS-RDS endpoint)
to help visualize user-specific data.

## Configuration
Prior to launching the application, please ensure that you run the provided bash script file, **intialRun.sh**, as it will guide you through the configuration process
of the on-premise database and its associated tables, components of the application that are crucial for its operation.
1. **Run the Bash Script**
   - Navigate to the root directory of the cloned repository.
   - Execute the provided bash script file, "initialRun.sh".
2. **Permission Issues**
All prospective users of the application should be able to run the bash script file upon cloning the repository onto their own local machines.
However, should you encounter permission issues, you can manually add the permission to execute the script with the following command:
```bash
chmod +x initialRun.sh
```
