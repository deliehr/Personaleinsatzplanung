package Store;

import java.util.Arrays;

// Zweite Marktvertretung
public class Empl_SecondSubstitution extends Employee {
	public Empl_SecondSubstitution() {
		this.setShortcut("2vt");
		this.setMaxHoursWeek(20);
		this.setRole(EmployeeRole.SecondSubstitution);
		Arrays.fill(this.getHours(), 4);
	}
}
