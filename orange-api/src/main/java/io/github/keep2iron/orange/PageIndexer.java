package io.github.keep2iron.orange;

import io.github.keep2iron.orange.annotations.extra.LoadMoreAble;
import io.github.keep2iron.orange.annotations.extra.Refreshable;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/30 13:44
 */
public class PageIndexer {

    Refreshable mRefreshable;
    LoadMoreAble mLoadMoreAble;
    int pageIndex;

    public PageIndexer(Refreshable refreshable, LoadMoreAble loadMoreAble, int pageIndex) {
        mRefreshable = refreshable;
        mLoadMoreAble = loadMoreAble;
        this.pageIndex = pageIndex;
    }


}
