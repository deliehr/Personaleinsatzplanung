package Store;

import java.util.Arrays;

// GFB = Geringfügig Beschäftigter
public class Empl_MarginalEmployee extends Employee {
	private int maxStundenMonat = -1;
	
	public Empl_MarginalEmployee() {
		this.setShortcut("gfb");
		this.setMaxHoursWeek(12);
		this.setMaxHoursMonth(48);
		this.setRole(EmployeeRole.MarginalEmployee);
		Arrays.fill(this.getHours(), 3);
	}

	public int getMaxStundenMonat() {
		return maxStundenMonat;
	}

	public void setMaxStundenMonat(int maxStundenMonat) {
		this.maxStundenMonat = maxStundenMonat;
	}
}