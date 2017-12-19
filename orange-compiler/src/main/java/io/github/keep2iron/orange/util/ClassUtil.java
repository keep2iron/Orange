package io.github.keep2iron.orange.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 10:44
 */
public class ClassUtil {

    private static Elements mElementUtils;
    private static Types mTypeUtils;

    public static void init(Elements elementUtils, Types typeUtils) {
        mElementUtils = elementUtils;
        mTypeUtils = typeUtils;
    }

    public final static ClassName LIST_CLASS = ClassName.get("java.util", "List");
    public final static ClassName CONTEXT_CLASS = ClassName.get("android.content", "Context");
    public final static ClassName VIEW_CLASS = ClassName.get("android.view", "View");
    public final static ClassName VIEW_GROUP_CLASS = ClassName.get("android.view", "ViewGroup");
    public final static ClassName RECYCLER_VIEW_CLASS = ClassName.get("android.support.v7.widget", "RecyclerView");
    public final static ClassName ITEM_TOUCH_HELPER = ClassName.get("android.support.v7.widget.helper","ItemTouchHelper");
    public final static ClassName RECYCLER_ADAPTER = ClassName.get("android.support.v7.widget", "RecyclerView.Adapter");

    public final static ClassName DATA_BINDING_UTILS_CLASS = ClassName.get("android.databinding", "DataBindingUtil");
    public final static ClassName VIEW_DATA_BINDING_CLASS = ClassName.get("android.databinding", "ViewDataBinding");

    public final static ClassName BASE_QUICK_ADAPTER = ClassName.get("com.chad.library.adapter.base", "BaseQuickAdapter");
    public final static ClassName BASE_DRAG_ADAPTER = ClassName.get("com.chad.library.adapter.base", "BaseItemDraggableAdapter");
    public final static ClassName BASE_VIEW_HOLDER = ClassName.get("com.chad.library.adapter.base", "BaseViewHolder");
    public final static ClassName REQUEST_LOAD_MORE_LISTENER = ClassName.get("com.chad.library.adapter.base.BaseQuickAdapter", "RequestLoadMoreListener");
    public final static ClassName ITEM_DRAG_AND_SWIPE_CALLBACK = ClassName.get("com.chad.library.adapter.base.callback", "ItemDragAndSwipeCallback");
    public final static ClassName ON_ITEM_SWIPE_LISTENER = ClassName.get("com.chad.library.adapter.base.listener", "OnItemSwipeListener");
    public final static ClassName ON_ITEM_DRAG_LISTENER = ClassName.get("com.chad.library.adapter.base.listener", "OnItemDragListener");


    public static ClassName toClassName(String className) {
        String[] result = new String[2];

        String[] names = className.split("\\.");
        result[0] = className.substring(0, className.lastIndexOf("."));
        result[1] = names[names.length - 1];

        return ClassName.get(result[0],result[1]);
    }

    public static String toSimpleName(ClassName className){
        String[] strings = className.toString().split("\\.");
        return strings[strings.length - 1];
    }

    public static String toSimpleName(TypeName className){
        String[] strings = className.toString().split("\\.");
        return strings[strings.length - 1];
    }

    public static String className2FieldName(String className) {
        if(className.contains(".")){
            return className.toLowerCase().substring(0,1).concat(
                    className.substring(1));
        }else{
            return className.replace("\\.","")
                    .toLowerCase().substring(0,1).concat(className.substring(1));
        }
    }

    public static String getFullName(Element ele){
        return mElementUtils.getPackageOf(ele).getQualifiedName().toString() + "." + ele.getSimpleName();
    }
}