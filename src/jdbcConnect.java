import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class jdbcConnect {

	String url = "jdbc:oracle:thin:@localhost:1522:xe";
	String user = "kosmo";
	String pwd = "1234";
	Connection conn;
	PreparedStatement pstmt;
	ResultSet resultset;

	public jdbcConnect() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void dbClose(){
		if(resultset!=null)
			try {
				if(resultset!=null)resultset.close();
				if(pstmt!=null)pstmt.close();
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}	

}
