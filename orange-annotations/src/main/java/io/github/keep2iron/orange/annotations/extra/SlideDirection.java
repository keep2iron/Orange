package io.github.keep2iron.orange.annotations.extra;

/**
 * @author keep-iron
 * @date 17-11-18
 */
public interface SlideDirection {

    int UP = 1;

    int DOWN = 1 << 1;

    int LEFT = 1 << 2;

    int RIGHT = 1 << 3;

    int START = LEFT << 2;

    int END = RIGHT << 2;
}
