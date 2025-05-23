package groupid.model;

import lombok.Value;


@Value
public class MissionLine {
    String description;
    String frequency;
    double progress;

    @Override
    public String toString() {
    return description + " (" + frequency + ")";
    }
}

