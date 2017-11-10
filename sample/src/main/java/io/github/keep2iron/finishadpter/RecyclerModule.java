package io.github.keep2iron.finishadpter;

import android.databinding.ObservableArrayList;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import javax.inject.Inject;

import io.github.keep2iron.finishadpter.databinding.ItemDefaultBinding;
import io.github.keep2iron.orange.Orange;
import io.github.keep2iron.orange.OrangeOptions;
import io.github.keep2iron.orange.annotations.BindConvert;
import io.github.keep2iron.orange.annotations.BindOnLoadMore;
import io.github.keep2iron.orange.annotations.BindOnRefresh;
import io.github.keep2iron.orange.annotations.LoadMoreAble;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.annotations.Refreshable;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 10:53
 */
@RecyclerHolder(type = String.class, items = {R.layout.item_default}, isUseDataBinding = true)
public class RecyclerModule {

    @Inject
    Refreshable mRefreshable;

    @Inject
    LoadMoreAble mLoadMoreAble;

    @Inject
    BaseQuickAdapter<String, ? extends BaseViewHolder> mAdapter;

    public ObservableArrayList<String> mData;

    public RecyclerModule(RecyclerView recyclerView) {
        mData = new ObservableArrayList<>();

        OrangeOptions<String> options = new OrangeOptions<>(recyclerView.getContext());
        options.buildVertical();
        options.buildRecyclerViewAdapter(recyclerView, mData);
        options.buildRefreshAdapter(new SwipeRefreshProvider());
        options.buildPageItem(15);

        Orange.inject(options, this);
    }

    @BindConvert
    public void render(ViewDataBinding binding, String item) {
        ItemDefaultBinding itemBinding = (ItemDefaultBinding) binding;
        itemBinding.itemText.setText(item);
    }

    @BindOnLoadMore
    public void onLoadMore() {
        DataServer.httpData(new DataServer.Callback<String>() {
            @Override
            public void onSuccess(List<String> list) {
                mData.addAll(list);
                mLoadMoreAble.showLoadMoreComplete();
            }

            @Override
            public void onError() {

            }
        });
    }

    @BindOnRefresh
    public void onRefresh() {
        DataServer.httpData(new DataServer.Callback<String>() {
            @Override
            public void onSuccess(List<String> list) {
                mData.addAll(list);
                mRefreshable.showRefreshComplete();
            }

            @Override
            public void onError() {

            }
        });
    }
}
