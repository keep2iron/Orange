package io.github.keep2iron.orange;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import io.github.keep2iron.orange.annotations.DragAble;
import io.github.keep2iron.orange.annotations.SwipeAble;
import io.github.keep2iron.orange.annotations.extra.LoadMoreAble;
import io.github.keep2iron.orange.annotations.extra.SlideDirection;
import io.github.keep2iron.orange.annotations.intrnal.IModuleClass;
import io.github.keep2iron.orange.annotations.intrnal.OnLoadMore;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.annotations.intrnal.OnSwipeOrDrag;
import io.github.keep2iron.orange.util.ClassUtil;

import static io.github.keep2iron.orange.util.ClassUtil.BASE_DRAG_ADAPTER;
import static io.github.keep2iron.orange.util.ClassUtil.BASE_QUICK_ADAPTER;
import static io.github.keep2iron.orange.util.ClassUtil.BASE_VIEW_HOLDER;
import static io.github.keep2iron.orange.util.ClassUtil.CONTEXT_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.DATA_BINDING_UTILS_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.ITEM_DRAG_AND_SWIPE_CALLBACK;
import static io.github.keep2iron.orange.util.ClassUtil.ITEM_TOUCH_HELPER;
import static io.github.keep2iron.orange.util.ClassUtil.LIST_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.ON_ITEM_SWIPE_LISTENER;
import static io.github.keep2iron.orange.util.ClassUtil.VIEW_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.VIEW_DATA_BINDING_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.VIEW_GROUP_CLASS;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 11:17
 */
public class BRAVHBuildingSet {
    /**
     * adapter 泛型参数
     */
    private TypeName mGenericType;
    /**
     * adapter 类的构造器
     */
    private TypeSpec.Builder mClassBuilder;
    /**
     * adapter 构造方法的构造器
     */
    private MethodSpec.Builder mConstructorBuilder;
    /**
     * OnLoadMore中的 setLoadMoreHolder的重写方法
     */
    private MethodSpec.Builder mSetLoadMoreHolderMethod;


    private String mPackageName;
    private int mItemResId;

    boolean mIsUseDataBinding;
    ClassName mDataBindingViewHolderClass;

    TypeName mRecyclerHolderClass;
    TypeName mRecyclerHolderModuleClass;

    public TypeName getRecyclerHolderModuleClass() {
        return mRecyclerHolderModuleClass;
    }

    public BRAVHBuildingSet(Element recyclerHolderType) {
        //get @RecyclerHolder arguments
        RecyclerHolder recyclerHolder = recyclerHolderType.getAnnotation(RecyclerHolder.class);
        DragAble dragAble = recyclerHolderType.getAnnotation(DragAble.class);
        SwipeAble swipeAble = recyclerHolderType.getAnnotation(SwipeAble.class);

        mRecyclerHolderClass = ClassName.get(recyclerHolderType.asType());


        mPackageName = ClassName.get((TypeElement) recyclerHolderType).packageName();
        //moduleType  生成的LoadMoreAble对象和Refreshable对象注入到对象中module
        TypeName moduleType = null;
        int[] itemResIds = recyclerHolder.items();
        int headerResId = recyclerHolder.header();
        mIsUseDataBinding = recyclerHolder.isUseDataBinding();

        try {
            //因为type值的获取是在runtime期间而非compile time。这里使用try catch获取TypeMirror
            mGenericType = ClassName.get(recyclerHolder.type());
        } catch (MirroredTypeException exception) {
            TypeMirror locatorType = exception.getTypeMirror();
            mGenericType = ClassName.get(locatorType);
        }

        try {
            recyclerHolder.module();
        } catch (MirroredTypeException exception) {
            TypeMirror locatorType = exception.getTypeMirror();
            moduleType = ClassName.get(locatorType);
            mRecyclerHolderModuleClass = moduleType;
        }

        if (mIsUseDataBinding) {
            mDataBindingViewHolderClass = ClassName.get(mPackageName, recyclerHolderType.getSimpleName() + "Adapter." +
                    recyclerHolderType.getSimpleName() + "Holder");
        }

        if (itemResIds.length > 1) {
        } else if (itemResIds.length == 1) {
            boolean isEnableSwipeOrSlide = dragAble != null || swipeAble != null;
            ClassName superClass = isEnableSwipeOrSlide ? BASE_DRAG_ADAPTER : BASE_QUICK_ADAPTER;


            //extends from BaseQuickAdapter
            mClassBuilder = TypeSpec.classBuilder(recyclerHolderType.getSimpleName() + "Adapter")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(superClass, mGenericType, mIsUseDataBinding ? mDataBindingViewHolderClass : BASE_VIEW_HOLDER));

            createField(recyclerHolderType, "mViewLayer");
            createField(moduleType, "mModuleLayer");
            createConstructor(recyclerHolderType, moduleType, mGenericType, itemResIds[0], headerResId);
            openSwipeOrSlide(dragAble,swipeAble);
        } else {
            throw new IllegalArgumentException("your must define least 1 items id in @RecyclerHolder(items={})");
        }


