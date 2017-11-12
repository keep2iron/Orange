package io.github.keep2iron.orange.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.github.keep2iron.orange.BRAVHBuildingSet;
import io.github.keep2iron.orange.annotations.BindConvert;
import io.github.keep2iron.orange.annotations.BindOnLoadMore;
import io.github.keep2iron.orange.annotations.LoadMoreAble;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.util.ClassUtil;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/04 21:46
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.github.keep2iron.orange.annotations.RecyclerHolder",
        "io.github.keep2iron.orange.annotations.BindConvert",
        "io.github.keep2iron.orange.annotations.BindOnLoadMore",
        "io.github.keep2iron.orange.annotations.BindOnRefresh",
        "javax.inject.Inject"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BaseRecyclerViewAdapterProcessor extends AbstractProcessor {

    /**
     * one @RecyclerHolder java file map into a BRAVHBuildingSet object to generate file
     */
    private ConcurrentHashMap<String, BRAVHBuildingSet> mBuildingMap;

    /**
     * access Element type util
     */
    private Elements mElementUtils;

    /**
     * access output file util
     */
    private Filer mFiler;

    /**
     *
     */
    Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        //init utils
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
        mBuildingMap = new ConcurrentHashMap<>(50);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return true;
        }

        //bind @RecyclerHolder class
        bindRecyclerHolder(roundEnvironment);

        //bind @BindConvert method
        bindConvert(roundEnvironment);

        //bind @BindOnLoadMore method
        bindLoadMore(roundEnvironment);

        //bind @Inject element
        bindInjectField(roundEnvironment);

        for (Map.Entry<String, BRAVHBuildingSet> entry : mBuildingMap.entrySet()) {
            entry.getValue().build(mFiler);
        }

        return false;
    }

    private void bindInjectField(RoundEnvironment roundEnvironment) {
        Set<? extends Element> injectElements = roundEnvironment.getElementsAnnotatedWith(Inject.class);
        if (injectElements != null && injectElements.size() != 0) {
            for (Element ele : injectElements) {
                TypeElement classFile = (TypeElement) ele.getEnclosingElement();
                BRAVHBuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());
                if (buildingSet != null) {
                    if (ele.asType().toString().contains(ClassUtil.BASE_QUICK_ADAPTER.toString())) {
                        buildingSet.bindInjectAdapter(ele);
                    } else if (ele.asType().toString().equals(LoadMoreAble.class.getName())) {
                        buildingSet.bindInjectLoadMore(ele);
                    }
                }
            }
        }
    }

    private void bindLoadMore(RoundEnvironment roundEnvironment) {
        Set<? extends Element> loadElements = roundEnvironment.getElementsAnnotatedWith(BindOnLoadMore.class);
        if (loadElements != null && loadElements.size() != 0) {
            for (Element ele : loadElements) {
                TypeElement classFile = (TypeElement) ele.getEnclosingElement();
                BRAVHBuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());
                if (buildingSet != null) {
                    //if have @BindOnLoadMore
                    buildingSet.bindLoadMore(ele);
                }
            }
        }
    }

    private void bindConvert(RoundEnvironment roundEnvironment) {
        Set<? extends Element> convertElements = roundEnvironment.getElementsAnnotatedWith(BindConvert.class);
        if (convertElements == null || convertElements.size() == 0) {
            throw new IllegalArgumentException("your must define @BindConvert in your java file");
        }
        for (Element ele : convertElements) {
            TypeElement classFile = (TypeElement) ele.getEnclosingElement();
            BRAVHBuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());
            if (buildingSet == null) {
                throw new IllegalArgumentException("@RecyclerHolder don't include @BindConvert element,please add @BindConvert on a method");
            }
            buildingSet.bindConvert(ele);
        }
    }

    private void bindRecyclerHolder(RoundEnvironment roundEnvironment) {
        Set<? extends Element> recyclerHolderElements = roundEnvironment.getElementsAnnotatedWith(RecyclerHolder.class);
        for (Element ele : recyclerHolderElements) {
            TypeElement classFile = (TypeElement) ele;
            BRAVHBuildingSet buildingSet = new BRAVHBuildingSet(ele);

            RecyclerHolder recyclerHolder = ele.getAnnotation(RecyclerHolder.class);
            TypeName moduleType = null;
            try {
                ClassName.get(recyclerHolder.module());
            }catch (MirroredTypeException exception){
                TypeMirror locatorType = exception.getTypeMirror();
                moduleType = ClassName.get(locatorType);
            }

            if(!"void".equals(moduleType.toString())){
                mBuildingMap.put(moduleType.toString(),buildingSet);
            }

            mBuildingMap.put(classFile.getQualifiedName().toString(), buildingSet);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> types = new HashSet<>();
        types.add("io.github.keep2iron.orange.annotations.RecyclerHolder");
        types.add("io.github.keep2iron.orange.annotations.BindConvert");
        types.add("io.github.keep2iron.orange.annotations.BindOnLoadMore");
        types.add("io.github.keep2iron.orange.annotations.BindOnRefresh");
        types.add("javax.inject.Inject");
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
