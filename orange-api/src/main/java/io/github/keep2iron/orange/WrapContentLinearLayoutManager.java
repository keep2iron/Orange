/*
 * Create bt Keep2iron on 17-6-21 下午5:44
 * Copyright (c) 2017. All rights reserved.
 */

package io.github.keep2iron.orange;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by keep2iron on .
 * write the powerful code ！
 * website : keep2iron.github.io
 *
 * 此方案是为了解决
 * java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
 *
 * 这个问题产生的原因在于数据被修改在不同的线程中，最好的方案是检查哪里数据被进行修改了
 * This problem is caused by RecyclerView Data modified in different thread.
 * The best way is checking all data access. And a workaround is wrapping LinearLayoutManager.
 *
 * @link https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 */
public class WrapContentLinearLayoutManager extends LinearLayoutManager {
    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}