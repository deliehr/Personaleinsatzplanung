package Store;

import java.util.Arrays;

// Marktleitung
public class Empl_StoreAdministration extends Employee {
	public Empl_StoreAdministration() {
		this.setShortcut("ml");
		this.setMaxHoursWeek(40);
		this.setRole(EmployeeRole.StoreAdministration);
		Arrays.fill(this.getHours(), 8);
	}
}
