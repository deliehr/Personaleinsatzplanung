package Database;

import java.sql.DriverManager;

public class Connection {
    public static java.sql.Connection getNewSQLConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        return DriverManager.getConnection("jdbc:mysql://debianvmware:3306/pep", "dliehr", "sadom01");
    }
}
