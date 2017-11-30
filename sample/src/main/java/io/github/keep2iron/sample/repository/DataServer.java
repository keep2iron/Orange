package io.github.keep2iron.sample.repository;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 15:41
 */
public class DataServer {

    static Handler handler = new Handler();

    public interface Callback<T> {
        void onSuccess(List<T> list);

        void onError();
    }

    public static void httpData(final Callback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> data = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    data.add(Math.random() * 100 + "");
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(data);
                    }
                });
            }
        }).start();

    }
}