package io.github.keep2iron.sample;

import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;

import java.util.Collection;
import java.util.List;

import io.github.keep2iron.orange.WrapContentLinearLayoutManager;

/**
 * Created by keep-iron on 17-11-18.
 */

public class BindRecyclerView {

    @BindingAdapter(value = {"adapter"})
    public static void bindRecyclerView(RecyclerView recyclerView,final RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter){
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
    }

}
