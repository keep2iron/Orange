package io.github.keep2iron.finishadpter;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.github.keep2iron.finishadpter.databinding.ActivityMainBinding;

/**
 * @author keep2iron
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        RecyclerModule recyclerModule = new RecyclerModule(binding.recyclerView);
        binding.setViewModule(recyclerModule);
    }
}
