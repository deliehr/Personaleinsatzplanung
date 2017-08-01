package Store;

import java.util.Arrays;

// Auszubildender
public class Empl_Trainee extends Employee {
	private int ausbildungsjahr = -1;
	private boolean[] schultage = new boolean[7];
	
	public Empl_Trainee(int ausbildungsjahr) throws Exception {
		this.setAusbildungsjahr(ausbildungsjahr);
		this.setShortcut("az");
		Arrays.fill(this.getHours(), 5);
		
		// init schultage
		Arrays.fill(this.schultage, false);
		
		if(ausbildungsjahr == 1) {
			this.setMaxHoursWeek(15);
			this.setRole(EmployeeRole.Trainee1Y);
		} 
		
		if(ausbildungsjahr == 2) {
			this.setMaxHoursWeek(20);
			this.setRole(EmployeeRole.Trainee2Y);
		}
		
		if(ausbildungsjahr == 3) {
			this.setMaxHoursWeek(20);
			this.setRole(EmployeeRole.Trainee3Y);
		}
	}

	public int getAusbildungsjahr() {
		return ausbildungsjahr;
	}

	public void setAusbildungsjahr(int ausbildungsjahr) {
		this.ausbildungsjahr = ausbildungsjahr;
	}
	
	public boolean[] getSchultage() {
		return schultage;
	}

	public void setSchultage(boolean[] schultage) {
		this.schultage = schultage;
	}
	
	public void setSchultag(int index, boolean value) throws Exception {
		if(index >= 0 && index <= 6) {
			this.schultage[index] = value;
		} else {
			// index out of bounds
			throw new Exception("Index out of bounds (Schultage >= 0 && <= 6)");
		}
	}

	@Override
	public String getShortcut() {
		return String.format("%s%dy", this.shortcut, this.getAusbildungsjahr());
	}
}