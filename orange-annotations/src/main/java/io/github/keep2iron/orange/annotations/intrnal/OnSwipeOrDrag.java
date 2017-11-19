package io.github.keep2iron.orange.annotations.intrnal;

/**
 * Created by keep-iron on 17-11-18.
 */
public interface OnSwipeOrDrag<T> {

    void attachRecyclerView(T recyclerView,Object onDragListener,Object onSwipeListener);

}
