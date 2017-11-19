package io.github.keep2iron.orange;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import io.github.keep2iron.orange.annotations.bind.BindOnRefresh;
import io.github.keep2iron.orange.annotations.intrnal.OnRefresh;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.annotations.extra.Refreshable;
import io.github.keep2iron.orange.util.ClassUtil;
import io.github.keep2iron.orange.util.Constants;
import io.github.keep2iron.orange.util.Util;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/09 18:22
 */
public class RefreshBuildingSet {

    private final TypeSpec.Builder mClassBuilder;

    private final String mPackageName;
    private TypeName mModuleLayerType = null;

    private HashSet<String> viewLayerClassName = new HashSet<>();


    public RefreshBuildingSet(Element recyclerHolderType) {
        RecyclerHolder recyclerHolder = recyclerHolderType.getAnnotation(RecyclerHolder.class);
        try {
            recyclerHolder.module();
        }catch (MirroredTypeException exception) {
            TypeMirror locatorType = exception.getTypeMirror();
            mModuleLayerType = ClassName.get(locatorType);
            if ("void".equals(mModuleLayerType.toString())) {
                mModuleLayerType = ClassName.get(recyclerHolderType.asType());
            }
        }
        String moduleClassName = ClassUtil.toSimpleName(mModuleLayerType);
        mPackageName = ClassName.get((TypeElement) recyclerHolderType).packageName();

        mClassBuilder = TypeSpec.classBuilder(moduleClassName + "RefreshAdapter")
                .addModifiers(Modifier.PUBLIC);

        mClassBuilder.addField(FieldSpec.builder(mModuleLayerType, "mModuleLayer")
                .addModifiers(Modifier.PRIVATE)
                .build());
    }

    public void buildConstructor(Element recyclerHolderType){
        TypeElement classElement = (TypeElement) recyclerHolderType;
        String viewFiledName = ClassUtil.className2FieldName(classElement.getSimpleName().toString());
        viewLayerClassName.add(classElement.getSimpleName().toString());

        mClassBuilder.addField(FieldSpec.builder(ClassName.get(classElement), viewFiledName)
                .addModifiers(Modifier.PRIVATE)
                .build());
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(recyclerHolderType.asType()), "viewLayer")
                .addParameter(mModuleLayerType, "module")
                .addStatement("this.$N = viewLayer", viewFiledName)
                .addStatement("this.mModuleLayer = module");

