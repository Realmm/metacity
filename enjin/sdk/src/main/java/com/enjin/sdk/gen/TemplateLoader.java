package com.enjin.sdk.gen;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

public class TemplateLoader {

    private Map<String, Template> fragments = new HashMap<>();
    private SortedMap<String, Template> operations = new TreeMap<>();

    private final File resourcesDir;

    protected TemplateLoader(File resourcesDir) {
        this.resourcesDir = resourcesDir;
    }

    public void load() {
        loadRawResources();
    }

    private void loadRawResources() {
        System.out.println("loading raw resources");
        try {
            Files.walk(Paths.get(resourcesDir.toURI()))
                 .filter(Files::isRegularFile)
                 .map(path -> new File(path.toUri()))
                 .filter(f -> f.getName().endsWith(".gql"))
                 .forEach(this::loadRawTemplate);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SneakyThrows
    private void loadRawTemplate(File file) {
        System.out.println("loading template " + file.getName());
        List<String> lines = Files.readAllLines(file.toPath());
        String key = file.getName().replace(".gql", "");

        if (key.endsWith("Fragment"))
            fragments.put(key, new Template(key, lines, fragments));
        else if (key.endsWith("Mutation") || key.endsWith("Query"))
            operations.put(key, new Template(key, lines, fragments));
        else
            System.out.println(String.format("Invalid template name detected: %s", key));
    }

    public Map<String, Template> getOperations() {
        return operations;
    }
}
