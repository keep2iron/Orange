package io.github.keep2iron.orange.annotations.intrnal;

import io.github.keep2iron.orange.annotations.extra.LoadMoreAble;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 12:06
 */
public interface OnLoadMore {

    /**
     * 当进行加载更多的时候
     */
    void onLoadMore();

    /**
     * 为@RecyclerHolder中添加 loadMoreAble
     *
     * @param loadMoreAble
     */
    void setLoadMoreAbleWithHolder(LoadMoreAble loadMoreAble);
}