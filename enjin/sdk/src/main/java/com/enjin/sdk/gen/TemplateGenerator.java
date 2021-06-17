package com.enjin.sdk.gen;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TemplateGenerator {

    public void generate() {
        try {
            File resourcesDir = new File("templates");
            TemplateLoader templateLoader = new TemplateLoader(resourcesDir);

            templateLoader.load();

            generateTemplateClass(templateLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void generateTemplateClass(TemplateLoader templateLoader) {
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder("TemplateConstants")
                                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        System.out.println("1a");
        for (Map.Entry<String, Template> entry : templateLoader.getOperations().entrySet()) {
            System.out.println("2a");
            String fieldName = entry.getKey()
                                    .replace("Mutation", "")
                                    .replace("Query", "");
            String fieldValue = entry.getValue().compile().replace("\n", " ");
            FieldSpec spec = FieldSpec.builder(String.class,
                                               fieldName,
                                               Modifier.STATIC,
                                               Modifier.FINAL,
                                               Modifier.PUBLIC)
                                      .initializer("$S", fieldValue)
                                      .build();
            typeSpec.addField(spec);
        }

        JavaFile javaFile = JavaFile.builder("com.enjin.sdk.graphql", typeSpec.build())
                                    .indent("    ")
                                    .build();

        try {
            javaFile.writeTo(new File("src/main/java/com/enjin/sdk/graphql"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
