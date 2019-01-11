import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.sql.*;

public class Methods {

    protected static Connection conn = null;
    protected static Statement stmt = null;
    protected ResultSet rs;

    @BeforeSuite
    public void setup() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:bin\\database.s3db");
        stmt = conn.createStatement();
    }

    @AfterSuite
    public void close() throws SQLException {
        conn.close();
    }

    @DataProvider
    public Object[][] getAgents()
    {
        Object[][] data = new Object[3][2];

        data[0][0] = "A003";
        data[0][1] = "Alex";

        data[1][0] = "A004";
        data[1][1] = "Ivan";

        data[2][0] = "A005";
        data[2][1] = "Anderson";

        return data;
    }
}
