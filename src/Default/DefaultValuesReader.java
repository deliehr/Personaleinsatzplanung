package Default;

import Store.EmployeeRole;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultValuesReader {
    private List<DefaultValue> defaultValueList = new ArrayList<DefaultValue>();

    public DefaultValuesReader(String xmlFilePath) {
        try {
            File xmlDefaultValuesFile = new File(xmlFilePath);

            if(xmlDefaultValuesFile.exists()) {
                // read xml file
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder parser = factory.newDocumentBuilder();
                Document doc = parser.parse(xmlDefaultValuesFile);

                // normalize
                doc.getDocumentElement().normalize();

                // get each Element Employee
                NodeList nodeListEmployees = doc.getElementsByTagName("Employee");
                for(int i=0;i < nodeListEmployees.getLength();i++) {
                    // new default value
                    DefaultValue defaultValue = new DefaultValue();

                    // iterate childs
                    NodeList childNodes = nodeListEmployees.item(i).getChildNodes();
                    for(int j=0;j < childNodes.getLength();j++) {
                        if(childNodes.item(j).getNodeName().equals("Type")) {
                            String role = childNodes.item(j).getChildNodes().item(0).getNodeValue();
                            switch (role) {
                                case "StoreAdministration": defaultValue.setRole(EmployeeRole.StoreAdministration); break;
                                case "FirstSubstitution": defaultValue.setRole(EmployeeRole.FirstSubstitution); break;
                                case "SecondSubstitution": defaultValue.setRole(EmployeeRole.SecondSubstitution); break;
                                case "Trainee1Y": defaultValue.setRole(EmployeeRole.Trainee1Y); break;
                                case "Trainee2Y": defaultValue.setRole(EmployeeRole.Trainee2Y); break;
                                case "Trainee3Y": defaultValue.setRole(EmployeeRole.Trainee3Y); break;
                                case "SaleAndCash": defaultValue.setRole(EmployeeRole.SaleAndCash); break;
                                default: defaultValue.setRole(EmployeeRole.MarginalEmployee); break;
                            }
                        }

                        if(childNodes.item(j).getNodeName().equals("MaxHoursWeekly")) {
                            int maxHoursWeekly = Integer.valueOf(childNodes.item(j).getChildNodes().item(0).getNodeValue());
                            defaultValue.setMaxHoursWeekly(maxHoursWeekly);
                        }

                        if(childNodes.item(j).getNodeName().equals("MaxHoursMonthly")) {
                            int maxHoursMonthly = Integer.valueOf(childNodes.item(j).getChildNodes().item(0).getNodeValue());
                            defaultValue.setMaxHoursMonthly(maxHoursMonthly);
                        }
                    }

                    // add default value to list
                    this.defaultValueList.add(defaultValue);
                }
            }
        } catch (Exception e) {
            System.out.println(String.format("Fehler: %s", e.toString()));
        }
    }

    public DefaultValue getDefaultValueByRole(EmployeeRole role) {
        for(DefaultValue value:this.defaultValueList) {
            if(role.equals(value.getRole())) {
                return value;
            }
        }

        return null;
    }

    public List<DefaultValue> getDefaultValueList() {
        return defaultValueList;
    }

    public static class DefaultValue {
        private EmployeeRole role = null;
        private int maxHoursWeekly = -1;
        private int maxHoursMonthly = -1;

        public DefaultValue() { }

        public EmployeeRole getRole() {
            return role;
        }

        public void setRole(EmployeeRole role) {
            this.role = role;
        }

        public int getMaxHoursWeekly() {
            return maxHoursWeekly;
        }

        public void setMaxHoursWeekly(int maxHoursWeekly) {
            this.maxHoursWeekly = maxHoursWeekly;
        }

        public int getMaxHoursMonthly() {
            return maxHoursMonthly;
        }

        public void setMaxHoursMonthly(int maxHoursMontly) {
            this.maxHoursMonthly = maxHoursMontly;
        }
    }
}