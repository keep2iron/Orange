package io.github.keep2iron.sample;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import io.github.keep2iron.orange.Orange;
import io.github.keep2iron.orange.OrangeOptions;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.annotations.SwipeAble;
import io.github.keep2iron.orange.annotations.bind.BindConvert;
import io.github.keep2iron.sample.databinding.ItemDragBinding;
import io.github.keep2iron.sample.databinding.RecyclerViewBinding;


//@DragAble
@SwipeAble
@RecyclerHolder(type = String.class,
        items = {R.layout.item_drag},
        module = RecyclerModule.class,
        isUseDataBinding = true
)
public class DragAndSwipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerViewBinding binding = DataBindingUtil.setContentView(this, R.layout.recycler_view);

        RecyclerModule recyclerModule = new RecyclerModule();
        binding.setViewModule(recyclerModule);

        OrangeOptions<String> options = new OrangeOptions<>(recyclerModule, getApplicationContext());
        options.buildRecyclerViewAdapter(binding.recyclerView, recyclerModule.mData);
        options.buildRefreshAdapter(new SwipeRefreshProvider());
        options.buildPageItem(15);

        Orange.inject(options, this);
    }

    @BindConvert
    public void render(ViewDataBinding binding, String item, int layoutPosition) {
        ItemDragBinding itemBinding = (ItemDragBinding) binding;
        switch (layoutPosition %
                3) {
            case 0:
                itemBinding.ivHead.setImageResource(R.mipmap.head_img0);
                break;
            case 1:
                itemBinding.ivHead.setImageResource(R.mipmap.head_img1);
                break;
            case 2:
                itemBinding.ivHead.setImageResource(R.mipmap.head_img2);
                break;
        }
        itemBinding.tv.setText(item);
    }
}
