# SQL Examples
Demonstration of some simple intercactions with an SQL database using the JDBC library. For the database component I used a local SQLite database, which is included in the project 'bin' folder.

The project consists of positive and negative tests run with TestNG. They mostly perform simple queries (SELECT, INSERT, UPDATE, DELETE) and verify the end result.

Initial data for the database was obtained from this tutorial: https://www.w3resource.com/sql/sql-table.php

One of the tests also reads data from an included sample Excel file and compares the data against the database.
