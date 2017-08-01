package Store;

import java.util.Arrays;

// Verkauf und Kasse
public class Empl_SaleAndCash extends Employee {
	public Empl_SaleAndCash() {
		this.setShortcut("vk");
		this.setMaxHoursWeek(15);
		this.setRole(EmployeeRole.SaleAndCash);
		Arrays.fill(this.getHours(), 5);
	}
}