package io.github.keep2iron.sample;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import io.github.keep2iron.orange.annotations.bind.Bind;
import io.github.keep2iron.orange.annotations.bind.BindOnLoadMore;
import io.github.keep2iron.orange.annotations.bind.BindOnRefresh;
import io.github.keep2iron.orange.annotations.extra.LoadMoreAble;
import io.github.keep2iron.orange.annotations.extra.Refreshable;
import io.github.keep2iron.sample.repository.DataServer;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 10:53
 */
public class RecyclerModule {

    @Bind
    Refreshable mRefreshable;

    @Bind
    LoadMoreAble mLoadMoreAble;

    @Bind
    public BaseQuickAdapter<String, ? extends BaseViewHolder> mAdapter;

    public ObservableList<String> mData;

    public RecyclerModule() {
        mData = new ObservableArrayList<>();
        for (int i = 0; i < 10; i++) {
            mData.add(Math.random() * 100 + "");
        }
    }

    int loadMoreCount = 1;

//    @BindOnLoadMore
    public void onLoadMore() {
        DataServer.httpData(new DataServer.Callback<String>() {
            @Override
            public void onSuccess(List<String> list) {
                Log.e("test", "加载更多的次数为 : " + (loadMoreCount++));


                mData.addAll(list);
                mLoadMoreAble.showLoadMoreComplete();
                mAdapter.notifyDataSetChanged();
            }
            @Override

            public void onError() {

            }
        });
    }

    int refreshCount = 1;

    @BindOnRefresh
    public void onRefresh() {
        DataServer.httpData(new DataServer.Callback<String>() {
            @Override
            public void onSuccess(List<String> list) {

                Log.e("test", "刷新的次数为 : " + (refreshCount++));

                mData.clear();
                mData.addAll(list);
                mRefreshable.showRefreshComplete();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError() {

            }
        });
    }
}
