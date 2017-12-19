package io.github.keep2iron.sample;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;


import io.github.keep2iron.orange.Orange;
import io.github.keep2iron.orange.OrangeOptions;
import io.github.keep2iron.orange.annotations.bind.Bind;
import io.github.keep2iron.orange.annotations.bind.BindConvert;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.sample.databinding.ItemDefaultBinding;
import io.github.keep2iron.sample.databinding.RecyclerViewBinding;

/**
 * @author keep2iron
 */
@RecyclerHolder(type = String.class,
        items = {R.layout.item_default},
        module = RecyclerModule.class,
        isUseDataBinding = true
)
public class MainActivity extends AppCompatActivity {

    @Bind
    RecyclerView.Adapter<? extends BaseViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerViewBinding binding = DataBindingUtil.setContentView(this, R.layout.recycler_view);

        final RecyclerModule recyclerModule = new RecyclerModule();
        binding.setViewModule(recyclerModule);

        OrangeOptions<String> options = new OrangeOptions<>(recyclerModule, getApplicationContext());
        options.buildRecyclerViewAdapter(binding.recyclerView, recyclerModule.mData);
        options.buildRefreshAdapter(new SwipeRefreshProvider());
        options.buildPageItem(15);

        Orange.inject(options, this);

        recyclerModule.mRefreshable.refresh();
        recyclerModule.onRefresh();
    }

    @BindConvert
    public void render(ViewDataBinding binding, String item, int layoutPosition) {
        ItemDefaultBinding itemBinding = (ItemDefaultBinding) binding;
        itemBinding.itemText.setText(item);
    }
}
