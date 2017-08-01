package Store;

import Default.VariableGenerator;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Store {
	// region object variables
	private double scheduledWeekVolume = -1.0;
	private int scheduledWeekHours = -1;
	private int storeNumber = -1;
	private int databaseId = -1;
	private String place = "";
	private String street = "";
	private List<Employee> employeeList = new ArrayList<Employee>();
	// endregion

	// region constructors
	public Store(int databaseId) {
		this.setDatabaseId(databaseId);
	}
	// endregion

	// region object methods
	public boolean readStoreFromDatabase() {
		try {
			VariableGenerator generator = new VariableGenerator();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://debianvmware:3306/pep", "dliehr", "sadom01");

			// read store
			Statement sqlStatement = connection.createStatement();
			String query = String.format("SELECT Store.number, Store.place, Store.street, Store.scheduled_week_hours, Store.scheduled_week_volume FROM Store WHERE Store.id = %d", this.getDatabaseId());
			ResultSet result = sqlStatement.executeQuery(query);
			result.first();

			// set store details
			this.setStoreNumber(result.getInt("number"));
			this.setScheduledWeekHours(result.getInt("scheduled_week_hours"));
			this.setScheduledWeekVolume(result.getInt("scheduled_week_volume"));
			this.setPlace(result.getString("place"));
			this.setStreet(result.getString("street"));

			// read related employees
			sqlStatement = connection.createStatement();
			query = String.format("SELECT * FROM Employee WHERE fk_store_id = %d ORDER BY empl_order ASC;", this.getDatabaseId());
			result = sqlStatement.executeQuery(query);
			result.beforeFirst();

			while(result.next()) {
				EmployeeRole role = EmployeeRole.values()[result.getInt("role")];

				Employee employee;

				switch (role) {
					case StoreAdministration: employee = new Empl_StoreAdministration(); break;
					case FirstSubstitution: employee = new Empl_FirstSubstitution(); break;
					case SecondSubstitution: employee = new Empl_SecondSubstitution(); break;
					case Trainee1Y: employee = new Empl_Trainee(1); break;
					case Trainee2Y: employee = new Empl_Trainee(2); break;
					case Trainee3Y: employee = new Empl_Trainee(3); break;
					case SaleAndCash: employee = new Empl_SaleAndCash(); break;
					default: employee = new Empl_MarginalEmployee(); break;
				}

                employee.setFirstname(result.getString("first_name"));
				employee.setLastname(result.getString("last_name"));
				employee.setOrder(result.getInt("empl_order"));
				employee.setMaxHoursWeek(result.getInt("max_hours_week"));
				employee.setMaxHoursMonth(result.getInt("max_hours_month"));
				employee.setVariableShortcut(generator.generateVariableShortcut(employee.getShortcut()));

				this.addEmployee(employee);

			}
			connection.close();

			return true;
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}

		return false;
	}

	public boolean readStoreFromDatabase(int withDatabaseId) {
		this.setDatabaseId(withDatabaseId);
		return this.readStoreFromDatabase();
	}

	public void addEmployee(Employee employee) {
		this.employeeList.add(employee);
	}

    public int getCountOfRole(EmployeeRole role) {
		int count = 0;

		for(Employee employee:this.employeeList) {
			if(employee.getRole().equals(role)) {
				count++;
			}
		}

		return count;
	}

	public List<Employee> getEmployeeListOfRole(EmployeeRole role) {
		List<Employee> employeeList = new ArrayList<Employee>();

		for(Employee employee:this.employeeList) {
			if(employee.getRole().equals(role)) {
				employeeList.add(employee);
			}
		}

		return employeeList;
	}

	public List<Employee> getAllTrainees() {
		List<Employee> employeeList = new ArrayList<Employee>();

		for(Employee employee:this.employeeList) {
			if(employee.getRole().equals(EmployeeRole.Trainee1Y) || employee.getRole().equals(EmployeeRole.Trainee2Y) || employee.getRole().equals(EmployeeRole.Trainee3Y)) {
				employeeList.add(employee);
			}
		}

		return employeeList;
	}

	public List<Employee> getAllAdministrators() {
		List<Employee> employeeList = new ArrayList<Employee>();

		for(Employee e:this.getEmployeeList()) {
			if(e.getRole().equals(EmployeeRole.StoreAdministration) || e.getRole().equals(EmployeeRole.FirstSubstitution) || e.getRole().equals(EmployeeRole.SecondSubstitution)) {
				employeeList.add(e);
			}
		}

		return employeeList;
	}

	public Employee getEmployeeByVariableShortcut(String dvs) {
		for(Employee e:this.getEmployeeList()) {
			if(e.getVariableShortcut().equals(dvs)) {
				return e;
			}
		}

		return null;
	}

	public int getEmployeeCount() {
	    return this.getEmployeeList().size();
    }
	// endregion

	// region getters & setters
	public double getScheduledWeekVolume() {
		return scheduledWeekVolume;
	}

	public void setScheduledWeekVolume(double scheduledWeekVolume) {
		this.scheduledWeekVolume = scheduledWeekVolume;
	}

	public int getScheduledWeekHours() {
		return scheduledWeekHours;
	}

	public void setScheduledWeekHours(int scheduledWeekHours) {
		this.scheduledWeekHours = scheduledWeekHours;
	}

	public int getStoreNumber() {
		return storeNumber;
	}

	public void setStoreNumber(int storeNumber) {
		this.storeNumber = storeNumber;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(int databaseId) {
		this.databaseId = databaseId;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public List<Employee> getEmployeeList() {
		return employeeList;
	}

	public void setEmployeeList(List<Employee> employeeList) {
		this.employeeList = employeeList;
	}
	// endregion
}