        mClassBuilder.addMethod(constructorBuilder.build());
    }

    /**
     * build refreshAdapter with compiler options
     * compilerOptions[0]   RefreshClassName                刷新的class的名字
     * compilerOptions[1]   RefreshListenerClassName        刷新监听器的class的名字
     * compilerOptions[2]   setRefreshListenerMethodName    设置刷新监听器的方法名
     *
     * @param compilerOptions 通过设置编译时的options设置上述参数
     */
    private Map<String, String> bindOnRefreshWithCompilerOptions(String... compilerOptions) {
        if (compilerOptions.length != Constants.COMPILER_OPTIONS_SIZE) {
            throw new IllegalArgumentException("compileOptions size have't" + Constants.COMPILER_OPTIONS_SIZE + "please set " +
                    "javaCompileOptions {" +
                    "annotationProcessorOptions {" +
                    "arguments = " +
                    "[" + Constants.KEY_REFRESH_CLASS + " : ]" +
                    "[" + Constants.KEY_REFRESH_LISTENER_CLASS + " : ]" +
                    "[" + Constants.KEY_SET_REFRESH_LISTENER_METHOD + " : ]" +
                    "}" +
                    "}" +
                    "in your .gradle file with in defaultConfig { javaCompileOptions { ...... }}");
        }

        HashMap<String, String> compilerOptionMap = new HashMap<>(20);
        for (int i = 0; i < compilerOptions.length; i++) {
            if (Util.isEmpty(compilerOptions[i])) {
                return null;
            }
        }

        compilerOptionMap.put(Constants.KEY_REFRESH_CLASS, compilerOptions[0]);
        compilerOptionMap.put(Constants.KEY_REFRESH_LISTENER_CLASS, compilerOptions[1]);
        compilerOptionMap.put(Constants.KEY_SET_REFRESH_LISTENER_METHOD, compilerOptions[2]);

        return compilerOptionMap;
    }

    public void bindOnRefresh(Element onRefreshMethod, String... compilerOptions) {
        mClassBuilder.addSuperinterface(OnRefresh.class);

        BindOnRefresh bindOnRefresh = onRefreshMethod.getAnnotation(BindOnRefresh.class);

        TypeName listenerClass;
        try {
            //因为type值的获取是在runtime期间而非compile time。这里使用try catch获取TypeMirror
            listenerClass = ClassName.get(bindOnRefresh.listener());
        } catch (MirroredTypeException exception) {
            TypeMirror locatorType = exception.getTypeMirror();
            listenerClass = ClassName.get(locatorType);
        }


        TypeName refreshLayoutClass;
        try {
            //因为type值的获取是在runtime期间而非compile time。这里使用try catch获取TypeMirror
            refreshLayoutClass = ClassName.get(bindOnRefresh.refreshLayout());
        } catch (MirroredTypeException exception) {
            TypeMirror locatorType = exception.getTypeMirror();
            refreshLayoutClass = ClassName.get(locatorType);
        }

        String methodName = bindOnRefresh.setRefreshMethod();

        //如果设置的信息不完整，则从全局编译的options进行获取信息
        if (listenerClass.toString().contains(Constants.DEFAULT_CLASS) ||
                refreshLayoutClass.toString().contains(Constants.DEFAULT_CLASS) ||
                Util.isEmpty(methodName)) {
            Map<String, String> compilerOptionMap = null;
            if ((compilerOptionMap = bindOnRefreshWithCompilerOptions(compilerOptions)) == null) {
                throw new IllegalArgumentException("you have not set enough information in your annotation or .gradle file");
            }

            refreshLayoutClass = ClassUtil.toClassName(compilerOptionMap.get(Constants.KEY_REFRESH_CLASS));
            listenerClass = ClassUtil.toClassName(compilerOptionMap.get(Constants.KEY_REFRESH_LISTENER_CLASS));
            methodName = compilerOptionMap.get(Constants.KEY_SET_REFRESH_LISTENER_METHOD);
        }

        mClassBuilder.addMethod(MethodSpec.methodBuilder("setRefreshListener")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(Object.class, "view")
                .addParameter(Object.class, "listener")
                .addStatement("$T refreshLayout = ($T)view", refreshLayoutClass, refreshLayoutClass)
                .addStatement("refreshLayout.$N(($T)listener)", methodName, listenerClass).build());


        mClassBuilder.addMethod(MethodSpec.methodBuilder("onRefresh")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("mModuleLayer.$N()",
                        onRefreshMethod.getSimpleName().toString()).build());
    }

    public void bindInject(Element injectFiled) {
        if (injectFiled.getModifiers().contains(Modifier.PRIVATE)) {
            throw new IllegalArgumentException(injectFiled.getSimpleName() + "can't set private,please use default or public");
        }

        MethodSpec.Builder method = MethodSpec.methodBuilder("setRefreshableWithHolder")
                .addParameter(Refreshable.class, "refreshAble")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        String fullName = ClassUtil.getFullName(injectFiled.getEnclosingElement());
        if(fullName.equals(mModuleLayerType.toString())){
            method.addStatement("mModuleLayer.$N = refreshAble",
                    injectFiled.getSimpleName().toString());
        }else{
            for (String sampleName : viewLayerClassName) {
                if(fullName.equals(sampleName)){
                    String fieldName = ClassUtil.className2FieldName(sampleName);
                    method.addStatement("$N.$N = refreshAble",fieldName,injectFiled.getSimpleName().toString());
                }
            }
        }


        mClassBuilder.addMethod(method.build());
    }

    public void build(Filer filer) {
        try {
            JavaFile.builder(mPackageName, mClassBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}