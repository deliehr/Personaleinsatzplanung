package Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Employee {
	private String firstname = "";
	private String lastname = "";
	protected String shortcut = "";
	private int max_hours_week = -1;
	private int max_hours_month = -1;
	private int order = -1;
	private String variableShortcut = "";
	private boolean[] early_shifts = new boolean[6];
	private boolean[] late_shifts = new boolean[6];
	private boolean[] freeShifts = new boolean[6];
	private boolean[] vacationShifts = new boolean[6];
	private EmployeeRole role;
	private double[] hours = new double[6];
	private List<String> errors = new ArrayList<String>();

	public Employee() {
		Arrays.fill(this.early_shifts, false);
        Arrays.fill(this.late_shifts, false);
        Arrays.fill(this.freeShifts, false);
        Arrays.fill(this.vacationShifts, false);
	}

	// region getter & setter
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getShortcut() {
		return shortcut;
	}

	public void setShortcut(String shortcut) {
		this.shortcut = shortcut;
	}

	public int getMaxHoursWeek() {
		return max_hours_week;
	}

	public void setMaxHoursWeek(int max_hours_week) {
		this.max_hours_week = max_hours_week;
	}

	public int getMaxHoursMonth() {
		return max_hours_month;
	}

	public void setMaxHoursMonth(int max_hours_month) {
		this.max_hours_month = max_hours_month;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getVariableShortcut() {
		return variableShortcut.toLowerCase();
	}

	public void setVariableShortcut(String variableShortcut) {
		this.variableShortcut = variableShortcut.toLowerCase();
	}

	public boolean[] getEarlyShifts() {
		return early_shifts;
	}

	public void setEarlyShifts(boolean[] early_shifts) {
		this.early_shifts = early_shifts;
	}

	public boolean[] getLateShifts() {
		return late_shifts;
	}

	public void setLateShifts(boolean[] late_shifts) {
		this.late_shifts = late_shifts;
	}

	public void setEarlyShift(int day, boolean value) {
		try {
			if(day >= 0 && day <= 6) {
				this.early_shifts[day] = value;
			} else {
				throw new Exception("Index out of bounds");
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}
	}

	public void setLateShift(int day, boolean value) {
		try {
			if(day >= 0 && day <= 6) {
				this.late_shifts[day] = value;
			} else {
				throw new Exception("Index out of bounds");
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}
	}

	public EmployeeRole getRole() {
		return role;
	}

	public void setRole(EmployeeRole role) {
		this.role = role;
	}

	public double[] getHours() {
		return hours;
	}

	public void setHours(double[] hours) {
		this.hours = hours;
	}

	public List<String> getErrors() {
		return errors;
	}

    public boolean[] getFreeShifts() {
        return freeShifts;
    }

    public boolean[] getVacationShifts() {
        return vacationShifts;
    }

    // endregion
}