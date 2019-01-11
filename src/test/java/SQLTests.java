import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class SQLTests extends Methods {

    //Check that DB connection is open before running any tests in this class
    @BeforeClass
    public void testConnection() throws SQLException {
        assert(!conn.isClosed());
    }

    //Count the number of entries in the ORDERS table
    @Test
    public void testCount() throws SQLException {
        //Execute SQL query, store result in ResultSet
        rs = stmt.executeQuery("SELECT COUNT(*) FROM ORDERS");

        //Export result to a string
        final String RES = rs.getString(1);

        //Assert the result matches 34
        assertEquals(RES, "34");
    }

    //Validate data in AGENTS table by comparing codes & names
    //This test uses a dataProvider (listed in Methods), so it will run 3 times with different values
    @Test (dataProvider = "getAgents")
    public void testSelect(final String CODE, final String NAME) throws SQLException {
        //Select agent name based on code
        rs = stmt.executeQuery("SELECT AGENT_NAME FROM AGENTS WHERE AGENT_CODE = '" + CODE + "'");
        final String RES = rs.getString(1);

        //Assert that names match
        assertEquals(RES, NAME);
    }

    //Insert random generated data into CUSTOMER table
    @Test
    public void testInsert() throws SQLException {
        //Retrieve current row count
        rs = stmt.executeQuery("SELECT COUNT(*) FROM CUSTOMER");
        final int ROWS = rs.getInt(1);

        //Generate random 6-char Customer Code using Apache Random Utils
        final String CCODE = RandomStringUtils.randomAlphanumeric(6);

        //Execute the insert, retrieve new row count
        stmt.execute("INSERT INTO CUSTOMER (CUST_CODE) VALUES ('" + CCODE + "')");
        rs = stmt.executeQuery("SELECT COUNT(*) FROM CUSTOMER");

        //Assert that the new count is +1
        assertEquals(rs.getInt(1), ROWS + 1);
    }

    //Delete the row added in 'testInsert'
    @Test (dependsOnMethods = "testInsert")
    public void testDelete() throws SQLException {
        //Retrieve current row count
        rs = stmt.executeQuery("SELECT COUNT(*) FROM CUSTOMER");
        final int ROWS = rs.getInt(1);

        //Delete a row where Customer Name is NULL and get new counter.
        //We left it NULL in testInsert, so this will delete only 1 row.
        stmt.execute("DELETE FROM CUSTOMER WHERE CUST_NAME IS NULL");
        rs = stmt.executeQuery("SELECT COUNT(*) FROM CUSTOMER");

        //Assert that the number of rows decreased by 1
        assertEquals(rs.getInt(1), ROWS - 1);
    }

    //Update a random row with random data
    @Test
    public void testRandUpdate() throws SQLException {
        //Select 1 random Order from ORDERS
        rs = stmt.executeQuery("SELECT ORD_NUM FROM ORDERS ORDER BY RANDOM() LIMIT 1");
        final String ORDNUM = rs.getString(1);

        //Generate random data for update
        final String NDESC = RandomStringUtils.randomAlphanumeric(8);
        final int NAMOUNT = RandomUtils.nextInt(1, 10000);

        //Update the Amount and Description fields for the random Order
        stmt.execute("UPDATE ORDERS SET ORD_AMOUNT = " + NAMOUNT + ", ORD_DESCRIPTION = '" +
                NDESC + "' WHERE ORD_NUM = " + ORDNUM);

        //Retrieve the updated values by the same Order Number
        rs = stmt.executeQuery("SELECT ORD_AMOUNT, ORD_DESCRIPTION FROM ORDERS\n" +
                "WHERE ORD_NUM = " + ORDNUM);

        //Assert that the retrieved values match the generated ones
        assertEquals(rs.getInt("ORD_AMOUNT"), NAMOUNT);
        assertEquals(rs.getString("ORD_DESCRIPTION"), NDESC);
    }

    //Compare data from an existing Excel file with the data in a SQL table
    @Test
    public void testCompareExcel() throws SQLException, IOException {
        //Arrays which will hold the data (consist of Customer class objects)
        ArrayList<Customer> excelData = new ArrayList<>();
        ArrayList<Customer> sqlData = new ArrayList<>();

        //Objects for reading data from the Excel file in bin project folder
        InputStream is = new FileInputStream("bin\\customers.xlsx");
        ReadableWorkbook wbr = new ReadableWorkbook(is);
        Sheet sheet = wbr.getFirstSheet();

        // https://github.com/dhatim/fastexcel
        // Read data from the Excel sheet
        try (Stream<Row> rows = sheet.openStream()) {
                rows.forEach(r -> {
                    //Create a Customer object by passing data from Excel to the class constructor
                    Customer cust = new Customer(r.getCellAsString(0).orElse(null),
                            r.getCellAsString(1).orElse(null),
                            r.getCellAsString(2).orElse(null));
                    //Add Customer object to the ExcelData array
                    excelData.add(cust);
                });
            }

        //Select the data from the AGENTS table
        rs = stmt.executeQuery("SELECT CS.CUST_CODE, AG.AGENT_CODE, CS.CUST_NAME\n" +
                "FROM AGENTS AS AG JOIN CUSTOMER CS ON AG.AGENT_CODE = CS.AGENT_CODE\n" +
                "ORDER BY AG.AGENT_CODE");

        //Iterate through the SQL ResultSet
        while(rs.next()) {
            //Create a Customer object
            Customer cust = new Customer(rs.getString(1),
                    rs.getString(2),
                    rs.getString(3));
            //Add Customer object to the SQLData array
            sqlData.add(cust);
        }

        //Compare the resulting two arrays.
        //For this to work the equals method in Customer needs to be overridden (see Customer class).
        assertEquals(sqlData, excelData);
    }

    //Negative tests start here.
    //Select from a non-existing table. Expecting to catch an SQL exception.
    @Test (expectedExceptions = SQLException.class)
    public void testNegNoTable() throws SQLException {
        stmt.executeQuery("SELECT * FROM EMPLOYEES");
    }

    //SELECT which returns no results
    @Test
    public void testNegEmpty() throws SQLException {
        //Generate random Customer Name and SELECT data for it
        final String RTEXT = RandomStringUtils.randomAlphanumeric(20);
        rs = stmt.executeQuery("SELECT * FROM CUSTOMER WHERE CUST_NAME = '" + RTEXT + "'");

        // https://stackoverflow.com/questions/867194/java-resultset-how-to-check-if-there-are-any-results/6813771#6813771
        // Verify that the ResultSet is empty (no results)
        assertFalse(rs.isBeforeFirst());
    }

    //INSERT without a Primary Key
    @Test (expectedExceptions = SQLException.class)
    public void testNegPrimaryKey() throws SQLException {
        //Generate random new Agent Name, try to INSERT without Agent Code (Primary Key and cannot be NULL)
        final String RNAME = RandomStringUtils.randomAlphanumeric(20);
        stmt.execute("INSERT INTO AGENTS (AGENT_NAME) VALUES ('" + RNAME + "')");
    }

}
