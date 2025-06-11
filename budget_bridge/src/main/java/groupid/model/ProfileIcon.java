package groupid.model;

import javafx.scene.paint.Paint;
import lombok.Value;

@Value
public class ProfileIcon {
    String name;
    String iconLiteral;  // FontAwesome icon literal (e.g., "fas-user", "fas-star")
    Paint color;
    int cost;
    String description;
    
    // Constructor with description
    public ProfileIcon(String name, String iconLiteral, Paint color, int cost, String description) {
        this.name = name;
        this.iconLiteral = iconLiteral;
        this.color = color;
        this.cost = cost;
        this.description = description;
    }
    
    // Constructor without description for simpler creation
    public ProfileIcon(String name, String iconLiteral, Paint color, int cost) {
        this(name, iconLiteral, color, cost, "");
    }
}