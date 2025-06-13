package groupid.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class CooldownManager {
    private static final CooldownManager instance = new CooldownManager();

    private final IntegerProperty secondsLeft = new SimpleIntegerProperty(0);
    private Timeline timer;

    private CooldownManager() {}

    public static CooldownManager getInstance() {
        return instance;
    }

    public void startCooldown(int durationInSeconds) {
        if (isOnCooldown()) return;

        secondsLeft.set(durationInSeconds);

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft.set(secondsLeft.get() - 1);
            if (secondsLeft.get() <= 0) {
                timer.stop();
            }
        }));
        timer.setCycleCount(durationInSeconds);
        timer.play();
    }

    public boolean isOnCooldown() {
        return secondsLeft.get() > 0;
    }

    public IntegerProperty secondsLeftProperty() {
        return secondsLeft;
    }

    public int getSecondsLeft() {
        return secondsLeft.get();
    }
}
