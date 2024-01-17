while true; do
  read -p "Would you like to use the AWS-RDS database? (Y/N) " AWS_USE_ANSWER
  if [ "${AWS_USE_ANSWER,,}" == "y" ]; then
      echo "You have opted to use the AWS-RDS database."
      echo "Endpoint: budgettracker.cfuygiu08mdn.ap-northeast-2.rds.amazonaws.com"
      break
  elif [ "${AWS_USE_ANSWER,,}" == "n" ]; then
      echo "You have chosen not to use the AWS-RDS database. Moving on to the next step."
      break
  else
      echo "Invalid response. Please enter 'Y' or 'N'."
  fi
done

echo

while true; do
  read -p "Would you like to use an on-premise database? (Y/N) " LOCAL_USE_ANSWER
  if [ "${LOCAL_USE_ANSWER,,}" == "y" ]; then
    echo "You have chosen to utilize an on-premise database."
    echo "This entails the configuration of a MySQL schema along with its associated tables."
    read -p "May we seek your authorization to proceed with this configuration? (Y/N) " CONFIGURE_LOCAL_ANSWER

    # make sure to have the mysql executable added to your system's path variable
    if [ "${CONFIGURE_LOCAL_ANSWER,,}" == "y" ]; then
      read -p "MySQL Host: " MYSQL_HOST
      read -p "MySQL Port: " MYSQL_PORT
      read -p "MySQL User: " MYSQL_USER
      read -s -p "MySQL Password: " MYSQL_PASSWORD
      MYSQL_COMMAND="mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD"
      # Create the database
      echo "Creating database and associated tables..."
      $MYSQL_COMMAND -e "CREATE DATABASE IF NOT EXISTS budget_tracker_test;"

      # Use the created database and create necessary tables.
      $MYSQL_COMMAND -e "USE budget_tracker_test;
                        CREATE TABLE user (id INT PRIMARY KEY AUTO_INCREMENT,
                         name varchar(100),
                         email varchar(100),
                         phone varchar(100),
                         address varchar(100),
                         password varchar(100),
                         status tinyint(1)
                        );

                        CREATE TABLE bank_accounts (account_id INT PRIMARY KEY AUTO_INCREMENT,
                         user_id INT,
                         account_balance DECIMAL(10, 2),
                         FOREIGN KEY (user_id) REFERENCES user(id)
                        );

                        CREATE TABLE categories (category_id INT PRIMARY KEY AUTO_INCREMENT,
                         name varchar(50),
                         description varchar(255)
                        );

                        CREATE TABLE transactions (transaction_id INT PRIMARY KEY AUTO_INCREMENT,
                         date date,
                         description varchar(255),
                         amount decimal(10, 2),
                         category_id int,
                         type varchar(10),
                         account_id int,
                         running_balance decimal(10, 2),
                         payment_method varchar(50),
                         location varchar(255),
                         FOREIGN KEY (category_id) REFERENCES categories(category_id),
                         FOREIGN KEY (account_id) REFERENCES user(id)
                        );
                        "
      echo "Database configuration complete!"
      echo
      echo "To give yourself admin privileges, you must first create an account."
      read -p "Please provide your email address with which you will login to the app: " USER_EMAIL
      read -s -p "Now, please choose a password: " USER_PASSWORD
      echo
      read -p "Your name: " USER_NAME
      read -p "Your phone #: " USER_PHONE
      read -p "Your address: " USER_ADDRESS
      read -p "Please provide your initial balance: " USER_INITIAL_BALANCE
      echo
      #Insert user data into the 'user' table
      $MYSQL_COMMAND -e "USE budget_tracker_test;
                         INSERT INTO user (name, email, phone, address, password, status)
                         values ('$USER_NAME', '$USER_EMAIL', '$USER_PHONE', '$USER_ADDRESS', '$USER_PASSWORD', 1);

                         INSERT INTO bank_accounts (user_id, account_balance)
                         values (LAST_INSERT_ID(), $USER_INITIAL_BALANCE);"
      echo "User setup complete. You may now login to the app using your email."
      break
    elif [ "${CONFIGURE_LOCAL_ANSWER,,}" == "n" ]; then
        echo "You have chose not to use the on-premise database."
        break
    fi
  elif [ "${LOCAL_USE_ANSWER,,}" == "n" ]; then
      echo "You have chosen not to use the on-premise database."
      break
  else
      echo "Invalid response. Please enter 'Y' or 'N'."
  fi
done