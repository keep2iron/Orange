package io.github.keep2iron.finishadpter;

import android.databinding.ObservableArrayList;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import io.github.keep2iron.finishadpter.databinding.ItemDefaultBinding;
import io.github.keep2iron.orange.annotations.BindConvert;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.api.Orange;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 10:53
 */
@RecyclerHolder(type = String.class, items = {R.layout.item_default}, isUseDataBinding = true)
public class RecyclerModule {

    public ObservableArrayList<String> mData;

    public RecyclerModule(RecyclerView recyclerView) {
        mData = new ObservableArrayList<>();
        for (int i = 0; i < 10; i++) {
            mData.add(Math.random() * 100 + "");
        }
        Orange.bind(this, recyclerView, mData);
    }

    @BindConvert
    public void render(ViewDataBinding binding, String item) {
        ItemDefaultBinding itemBinding = (ItemDefaultBinding) binding;
        itemBinding.itemText.setText(item);
    }
}
