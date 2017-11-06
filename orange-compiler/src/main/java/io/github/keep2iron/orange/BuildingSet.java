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

import io.github.keep2iron.orange.annotations.RecyclerHolder;

import static io.github.keep2iron.orange.util.ClassUtil.BASE_QUICK_ADAPTER;
import static io.github.keep2iron.orange.util.ClassUtil.BASE_VIEW_HOLDER;
import static io.github.keep2iron.orange.util.ClassUtil.CONTEXT_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.DATA_BINDING_UTILS_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.LIST_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.VIEW_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.VIEW_DATA_BINDING_CLASS;
import static io.github.keep2iron.orange.util.ClassUtil.VIEW_GROUP_CLASS;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/05 11:17
 */
public class BuildingSet {

    private TypeName mGenericType;
    private TypeSpec.Builder mClassBuilder;

    private String mPackageName;

    boolean mIsUseDataBinding;
    ClassName mDataBindingViewHolderClass;

    public BuildingSet(Element recyclerHolderType) {
        RecyclerHolder recyclerHolder = recyclerHolderType.getAnnotation(RecyclerHolder.class);
        mPackageName = ClassName.get((TypeElement) recyclerHolderType).packageName();

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

        if (mIsUseDataBinding) {
            mDataBindingViewHolderClass = ClassName.get(mPackageName, recyclerHolderType.getSimpleName() + "Adapter." +
                    recyclerHolderType.getSimpleName() + "Holder");
        }

        if (itemResIds.length > 1) {
            //extends from BaseMultiItemQuickAdapter
        } else if (itemResIds.length == 1) {
            //extends from BaseQuickAdapter
            mClassBuilder = TypeSpec.classBuilder(recyclerHolderType.getSimpleName() + "Adapter")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(BASE_QUICK_ADAPTER, mGenericType, mIsUseDataBinding ? mDataBindingViewHolderClass : BASE_VIEW_HOLDER));

            createField(recyclerHolderType, mGenericType);
            createConstructor(recyclerHolderType, mGenericType, itemResIds[0], headerResId);
        } else {
            throw new IllegalArgumentException("your must define least 1 items id in @RecyclerHolder(items={})");
        }

        if(mIsUseDataBinding){
            createDataBindingMethod(recyclerHolderType);
        }
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
                .addStatement("view.setTag(R.id.BaseQuickAdapter_databinding_support, binding)")
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
                        .addStatement("return ($T) itemView.getTag(R.id.BaseQuickAdapter_databinding_support)", VIEW_DATA_BINDING_CLASS)
                        .build());

        TypeSpec innerClass = innerViewHolderClass.build();
        mClassBuilder.addType(innerClass);
        mClassBuilder.addMethod(gerItemViewMethod.build());
    }

    /**
     * create field in the generate java file
     *
     * @param recyclerHolderType
     * @param mGenericType
     */
    private void createField(Element recyclerHolderType, TypeName mGenericType) {
        TypeElement classElement = (TypeElement) recyclerHolderType;

        mClassBuilder.addField(FieldSpec.builder(ClassName.get(classElement), "mRecyclerHolder")
                .addModifiers(Modifier.PRIVATE)
                .build());
    }

    private void createConstructor(Element recyclerHolderType, TypeName genericType, int itemResId, int headerResId) {
        //generate constructor
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CONTEXT_CLASS, "context")
                .addParameter(ParameterizedTypeName.get(LIST_CLASS, genericType), "data")
                .addParameter(TypeName.get(recyclerHolderType.asType()), "recyclerHolder")
                .addStatement("super($L,data)", itemResId)
                .addStatement("this.mRecyclerHolder = recyclerHolder");

        if (headerResId != -1) {
            constructorBuilder.addStatement("$T headerView = $T.inflate(context,$L,null)", VIEW_CLASS, VIEW_CLASS, headerResId)
                    .addStatement("addHeaderView(headerView)");
        }

        mClassBuilder.addMethod(constructorBuilder.build());
    }

    /**
     * in generate code bind convert method.
     *
     * @param convertMethod that be @BindConvert Method Element object
     */
    public void bindConvert(Element convertMethod) {
        TypeName genericType = mGenericType;
        MethodSpec.Builder convertBuilder;
        if (!mIsUseDataBinding) {
            convertBuilder = MethodSpec.methodBuilder("convert")
                    .returns(void.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(BASE_VIEW_HOLDER, "helper")
                    .addParameter(genericType, "item")
                    .addStatement("mRecyclerHolder.$N(helper,item)", convertMethod.getSimpleName().toString());
        } else {
            convertBuilder = MethodSpec.methodBuilder("convert")
                    .returns(void.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(mDataBindingViewHolderClass, "helper")
                    .addParameter(genericType, "item")
                    .addStatement("ViewDataBinding binding = helper.getBinding()")
                    .addStatement("mRecyclerHolder.$N(binding,item)", convertMethod.getSimpleName().toString());
        }

        mClassBuilder.addMethod(convertBuilder.build());
    }

    /**
     * generate file
     */
    public void build(Filer filer) {
        try {
            JavaFile.builder(mPackageName, mClassBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}