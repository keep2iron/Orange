package io.github.keep2iron.orange;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.keep2iron.orange.annotations.extra.LoadMoreAble;
import io.github.keep2iron.orange.annotations.extra.Refreshable;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/30 13:44
 */
public class PageIndexer {

    public @Nullable Refreshable mRefreshable;
    public @Nullable LoadMoreAble mLoadMoreAble;

    private int pageIndex;
    private int mDefaultIndex;

    public PageIndexer(@NonNull Refreshable refreshable, @NonNull LoadMoreAble loadMoreAble, int defaultIndex) {
        mRefreshable = refreshable;
        mLoadMoreAble = loadMoreAble;
        this.pageIndex = defaultIndex;

        mLoadMoreAble.loadMoreEnable(false);

        mDefaultIndex = defaultIndex;
    }

    public void indexAdd() {
        pageIndex++;
    }

    public void restoreIndex() {
        pageIndex = mDefaultIndex;
    }

    public int getDefaultIndex() {
        return mDefaultIndex;
    }

    public int getCurrentIndex() {
        return pageIndex;
    }
}
