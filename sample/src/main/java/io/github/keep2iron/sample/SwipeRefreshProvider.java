package io.github.keep2iron.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ViewGroup;

import io.github.keep2iron.orange.RefreshViewAdapter;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 14:32
 */
public class SwipeRefreshProvider extends RefreshViewAdapter<SwipeRefreshLayout> {

    @Override
    public void setRefreshing(boolean isRefresh) {
        mRefreshLayout.setRefreshing(isRefresh);
    }

    @NonNull
    @Override
    public ViewGroup onCreateRefreshLayout(Context context) {
        return new SwipeRefreshLayout(context);
    }

    @Override
    public Class<?> onGetListenerClass() {
        return SwipeRefreshLayout.OnRefreshListener.class;
    }
}