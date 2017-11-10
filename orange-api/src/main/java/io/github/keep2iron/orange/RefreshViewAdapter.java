package io.github.keep2iron.orange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 14:26
 */
public abstract class RefreshViewAdapter<T extends ViewGroup> {

    protected T mRefreshLayout;

    void setRefreshLayout(T mRefreshLayout) {
        this.mRefreshLayout = mRefreshLayout;
    }

    public abstract void setRefreshing(boolean isRefresh);

    @NonNull
    public abstract ViewGroup onCreateRefreshLayout(Context context);

    /**
     * 提供监听器的class字节码
     */
    @NonNull
    public abstract Class<?> onGetListenerClass();
}