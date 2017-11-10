package io.github.keep2iron.orange.annotations;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/10 12:00
 */
public interface Refreshable {

    void refreshEnable(boolean isEnable);

    void refresh();

    void showRefreshComplete();
}