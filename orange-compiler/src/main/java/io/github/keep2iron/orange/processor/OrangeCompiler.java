package io.github.keep2iron.orange.processor;

import com.google.auto.service.AutoService;

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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.github.keep2iron.orange.BuildingSet;
import io.github.keep2iron.orange.annotations.BindConvert;
import io.github.keep2iron.orange.annotations.RecyclerHolder;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/04 21:46
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.github.keep2iron.orange.annotations.RecyclerHolder",
        "io.github.keep2iron.orange.annotations.BindConvert"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class OrangeCompiler extends AbstractProcessor {

    /**
     * one @RecyclerHolder java file map into a BuildingSet object to generate file
     */
    private ConcurrentHashMap<String, BuildingSet> mBuildingMap;

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

        Set<? extends Element> recyclerHolderElements = roundEnvironment.getElementsAnnotatedWith(RecyclerHolder.class);
        for (Element ele : recyclerHolderElements) {
            TypeElement classFile = (TypeElement) ele;
            BuildingSet buildingSet = new BuildingSet(ele);
            mBuildingMap.put(classFile.getQualifiedName().toString(), buildingSet);
        }

        Set<? extends Element> convertElements = roundEnvironment.getElementsAnnotatedWith(BindConvert.class);
        if (convertElements == null || convertElements.size() == 0) {
            throw new IllegalArgumentException("your must define @BindConvert in your java file");
        }

        for (Element ele : convertElements) {
            TypeElement classFile = (TypeElement) ele.getEnclosingElement();
            BuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());
            if (buildingSet == null) {
                throw new IllegalArgumentException("don't find class annotation @RecyclerHolder mapping");
            }
            buildingSet.bindConvert(ele);
        }

        for (Map.Entry<String, BuildingSet> entry : mBuildingMap.entrySet()) {
            entry.getValue().build(mFiler);
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> types = new HashSet<>();
        types.add("io.github.keep2iron.orange.annotations.RecyclerHolder");
        types.add("io.github.keep2iron.orange.annotations.BindConvert");
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
