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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.github.keep2iron.orange.BRAVHBuildingSet;
import io.github.keep2iron.orange.annotations.bind.Bind;
import io.github.keep2iron.orange.annotations.bind.BindConvert;
import io.github.keep2iron.orange.annotations.bind.BindOnLoadMore;
import io.github.keep2iron.orange.annotations.extra.LoadMoreAble;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.util.ClassUtil;
import io.github.keep2iron.orange.util.Logger;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/04 21:46
 */
@AutoService(Processor.class)
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

    Types mTypeUtils;

    private Logger mLogger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        //init utils
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
        mBuildingMap = new ConcurrentHashMap<>(50);

        // Package the log utils.
        mLogger = new Logger(processingEnv.getMessager());

        ClassUtil.init(mElementUtils, mTypeUtils);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return true;
        }

        //bind @RecyclerHolder
        //bind @SwipeAble
        //bind @DragAble
        bindClassAnnotation(roundEnvironment);

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
        Set<? extends Element> injectElements = roundEnvironment.getElementsAnnotatedWith(Bind.class);
        if (injectElements != null && injectElements.size() != 0) {
            for (Element ele : injectElements) {
                mLogger.info("bind      '" + ele.getSimpleName() + "'     inject");

                TypeElement classFile = (TypeElement) ele.getEnclosingElement();
                String fullName = classFile.getQualifiedName().toString();

                BRAVHBuildingSet buildingSet = mBuildingMap.get(fullName);
                if (buildingSet != null) {
                    if (ele.asType().toString().contains(ClassUtil.BASE_QUICK_ADAPTER.toString()) ||
                            ele.asType().toString().contains(ClassUtil.RECYCLER_ADAPTER.toString())) {
                        buildingSet.bindInjectAdapter(ele);
                    } else if (ele.asType().toString().equals(LoadMoreAble.class.getName())) {
                        buildingSet.bindInjectLoadMore(ele);
                    }
                }

                for (Map.Entry<String, BRAVHBuildingSet> entry : mBuildingMap.entrySet()) {
                    BRAVHBuildingSet bravhBuildingSet = entry.getValue();
                    String moduleClassName = bravhBuildingSet.getRecyclerHolderModuleClass().toString();
                    if (moduleClassName.equals(fullName)) {
                        if (ele.asType().toString().contains(ClassUtil.BASE_QUICK_ADAPTER.toString())) {
                            bravhBuildingSet.bindInjectAdapter(ele);
                        } else if (ele.asType().toString().equals(LoadMoreAble.class.getName())) {
                            bravhBuildingSet.bindInjectLoadMore(ele);
                        }
                    }
                }
            }
        }
    }

    private void bindLoadMore(RoundEnvironment roundEnvironment) {
        Set<? extends Element> loadElements = roundEnvironment.getElementsAnnotatedWith(BindOnLoadMore.class);
        if (loadElements != null && loadElements.size() != 0) {
            for (Element ele : loadElements) {
                mLogger.info("bind      '" + ele.getSimpleName() + "'     loadMore");

                TypeElement classFile = (TypeElement) ele.getEnclosingElement();
                String fullName = ClassUtil.getFullName(classFile);

                BRAVHBuildingSet buildingSet = mBuildingMap.get(fullName);
                if (buildingSet != null) {
                    //if have @BindOnLoadMore
                    buildingSet.bindLoadMore(ele);
                } else {
                    for (Map.Entry<String, BRAVHBuildingSet> entry : mBuildingMap.entrySet()) {
                        BRAVHBuildingSet bravhBuildingSet = entry.getValue();
                        String moduleClassName = bravhBuildingSet.getRecyclerHolderModuleClass().toString();
                        if (moduleClassName.equals(fullName)) {
                            //if have @BindOnLoadMore
                            bravhBuildingSet.bindLoadMore(ele);
                        }
                    }
                }
            }
        }
    }

    private void bindConvert(RoundEnvironment roundEnvironment) {
        Set<? extends Element> convertElements = roundEnvironment.getElementsAnnotatedWith(BindConvert.class);
        for (Element ele : convertElements) {
            mLogger.info("bind      '" + ele.getSimpleName() + "'     Convert");

            TypeElement classFile = (TypeElement) ele.getEnclosingElement();
            BRAVHBuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());
            if (buildingSet == null) {
                throw new IllegalArgumentException("@RecyclerHolder don't include @BindConvert element,please add @BindConvert on a method");
            }
            buildingSet.bindConvert(ele);
        }
    }

    private void bindClassAnnotation(RoundEnvironment roundEnvironment) {
        Set<? extends Element> recyclerHolderElements = roundEnvironment.getElementsAnnotatedWith(RecyclerHolder.class);
        for (Element ele : recyclerHolderElements) {
            mLogger.info("bind      '" + ele.getSimpleName() + "'     RecyclerHolder");

            TypeElement classFile = (TypeElement) ele;
            BRAVHBuildingSet buildingSet = new BRAVHBuildingSet(ele);
            mBuildingMap.put(classFile.getQualifiedName().toString(), buildingSet);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> types = new HashSet<>();
        types.add(RecyclerHolder.class.getCanonicalName());
        types.add(BindConvert.class.getCanonicalName());
        types.add(BindOnLoadMore.class.getCanonicalName());
        types.add(Bind.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
