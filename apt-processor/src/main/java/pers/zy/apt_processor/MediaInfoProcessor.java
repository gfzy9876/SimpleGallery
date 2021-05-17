package pers.zy.apt_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import pers.zy.apt_annotation.MediaInfoConstants;
import pers.zy.apt_annotation.MediaInfoReceived;

/**
 * date: 4/19/21   time: 12:05 PM
 * author zy
 * Have a nice day :)
 **/
@AutoService(Processor.class)
public class MediaInfoProcessor extends AbstractProcessor {

    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        System.out.println("init");
        mFiler = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("getSupportedSourceVersion " + SourceVersion.latestSupported());
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("getSupportedAnnotationTypes");
        HashSet<String> strings = new HashSet<>();
        strings.add(MediaInfoReceived.class.getCanonicalName());
        return strings;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("process " + annotations.isEmpty());
        for (Element element : roundEnv.getElementsAnnotatedWith(MediaInfoReceived.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;
            try {
                createMediaInfoReceivedBindingFile(executableElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void createMediaInfoReceivedBindingFile(ExecutableElement executableElement) throws IOException {
        Element enclosingElement = executableElement.getEnclosingElement();
        Name targetSimpleName = executableElement.getEnclosingElement().getSimpleName();
        String pkgName = enclosingElement.toString().replace("." + targetSimpleName.toString(), "");
        /**
         * 生成方法
         * 1. 生成方法参数
         * 2. 生成方法
         * */
        //1.
        ClassName list = ClassName.get("java.util", "List");
        ClassName mediaInfo = ClassName.get("pers.zy.gallerylib.model", "MediaInfo");
        TypeName parameterizedTypeName = ParameterizedTypeName.get(list, mediaInfo);
        ParameterSpec build = ParameterSpec.builder(parameterizedTypeName, "list").build();
        //2.
        MethodSpec method = MethodSpec.methodBuilder("onMediaInfoReceived")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(build)
                .addStatement("$T activity = reference.get()", ClassName.get(pkgName, targetSimpleName.toString()))
                .addCode("if (activity != null) {\n" +
                        "\tactivity." + executableElement.getSimpleName() + "(list);\n" +
                        "}\n")
                .build();

        //生成成员变量
        FieldSpec fieldSpec = FieldSpec.builder(
                    ParameterizedTypeName.get(ClassName.get("java.lang.ref", "WeakReference"),
                        ClassName.get(pkgName, targetSimpleName.toString())), "reference")
                .addModifiers(Modifier.PUBLIC)
                .build();

        //生成类
        TypeSpec bindClass = TypeSpec.classBuilder(enclosingElement.getSimpleName() + MediaInfoConstants.MEDIA_INFO_PROXY)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method)
                .addField(fieldSpec)
                .build();

        //生成类文件，并写入
        JavaFile javaFile = JavaFile.builder(pkgName, bindClass)
                .build();
        javaFile.writeTo(mFiler);
    }
}
