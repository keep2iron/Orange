package io.github.keep2iron.orange;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import io.github.keep2iron.orange.OrangeOptions;
import io.github.keep2iron.orange.RefreshViewAdapter;
import io.github.keep2iron.orange.annotations.LoadMoreAble;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 16:17
 */
public class LoadMoreAbleWrapper implements LoadMoreAble {

    private final RefreshViewAdapter<?> mRefreshAdapter;
    private final OrangeOptions<?> mOptions;
    private BaseQuickAdapter<?, ? extends BaseViewHolder> mLoadMoreAdapter;

    public LoadMoreAbleWrapper(BaseQuickAdapter<?, ? extends BaseViewHolder> loadMoreAdapter,
                               RefreshViewAdapter<?> refreshAdapter,
                               OrangeOptions<?> options) {
        this.mLoadMoreAdapter = loadMoreAdapter;
        this.mRefreshAdapter = refreshAdapter;
        this.mOptions = options;
    }

    @Override
    public void loadMoreEnable(boolean isEnable) {
        mLoadMoreAdapter.setEnableLoadMore(isEnable);
    }

    @Override
    public void showLoadMoreComplete() {
        if (mOptions.isRefresh) {
            mRefreshAdapter.mRefreshLayout.setEnabled(true);
        }

        //如果刷新时的数据小于PageSize则
        if (mLoadMoreAdapter.getData().size() < OrangeOptions.MAX_ITEM_COUNT) {
            mLoadMoreAdapter.loadMoreEnd(true);
            return;
        }

        if (mLoadMoreAdapter.getData().size() - mLoadMoreAdapter.getHeaderLayoutCount() <= 0) {
            mLoadMoreAdapter.loadMoreEnd();
        }

        mLoadMoreAdapter.loadMoreComplete();
    }

    @Override
    public void showLoadMoreError() {
        mLoadMoreAdapter.loadMoreFail();
    }

    @Override
    public void showLoadMoreEnd() {
        mLoadMoreAdapter.loadMoreEnd();
    }
}
