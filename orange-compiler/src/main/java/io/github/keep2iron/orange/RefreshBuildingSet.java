package io.github.keep2iron.orange;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import io.github.keep2iron.orange.annotations.BindOnRefresh;
import io.github.keep2iron.orange.annotations.LoadMoreAble;
import io.github.keep2iron.orange.annotations.OnRefresh;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.annotations.Refreshable;
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
    MethodSpec.Builder mConstructorBuilder;

    private final String mPackageName;

    public RefreshBuildingSet(Element recyclerHolderType) {
        mPackageName = ClassName.get((TypeElement) recyclerHolderType).packageName();

        mClassBuilder = TypeSpec.classBuilder(recyclerHolderType.getSimpleName() + "RefreshAdapter")
                .addSuperinterface(OnRefresh.class)
                .addModifiers(Modifier.PUBLIC);

        TypeElement classElement = (TypeElement) recyclerHolderType;
        mClassBuilder.addField(FieldSpec.builder(ClassName.get(classElement), "mRecyclerHolder")
                .addModifiers(Modifier.PRIVATE)
                .build());

        mConstructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(recyclerHolderType.asType()), "recyclerHolder")
                .addStatement("this.mRecyclerHolder = recyclerHolder");
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

        mConstructorBuilder.addParameter(ClassUtil.VIEW_CLASS, "view")
                .addParameter(Object.class, "listener")
                .addStatement("$T refreshLayout = ($T)view", refreshLayoutClass, refreshLayoutClass)
                .addStatement("refreshLayout.$N(($T)listener)", methodName, listenerClass);


        mClassBuilder.addMethod(MethodSpec.methodBuilder("onRefresh")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addStatement("mRecyclerHolder.$N()", onRefreshMethod.getSimpleName().toString()).build());
    }

    public void bindInject(Element injectFiled) {
        if (injectFiled.getModifiers().contains(Modifier.PRIVATE)) {
            throw new IllegalArgumentException(injectFiled.getSimpleName() + "can't set private,please use default or public");
        }

        mClassBuilder.addMethod(MethodSpec.methodBuilder("setRefreshableWithHolder")
                .addParameter(Refreshable.class, "refreshAble")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mRecyclerHolder.$N = refreshAble", injectFiled.getSimpleName().toString())
                .returns(void.class).build());
    }

    public void build(Filer filer) {
        mClassBuilder.addMethod(mConstructorBuilder.build());

        try {
            JavaFile.builder(mPackageName, mClassBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}