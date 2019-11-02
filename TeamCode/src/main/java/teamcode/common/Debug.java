package teamcode.common;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an easy way to debug.
 */
public class Debug {

    /**
     * How many lines to display at max in the telemetry window.
     */
    private static final int MAX_NUM_LINES = 10;

    private static final Telemetry telemetry;
    private static final List<Object> lines;

    static {
        if (TTOpMode.currentOpMode() == null) {
            throw new IllegalStateException("Cannot use Debug unless TTOpMode is initialized.");
        }
        telemetry = TTOpMode.currentOpMode().telemetry;
        lines = new ArrayList<>();
    }

    public static void log(Object message) {
        lines.add(message);
        if (lines.size() > MAX_NUM_LINES) {
            lines.remove(0);
        }
        updateTelemetry();
    }

    private static void updateTelemetry() {
        for (Object line : lines) {
            telemetry.addData("Debug", line);
        }
        telemetry.update();
    }

}
