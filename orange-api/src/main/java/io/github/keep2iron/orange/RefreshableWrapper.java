package io.github.keep2iron.orange;

import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import io.github.keep2iron.orange.annotations.Refreshable;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 16:20
 */
public class RefreshableWrapper<T extends ViewGroup> implements Refreshable {

    private final BaseQuickAdapter<?, ? extends BaseViewHolder> mLoadMoreAdapter;
    private final OrangeOptions<?> mOptions;
    RefreshViewAdapter<T> mAdapter;

    public RefreshableWrapper(RefreshViewAdapter<T> refreshViewAdapter,
                              BaseQuickAdapter<?, ? extends BaseViewHolder> loadMoreAdapter,
                              OrangeOptions<?> options) {
        this.mAdapter = refreshViewAdapter;
        this.mLoadMoreAdapter = loadMoreAdapter;
        this.mOptions = options;
    }

    @Override
    public void refreshEnable(boolean isEnable) {
        mAdapter.mRefreshLayout.setEnabled(isEnable);
    }

    @Override
    public void refresh() {
        mAdapter.setRefreshing(true);
    }

    @Override
    public void showRefreshComplete() {
        //如果允许加载更多
        if (mOptions.isLoadMore) {
            mLoadMoreAdapter.setEnableLoadMore(true);
        }

        mAdapter.setRefreshing(false);
    }
}
