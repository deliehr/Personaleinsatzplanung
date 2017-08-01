package Store;

import java.util.Arrays;

// Erste Marktvertretung
public class Empl_FirstSubstitution extends Employee {
	public Empl_FirstSubstitution() {
		this.setShortcut("1vt");
		this.setMaxHoursWeek(30);
		this.setRole(EmployeeRole.FirstSubstitution);
		Arrays.fill(this.getHours(), 6);
	}
}
