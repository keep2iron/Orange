package io.github.keep2iron.orange.annotations.intrnal;

import io.github.keep2iron.orange.annotations.extra.Refreshable;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 12:07
 */
public interface OnRefresh {

    /**
     * 对外暴露的onRefresh方法，内部实际上调用了holder对象的onRefresh
     */
    void onRefresh();

    /**
     * 为holder对象设置refreshable对象
     *
     * @param refreshable 提供刷新操作类
     */
    void setRefreshableWithHolder(Refreshable refreshable);

    void setRefreshListener(Object view,Object refreshListener);
}