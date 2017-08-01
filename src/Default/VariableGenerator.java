package Default;

import java.util.ArrayList;
import java.util.List;

public class VariableGenerator {
    private List<String> listVariableShortcuts = null;

    public VariableGenerator() {
        listVariableShortcuts = new ArrayList<String>();
    }

    public String generateVariableShortcut(String shortcut) {
        boolean newShortcutFound = false;
        String variableShortcut = "";

        int i = 1;
        while(!newShortcutFound) {
            String newVariableShortcut = String.format("x_%s_%d", shortcut, i);

            if(this.listVariableShortcuts.contains(newVariableShortcut)) {
                i++;
            } else {
                variableShortcut = newVariableShortcut;
                this.listVariableShortcuts.add(newVariableShortcut);
                newShortcutFound = true;
            }
        }

        return variableShortcut;
    }
}
