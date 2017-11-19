package io.github.keep2iron.orange.annotations.extra;

/**
 * Created by keep-iron on 17-11-18.
 */

public interface SlideDirection {

    /**
     * Up direction, used for swipe & drag control.
     */
    public static final int UP = 1;

    /**
     * Down direction, used for swipe & drag control.
     */
    public static final int DOWN = 1 << 1;

    /**
     * Left direction, used for swipe & drag control.
     */
    public static final int LEFT = 1 << 2;

    /**
     * Right direction, used for swipe & drag control.
     */
    public static final int RIGHT = 1 << 3;

    // If you change these relative direction values, update Callback#convertToAbsoluteDirection,
    // Callback#convertToRelativeDirection.
    /**
     * Horizontal start direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction. Used for swipe & drag control.
     */
    public static final int START = LEFT << 2;

    /**
     * Horizontal end direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction. Used for swipe & drag control.
     */
    public static final int END = RIGHT << 2;
}
