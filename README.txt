LIBRARY MANAGEMENT SYSTEM (LEVEL 2 - INTERMEDIATE)
==================================================

PROJECT OBJECTIVE
Build a database-driven Java application using Java, JDBC and MySQL.

FEATURES
1. Add books
2. Issue books
3. Return books
4. View available books
5. View all books
6. View currently issued books

TECHNOLOGIES
- Java 17
- JDBC
- MySQL
- Maven

REQUIREMENTS
- JDK 17 or later
- MySQL Server and MySQL Workbench
- Apache Maven
- Internet connection for Maven to download the MySQL JDBC dependency the first time

STEP 1: CREATE THE DATABASE
1. Open MySQL Workbench and connect to your local MySQL server.
2. Open database.sql.
3. Execute the complete script.
4. Confirm that the library_management schema contains books and book_issues tables.

STEP 2: CONFIGURE YOUR MYSQL PASSWORD
1. Open config.properties in Notepad or another text editor.
2. Keep db.username=root if that is your MySQL username.
3. Replace YOUR_MYSQL_PASSWORD with your actual MySQL password.
4. Save the file.
IMPORTANT: Do not upload config.properties to a public GitHub repository.
For GitHub, upload config.example.properties instead.

STEP 3: RUN THE PROJECT
Option A:
1. Open the LibraryManagementSystem folder.
2. Click the address bar in File Explorer, type cmd and press Enter.
3. Run:
   mvn clean compile exec:java

Option B:
Double-click run.bat.

STEP 4: USE THE APPLICATION
Choose an option from the console menu:
1 - Add Book
2 - View Available Books
3 - Issue Book
4 - Return Book
5 - View All Books
6 - View Issued Books
0 - Exit

HOW ISSUE AND RETURN WORK
- When a book is issued, one available copy is reduced.
- An issue record is created with status ISSUED.
- When the book is returned, its return date and status are updated.
- The available copy count is increased.
- A book cannot be issued when no copies are available.

TROUBLESHOOTING
- Access denied: check db.username and db.password in config.properties.
- Unknown database: execute database.sql in MySQL Workbench.
- Communications link failure: make sure MySQL Server is running.
- mvn is not recognized: check that Maven is installed and its bin folder is in PATH.

NOTE
This is a Java console application, not a hosted website. The source code can be uploaded
to GitHub. A live website is not required by the Level 2 task description.
