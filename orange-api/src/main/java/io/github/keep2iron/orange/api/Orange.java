package io.github.keep2iron.orange.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 22:00
 */
public class Orange {
    private final static Map<Class, Constructor<? extends BaseQuickAdapter>> BINDER = new HashMap<>();

    public static <T> BaseQuickAdapter<T, ? extends BaseViewHolder> bind(@NonNull Object obj,
                                                                         @NonNull RecyclerView recyclerView,
                                                                         @NonNull List<T> data) {

        Constructor<? extends BaseQuickAdapter> constructor = loadAdapterConstructor(obj);
        Context ctx = recyclerView.getContext().getApplicationContext();
        BaseQuickAdapter<T, ? extends BaseViewHolder> baseQuickAdapter = null;

        try {
            baseQuickAdapter = constructor.newInstance(ctx, data, obj);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (baseQuickAdapter == null) {
            throw new IllegalArgumentException("create adapter error");
        }

        recyclerView.setAdapter(baseQuickAdapter);

        return baseQuickAdapter;
    }

    public static Constructor<? extends BaseQuickAdapter> loadAdapterConstructor(Object object) {
        Class<?> clazz = object.getClass();
        Constructor<? extends BaseQuickAdapter> constructor = BINDER.get(clazz);
        if (constructor != null) {
            return constructor;
        }

        String clsName = clazz.getName();
        try {
            Class<? extends BaseQuickAdapter> bindingClass = (Class<? extends BaseQuickAdapter>) Class.forName(clsName + "Adapter");
            constructor = bindingClass.getConstructor(Context.class, List.class, clazz);
            BINDER.put(clazz, constructor);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return constructor;
    }
}
