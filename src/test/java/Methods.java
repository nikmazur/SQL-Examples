import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.sql.*;

public class Methods {

    //Objects for working SQL
    protected static Connection conn = null;
    protected static Statement stmt = null;
    protected ResultSet rs;

    //This method runs before any test classes. Opens the SQL connection before any tests are ran.
    @BeforeSuite
    public void setup() throws SQLException {
        //Establish connection to the local SQLite database (file is located in 'bin' project folder)
        conn = DriverManager.getConnection("jdbc:sqlite:bin\\database.s3db");
        //Prepare statement for executing SQL queries
        stmt = conn.createStatement();
    }

    //Runs after all tests are finished. Closes the connection to the database before exiting the program.
    @AfterSuite
    public void finish() throws SQLException {
        conn.close();
    }

    //DataProvider for 'testSelect'.
    //Contains data which will be passed to the test for multiple runs (3 in this case).
    @DataProvider
    public Object[][] getAgents()
    {
        //Create a data object array (3 rows & 2 values), fill with data
        Object[][] data = new Object[3][2];

        data[0][0] = "A003";
        data[0][1] = "Alex";

        data[1][0] = "A004";
        data[1][1] = "Ivan";

        data[2][0] = "A005";
        data[2][1] = "Anderson";

        //We've written 3 rows, so the test using using this DataProvider will run 3 times
        return data;
    }
}
