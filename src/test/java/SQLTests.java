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

    @BeforeClass
    public void testConnection() throws SQLException {
        assert(!conn.isClosed());
    }

    @Test
    public void testCount() throws SQLException {
        final String QUERY = "SELECT COUNT(*) FROM ORDERS";
        rs = stmt.executeQuery(QUERY);

        final String RES = rs.getString(1);
        assertEquals(RES, "34");
    }

    @Test (dataProvider = "getAgents")
    public void testSelect(final String CODE, final String NAME) throws SQLException {
        final String QUERY = "SELECT AGENT_NAME FROM AGENTS WHERE AGENT_CODE = '" + CODE + "'";
        rs = stmt.executeQuery(QUERY);

        final String RES = rs.getString(1);
        assertEquals(RES, NAME);
    }

    @Test
    public void testInsert() throws SQLException {
        final String COUNT = "SELECT COUNT(*) FROM CUSTOMER";
        rs = stmt.executeQuery(COUNT);
        final int ROWS = rs.getInt(1);

        final String CCODE = RandomStringUtils.randomAlphanumeric(6);
        final String INS = "INSERT INTO CUSTOMER (CUST_CODE) VALUES ('" + CCODE + "')";
        stmt.execute(INS);
        rs = stmt.executeQuery(COUNT);

        assertEquals(rs.getInt(1), ROWS + 1);
    }

    @Test (dependsOnMethods = "testInsert")
    public void testDelete() throws SQLException {
        final String COUNT = "SELECT COUNT(*) FROM CUSTOMER";
        rs = stmt.executeQuery(COUNT);
        final int ROWS = rs.getInt(1);

        final String INS = "DELETE FROM CUSTOMER WHERE CUST_NAME IS NULL";
        stmt.execute(INS);
        rs = stmt.executeQuery(COUNT);

        assertEquals(rs.getInt(1), ROWS - 1);
    }

    @Test
    public void testRandUpdate() throws SQLException {
        final String SQUERY1 = "SELECT ORD_NUM FROM ORDERS ORDER BY RANDOM() LIMIT 1";
        rs = stmt.executeQuery(SQUERY1);
        final String ORDNUM = rs.getString(1);

        final String NDESC = RandomStringUtils.randomAlphanumeric(8);
        final int NAMOUNT = RandomUtils.nextInt(1, 10000);

        final String UPDATE = "UPDATE ORDERS SET ORD_AMOUNT = " + NAMOUNT + ", ORD_DESCRIPTION = '" +
                NDESC + "' WHERE ORD_NUM = " + ORDNUM;
        stmt.execute(UPDATE);

        final String SQUERY2 = "SELECT ORD_AMOUNT, ORD_DESCRIPTION FROM ORDERS\n" +
                "WHERE ORD_NUM = " + ORDNUM;
        rs = stmt.executeQuery(SQUERY2);

        int selAmount = 0;
        String selDesc = null;
        while (rs.next()) {
            selAmount = rs.getInt("ORD_AMOUNT");
            selDesc = rs.getString("ORD_DESCRIPTION");
        }

        assertEquals(selAmount, NAMOUNT);
        assertEquals(selDesc, NDESC);
    }

    @Test
    public void testCompareExcel() throws SQLException, IOException {
        ArrayList<Customer> excelData = new ArrayList<>();
        ArrayList<Customer> sqlData = new ArrayList<>();

        InputStream is = new FileInputStream("bin\\customers.xlsx");
        ReadableWorkbook wbr = new ReadableWorkbook(is);
        Sheet sheet = wbr.getFirstSheet();

        try (Stream<Row> rows = sheet.openStream()) {
                rows.forEach(r -> {
                    Customer cust = new Customer(r.getCellAsString(0).orElse(null),
                            r.getCellAsString(1).orElse(null),
                            r.getCellAsString(2).orElse(null));
                    excelData.add(cust);
                });
            }

        final String QUERY = "SELECT CS.CUST_CODE, AG.AGENT_CODE, CS.CUST_NAME\n" +
                "FROM AGENTS AS AG JOIN CUSTOMER CS ON AG.AGENT_CODE = CS.AGENT_CODE\n" +
                "ORDER BY AG.AGENT_CODE";
        rs = stmt.executeQuery(QUERY);

        while(rs.next()) {
            Customer cust = new Customer(rs.getString(1),
                    rs.getString(2),
                    rs.getString(3));
            sqlData.add(cust);
        }

        assertEquals(sqlData, excelData);
    }

    @Test (expectedExceptions = SQLException.class)
    public void testNegNoTable() throws SQLException {
        final String QUERY = "SELECT * FROM EMPLOYEES";
        stmt.executeQuery(QUERY);
    }

    @Test
    public void testNegEmpty() throws SQLException {
        final String RTEXT = RandomStringUtils.randomAlphanumeric(20);
        final String QUERY = "SELECT * FROM CUSTOMER WHERE CUST_NAME = '" + RTEXT + "'";
        rs = stmt.executeQuery(QUERY);

        //https://stackoverflow.com/questions/867194/java-resultset-how-to-check-if-there-are-any-results/6813771#6813771
        assertFalse(rs.isBeforeFirst());
    }

    @Test (expectedExceptions = SQLException.class)
    public void testNegPrimaryKey() throws SQLException {
        final String RNAME = RandomStringUtils.randomAlphanumeric(20);
        final String INS = "INSERT INTO AGENTS (AGENT_NAME) VALUES ('" + RNAME + "')";
        stmt.execute(INS);
    }

}
