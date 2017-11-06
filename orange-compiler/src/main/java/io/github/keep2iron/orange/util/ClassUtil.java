package io.github.keep2iron.orange.util;

import com.squareup.javapoet.ClassName;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 10:44
 */
public class ClassUtil {

    public final static ClassName LIST_CLASS = ClassName.get("java.util", "List");
    public final static ClassName CONTEXT_CLASS = ClassName.get("android.content", "Context");
    public final static ClassName VIEW_CLASS = ClassName.get("android.view", "View");
    public final static ClassName VIEW_GROUP_CLASS = ClassName.get("android.view","ViewGroup");

    public final static ClassName DATA_BINDING_UTILS_CLASS = ClassName.get("android.databinding","DataBindingUtil");
    public final static ClassName VIEW_DATA_BINDING_CLASS = ClassName.get("android.databinding","ViewDataBinding");

    public final static ClassName BASE_QUICK_ADAPTER = ClassName.get("com.chad.library.adapter.base", "BaseQuickAdapter");
    public final static ClassName BASE_VIEW_HOLDER = ClassName.get("com.chad.library.adapter.base", "BaseViewHolder");
}