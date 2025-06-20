package groupid.model;

import javafx.scene.paint.Paint;
import lombok.Value;  

@Value
public class BadgeLine {
    String name;
    String iconLiteral;
    Paint color;
    int cost;
}