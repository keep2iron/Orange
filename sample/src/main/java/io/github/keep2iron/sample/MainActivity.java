package io.github.keep2iron.sample;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import javax.inject.Inject;

import io.github.keep2iron.orange.Orange;
import io.github.keep2iron.orange.OrangeOptions;
import io.github.keep2iron.orange.annotations.BindConvert;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.sample.databinding.ActivityMainBinding;
import io.github.keep2iron.sample.databinding.ItemDefaultBinding;

/**
 * @author keep2iron
 */
@RecyclerHolder(type = String.class,
        items = {R.layout.item_default},
        module = RecyclerModule.class,
        isUseDataBinding = true
)
public class MainActivity extends AppCompatActivity {

    @Inject
    BaseQuickAdapter<String, ? extends BaseViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        RecyclerModule recyclerModule = new RecyclerModule();
        binding.setViewModule(recyclerModule);

        OrangeOptions<String> options = new OrangeOptions<>(recyclerModule, getApplicationContext());
        options.buildVertical();
        options.buildRecyclerViewAdapter(binding.recyclerView, recyclerModule.mData);
        options.buildRefreshAdapter(new SwipeRefreshProvider());
        options.buildPageItem(15);

        Orange.inject(options, this);
    }

    @BindConvert
    public void render(ViewDataBinding binding, String item, int layoutPosition) {
        ItemDefaultBinding itemBinding = (ItemDefaultBinding) binding;
        itemBinding.itemText.setText(item);
    }
}
