package io.github.keep2iron.orange;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import io.github.keep2iron.orange.annotations.intrnal.OnRefresh;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 17:04
 */
public class ProxyRefreshListener implements InvocationHandler {

    OnRefresh mOnRefreshItem;
    BaseQuickAdapter<?, ? extends BaseViewHolder> recyclerAdapter;

    public void setOnRefreshItem(OnRefresh mOnRefreshItem) {
        this.mOnRefreshItem = mOnRefreshItem;
    }

    public void setLoadMoreAdapter(BaseQuickAdapter<?, ? extends BaseViewHolder> recyclerAdapter) {
        this.recyclerAdapter = recyclerAdapter;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (mOnRefreshItem == null) {
            throw new IllegalArgumentException("mOnRefreshItem can't be null");
        }

        //正在刷新的过程中禁用加载更多
        if (recyclerAdapter != null) {
            recyclerAdapter.setEnableLoadMore(false);
        }

        mOnRefreshItem.onRefresh();

        return null;
    }
}
