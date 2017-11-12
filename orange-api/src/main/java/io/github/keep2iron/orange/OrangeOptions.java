package io.github.keep2iron.orange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 16:29
 */
public class OrangeOptions<T> {
    /**
     * 加载一页的item的数量
     */
    public final static int MAX_ITEM_COUNT = 8;

    Context context;
    List<T> data;
    RecyclerView recyclerView;
    RefreshViewAdapter refreshAdapter;
    int pageItem = MAX_ITEM_COUNT;

    LinearLayoutManager layoutManager;

    boolean isLoadMore;
    boolean isRefresh;

    Object mModule;


    public OrangeOptions(Object module,@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.mModule = module;
    }

    public void buildVertical() {
        layoutManager = new WrapContentLinearLayoutManager(context);
    }

    public void buildRecyclerViewAdapter(RecyclerView recyclerView, @NonNull List<T> data) {
        this.recyclerView = recyclerView;
        this.data = data;
    }

    public void buildPageItem(int pageItem) {
        this.pageItem = pageItem;
    }

    public void buildRefreshAdapter(RefreshViewAdapter adapter) {
        this.refreshAdapter = adapter;
    }

    /**
     * 当执行加载更多时，检查所需要的值
     */
    void checkValueWhenCrateRecyclerAdapter() {
        if (recyclerView == null) {
            throw new IllegalArgumentException("you must set recyclerView in OrangeOptions,please call buildRecyclerViewAdapter()!!");
        }
        if (data == null) {
            throw new IllegalArgumentException("you must set data in OrangeOptions,please call buildRecyclerViewAdapter()!!");
        }
    }

    /**
     * 当绑定刷新时，检查必要的值
     */
    void checkValueWhenCreateRefreshInstance() {
        if (refreshAdapter == null) {
            throw new IllegalArgumentException("you must set refreshAdapter in OrangeOptions,please call buildRefreshAdapter()!!");
        }
    }
}