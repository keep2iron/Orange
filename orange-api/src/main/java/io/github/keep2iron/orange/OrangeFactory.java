package io.github.keep2iron.orange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/12/15 16:14
 */
public class OrangeFactory {

    private OrangeFactory() {
    }

    public static <T> void buildVerticalList(Object viewLayer,
                                             Object moduleLayer,
                                             Context context,
                                             @NonNull RefreshViewAdapter<? extends ViewGroup> refreshProvider,
                                             RecyclerView recyclerView,
                                             int pageSize,
                                             List<T> data) {
        OrangeOptions<T> options = new OrangeOptions<>(moduleLayer, context.getApplicationContext());
        options.buildRecyclerViewAdapter(recyclerView, data);
        options.buildRefreshAdapter(refreshProvider);
        options.buildPageItem(pageSize);
        Orange.inject(options, viewLayer);
    }
}