        if (mIsUseDataBinding) {
            createDataBindingMethod(recyclerHolderType);
        }
    }

    private void openSwipeOrSlide(DragAble dragAble, SwipeAble swipeAble) {
        if(dragAble == null && swipeAble == null) return;

        mClassBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(OnSwipeOrDrag.class),ClassUtil.RECYCLER_VIEW_CLASS));

        MethodSpec.Builder attachMethod = MethodSpec.methodBuilder("attachRecyclerView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassUtil.RECYCLER_VIEW_CLASS, "recyclerView")
                .addParameter(Object.class,"onDragListener")
                .addParameter(Object.class,"onSwipeListener")
                .returns(void.class);

        attachMethod.addStatement("$T itemDragAndSwipeCallback = new $T(this)",ITEM_DRAG_AND_SWIPE_CALLBACK,ITEM_DRAG_AND_SWIPE_CALLBACK);
        attachMethod.addStatement("$T itemTouchHelper = new $T(itemDragAndSwipeCallback)",ITEM_TOUCH_HELPER,ITEM_TOUCH_HELPER);
        attachMethod.addStatement("itemTouchHelper.attachToRecyclerView(recyclerView)");

        if(swipeAble != null){
            int flag = swipeAble.flag();
            if(flag == -1){
                flag = SlideDirection.START;
            }

            attachMethod.addStatement("itemDragAndSwipeCallback.setSwipeMoveFlags($L)",flag);
            attachMethod.addStatement("enableSwipeItem()");
            attachMethod.beginControlFlow("if(onSwipeListener != null)");
            attachMethod.addStatement("setOnItemSwipeListener(($T)onSwipeListener)",ON_ITEM_SWIPE_LISTENER);
            attachMethod.endControlFlow();
        }

        if(dragAble != null){
            int flag = dragAble.flag();
            if(flag == -1){
                flag = SlideDirection.LEFT | SlideDirection.RIGHT | SlideDirection.UP | SlideDirection.DOWN;
            }
            attachMethod.addStatement("itemDragAndSwipeCallback.setDragMoveFlags($L)",flag);
            attachMethod.addStatement("enableDragItem(itemTouchHelper)");
            attachMethod.beginControlFlow("if(onDragListener != null)");
            attachMethod.addStatement("setOnItemDragListener(($T)onDragListener)",ClassUtil.ON_ITEM_DRAG_LISTENER);
            attachMethod.endControlFlow();
        }

        mClassBuilder.addMethod(attachMethod.build());
    }

    private void createDataBindingMethod(Element recyclerHolderType) {

        MethodSpec.Builder gerItemViewMethod = MethodSpec.methodBuilder("getItemView")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(VIEW_CLASS)
                .addParameter(int.class, "layoutResId")
                .addParameter(VIEW_GROUP_CLASS, "parent")
                .addStatement("$T binding = $T.inflate(mLayoutInflater, layoutResId, parent, false);", VIEW_DATA_BINDING_CLASS, DATA_BINDING_UTILS_CLASS)
                .addStatement("if(binding == null){return super.getItemView(layoutResId, parent);}")
                .addStatement("$T view = binding.getRoot()", VIEW_CLASS)
                .addStatement("view.setTag($L, binding)", mItemResId)
                .addStatement("return view");

        TypeSpec.Builder innerViewHolderClass = TypeSpec.classBuilder(recyclerHolderType.getSimpleName() + "Holder")
                .superclass(BASE_VIEW_HOLDER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(VIEW_CLASS, "view")
                        .addStatement("super(view)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getBinding")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(VIEW_DATA_BINDING_CLASS)
                        .addStatement("return ($T) itemView.getTag($L)", VIEW_DATA_BINDING_CLASS, mItemResId)
                        .build());

        TypeSpec innerClass = innerViewHolderClass.build();
        mClassBuilder.addType(innerClass);
        mClassBuilder.addMethod(gerItemViewMethod.build());
    }

    /**
     * create field in the generate java file
     *
     * @param element   注入的Element的类型
     * @param filedName 注入fileldName
     */
    private void createField(Element element, String filedName) {
        TypeElement classElement = (TypeElement) element;

        mClassBuilder.addField(FieldSpec.builder(ClassName.get(classElement), filedName)
                .addModifiers(Modifier.PRIVATE)
                .build());
    }

    private void createField(TypeName typeName, String filedName) {
        mClassBuilder.addField(FieldSpec.builder(typeName, filedName)
                .addModifiers(Modifier.PRIVATE)
                .build());
    }

    private void createConstructor(Element recyclerHolderType, TypeName moduleType, TypeName genericType, int itemResId, int headerResId) {
        mItemResId = itemResId;

        //generate constructor
        mConstructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CONTEXT_CLASS, "context")
                .addParameter(ParameterizedTypeName.get(LIST_CLASS, genericType), "data")
                .addParameter(TypeName.get(recyclerHolderType.asType()), "viewLayer")
                .addParameter(moduleType, "moduleLayer")
                .addStatement("super($L,data)", itemResId)
                .addStatement("this.mViewLayer = viewLayer")
                .addStatement("this.mModuleLayer = moduleLayer");

        if (headerResId != -1) {
            mConstructorBuilder.addStatement("$T headerView = $T.inflate(context,$L,null)", VIEW_CLASS, VIEW_CLASS, headerResId)
                    .addStatement("addHeaderView(headerView)");
        }

        mClassBuilder.addSuperinterface(IModuleClass.class);
        mClassBuilder.addMethod(MethodSpec.methodBuilder("getRefreshModuleAdapterClassName")
                .returns(String.class)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $S",mPackageName + "." + ClassUtil.toSimpleName(moduleType) + "RefreshAdapter")
                .build());
    }

    /**
     * in generate code bind convert method.
     * convert method in v layer
     *
     * @param convertMethod that be @BindConvert Method Element object
     */
    public void bindConvert(Element convertMethod) {
        TypeName genericType = mGenericType;
        MethodSpec.Builder convertBuilder = MethodSpec.methodBuilder("convert")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class);

        if (!mIsUseDataBinding) {
            convertBuilder.addParameter(ClassUtil.BASE_VIEW_HOLDER, "helper")
                    .addStatement("mViewLayer.$N(helper,item,helper.getLayoutPosition())", convertMethod.getSimpleName().toString());
        } else {
            convertBuilder.addParameter(mDataBindingViewHolderClass, "helper")
                    .addStatement("ViewDataBinding binding = helper.getBinding()")
                    .addStatement("mViewLayer.$N(binding,item,helper.getLayoutPosition())", convertMethod.getSimpleName().toString());
        }

        mClassBuilder.addMethod(convertBuilder.addParameter(genericType, "item")
                .build());
    }

    /**
     * in generate code bind onLoadMoreRequest method
     * loadMoreRequest in M layer
     *
     * @param loadMoreMethod
     */
    public void bindLoadMore(Element loadMoreMethod) {
        mClassBuilder.addSuperinterface(OnLoadMore.class);

        mClassBuilder.addMethod(MethodSpec.methodBuilder("onLoadMore")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("mModuleLayer.$N()", loadMoreMethod.getSimpleName().toString()).build());

        mSetLoadMoreHolderMethod = MethodSpec.methodBuilder("setLoadMoreAbleWithHolder")
                .addParameter(LoadMoreAble.class, "loadMoreAble")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
    }

    public void bindInjectAdapter(Element injectFiled) {
        if (injectFiled.getModifiers().contains(Modifier.PRIVATE)) {
            throw new IllegalArgumentException(injectFiled.getSimpleName() + "can't set private,please use default or public");
        }
        Element element = injectFiled.getEnclosingElement();
        String fullName = ClassUtil.getFullName(element);

        if(fullName.equals(mRecyclerHolderClass.toString())){
            mConstructorBuilder.addStatement("mViewLayer.$N = this", injectFiled.getSimpleName().toString());
        }else if(fullName.equals(mRecyclerHolderModuleClass.toString())){
            mConstructorBuilder.addStatement("mModuleLayer.$N = this", injectFiled.getSimpleName().toString());
        }
    }

    public void bindInjectLoadMore(Element injectFiled) {
        if (injectFiled.getModifiers().contains(Modifier.PRIVATE)) {
            throw new IllegalArgumentException(injectFiled.getSimpleName() + "can't set private,please use default or public");
        }

        //如果没有继承OnLoadMore接口，那么不必要进行添加代码块了
        if(mSetLoadMoreHolderMethod != null) {
            mSetLoadMoreHolderMethod.addStatement("mModuleLayer.$N = loadMoreAble",
                    injectFiled.getSimpleName().toString());
        }
    }

    /**
     * generate file
     */
    public void build(Filer filer) {
        //当Module中没有BindLoadMore注解时，mSetLoadMoreHolderMethod为空
        if(mSetLoadMoreHolderMethod != null) {
            mClassBuilder.addMethod(mSetLoadMoreHolderMethod.build());
        }
        mClassBuilder.addMethod(mConstructorBuilder.build());

        try {
            JavaFile.builder(mPackageName, mClassBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}