package io.github.keep2iron.orange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.keep2iron.orange.annotations.OnLoadMore;
import io.github.keep2iron.orange.annotations.OnRefresh;
import io.github.keep2iron.orange.api.R;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 22:00
 */
public class Orange {
    private final static Map<Class, Constructor<? extends BaseQuickAdapter>> BRAVH_BINDER = new HashMap<>();
    private final static Map<Class, Constructor<? extends OnRefresh>> REFRESH_BINDER = new HashMap<>();


//    public static <T> void inject(@NonNull final OrangeOptions<T> options,
//                                  @NonNull Object viewLayer){
//        inject(options,viewLayer,viewLayer);
//    }

    /**
     * 将代码注入到adapterHolder中
     *
     * @param options
     * @param viewObj 创建出的Adapter的持有者对象
     * @param <T>
     */
    public static <T> void inject(@NonNull final OrangeOptions<T> options,
                                  @NonNull Object viewObj) {
        //viewModule是注入LoadMoreAble和RefreshAble对象的一个注入对象
        Object moduleObj = options.mModule;

        ProxyRefreshListener proxyListener = new ProxyRefreshListener();

        BaseQuickAdapter<T, ? extends BaseViewHolder> recyclerAdapter = createRecyclerAdapter(options, viewObj,moduleObj);
        OnRefresh onRefreshItem = bindRefreshInstance(options, viewObj, moduleObj,proxyListener);

        //set listener
        RecyclerView recyclerView = options.recyclerView;
        final RefreshViewAdapter refreshAdapter = options.refreshAdapter;
        if (options.isLoadMore) {
            final OnLoadMore onLoadItem = (OnLoadMore) recyclerAdapter;
            //将控制加载更多的对象进行注入到obj中
            onLoadItem.setLoadMoreAbleWithHolder(new LoadMoreAbleWrapper(recyclerAdapter, refreshAdapter, options));
            recyclerAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    //如果构建中进行了刷新，则在加载更多的过程中，停止刷新操作
                    if (options.isRefresh) {
                        refreshAdapter.mRefreshLayout.setEnabled(false);
                    }
                    onLoadItem.onLoadMore();
                }
            }, recyclerView);
        }

        if (options.isRefresh) {
            proxyListener.setLoadMoreAdapter(recyclerAdapter);
            onRefreshItem.setRefreshableWithHolder(new RefreshableWrapper(options.refreshAdapter, recyclerAdapter, options));
        }

        if (options.layoutManager != null) {
            recyclerView.setLayoutManager(options.layoutManager);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    private static <T> OnRefresh bindRefreshInstance(@NonNull OrangeOptions<T> options, Object viewObj, Object moduleObj, ProxyRefreshListener proxyListener) {

        Constructor<? extends OnRefresh> constructor = loadRefreshableAdapterConstructor(viewObj,moduleObj);

        options.isRefresh = (constructor != null);
        //改模块并不存在刷新操作
        if (constructor == null) {
            return null;
        }

        options.checkValueWhenCreateRefreshInstance();
        RefreshViewAdapter refreshAdapter = options.refreshAdapter;

        //onCreateRefreshLayout()
        ViewGroup refreshView = refreshAdapter.onCreateRefreshLayout(options.context);
        refreshAdapter.setRefreshLayout(refreshView);

        //onGetListenerClass()
        Class<?> listenerClass = refreshAdapter.onGetListenerClass();
        if (listenerClass == null) {
            throw new IllegalArgumentException("listener class is null,please set a class extends " +
                    "io.github.keep2iron.orange.RefreshViewAdapter and override onGetListenerClass() method");
        }

        Object listener = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[]{listenerClass}, proxyListener);

        OnRefresh onRefreshItem = null;
        try {
            onRefreshItem = constructor.newInstance(viewObj, moduleObj,refreshView, listener);
            proxyListener.setOnRefreshItem(onRefreshItem);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (onRefreshItem == null) {
            throw new IllegalArgumentException("create refresh instance error.");
        }

        //inject refreshLayout into your layout
        RecyclerView recyclerView = options.recyclerView;
        ViewGroup parent = (ViewGroup) recyclerView.getParent();
        parent.removeView(recyclerView);
        parent.addView(refreshView);
        refreshView.setId(R.id.refreshLayout);
        refreshView.setLayoutParams(recyclerView.getLayoutParams());
        refreshView.addView(recyclerView);

        return onRefreshItem;
    }


    private static <T> BaseQuickAdapter<T, ? extends BaseViewHolder> createRecyclerAdapter(@NonNull OrangeOptions<T> options, Object viewObj, Object moduleObj) {
        options.checkValueWhenCrateRecyclerAdapter();

        Constructor<? extends BaseQuickAdapter> constructor = loadBRAVHAdapterConstructor(viewObj,moduleObj);

        if (constructor == null) {
            throw new IllegalArgumentException("can't find recycler adapter class...");
        }

        Context ctx = options.context.getApplicationContext();
        RecyclerView recyclerView = options.recyclerView;
        BaseQuickAdapter<T, ? extends BaseViewHolder> baseQuickAdapter = null;

        try {
            baseQuickAdapter = constructor.newInstance(ctx, options.data, viewObj,moduleObj);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (baseQuickAdapter == null) {
            throw new IllegalArgumentException("create baseQuickAdapter error");
        }

        options.isLoadMore = (baseQuickAdapter instanceof OnLoadMore);

        return baseQuickAdapter;
    }

    private static Constructor<? extends OnRefresh> loadRefreshableAdapterConstructor(Object viewObj, Object moduleObj) {
        Class<?> viewClass = viewObj.getClass();
        Class<?> moduleClass = moduleObj.getClass();

        Constructor<? extends OnRefresh> constructor = REFRESH_BINDER.get(viewClass);
        if (constructor != null) {
            return constructor;
        }

        String clsName = viewClass.getName();

        try {
            Class<? extends OnRefresh> bindingClass = (Class<? extends OnRefresh>) Class.forName(clsName + "RefreshAdapter");
            constructor = bindingClass.getConstructor(viewClass, moduleClass,View.class, Object.class);
            REFRESH_BINDER.put(viewClass, constructor);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return constructor;
    }

    private static Constructor<? extends BaseQuickAdapter> loadBRAVHAdapterConstructor(Object viewObj, Object moduleObj) {
        Class<?> viewClass = viewObj.getClass();
        Class<?> moduleClass = moduleObj.getClass();
        Constructor<? extends BaseQuickAdapter> constructor = BRAVH_BINDER.get(viewClass);
        if (constructor != null) {
            return constructor;
        }

        String clsName = viewClass.getName();
        try {
            Class<? extends BaseQuickAdapter> bindingClass = (Class<? extends BaseQuickAdapter>) Class.forName(clsName + "Adapter");
            constructor = bindingClass.getConstructor(Context.class, List.class, viewClass,moduleClass);
            BRAVH_BINDER.put(viewClass, constructor);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("@RecyclerHolder's module class \n" +
                    "isn't match OrangeOptions's module class," +
                    "your should set @RecyclerHolder module");
        }
        return constructor;
    }
}