package ru.windwail;
import com.google.gson.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlToJsonConverter {
    public static void main(String[] args) {
        try {
            convertYamlFileInResources("lessons.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearBuildFolder() throws IOException {
        // Clear old files in the 'build' folder
        Path buildFolderPath = Paths.get("build");
        if (Files.exists(buildFolderPath)) {
            Files.walk(buildFolderPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private static void convertYamlFileInResources(String fileName) throws IOException {
        // Get the YAML file in the resources folder
        ClassLoader classLoader = YamlToJsonConverter.class.getClassLoader();
        Path yamlFilePath = Paths.get(classLoader.getResource(fileName).getPath());

        if (Files.exists(yamlFilePath)) {
            String yamlContent = readFile(yamlFilePath);
            List<Lesson> lessons = convertToLessonList(yamlContent);
            String jsonResult = convertToJson(lessons);

            // Write the JSON result to the build folder
            String outputFileName = "output_" + fileName.replace(".yaml", ".json");
            writeFile(Paths.get("build", outputFileName), jsonResult);
        } else {
            System.out.println("File not found: " + fileName);
        }
    }

    private static String readFile(Path filePath) throws IOException {
        // Read the content of the YAML file
        return Files.readString(filePath);
    }

    private static void writeFile(Path filePath, String content) throws IOException {
        // Write the content to the output file
        Files.writeString(filePath, content);
    }

    private static List<Lesson> convertToLessonList(String yamlContent) {
        Yaml yaml = new Yaml();
        Map<String, List<Map<String, Map<String, List<String>>>>> yamlMap = yaml.load(yamlContent);

        List<Lesson> lessons = new ArrayList<>();
        int lessonId = 1;

        for (Map.Entry<String, List<Map<String, Map<String, List<String>>>>> entry : yamlMap.entrySet()) {
            String lessonName = entry.getKey();
            List<Map<String, Map<String, List<String>>>> quizzes = entry.getValue();

            List<Quiz> quizList = new ArrayList<>();
            int quizId = 1;

            for (Map<String, Map<String, List<String>>> quizMap : quizzes) {
                Map.Entry<String, Map<String, List<String>>> quizEntry = quizMap.entrySet().iterator().next();
                String quizName = quizEntry.getKey();
                JsonArray data = readAndConcatenateJsonFiles(quizEntry.getValue().get("files"));

                quizList.add(new Quiz(Integer.toString(quizId), Integer.toString(lessonId), quizName, data));
                quizId++;
            }

            lessons.add(new Lesson(Integer.toString(lessonId), lessonName, quizList));
            lessonId++;
        }

        return lessons;
    }

    private static JsonArray readAndConcatenateJsonFiles(List<String> fileNames) {
        JsonArray result = new JsonArray();

        fileNames.forEach(fileName -> {
            JsonArray jsonArray = readJsonFileContent(fileName);
            jsonArray.forEach(result::add);
        });

        return result;
    }

    private static JsonArray readJsonFileContent(String fileName) {
        try {
            // Decode the URL-encoded filename
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());

            // Get the JSON file content
            ClassLoader classLoader = YamlToJsonConverter.class.getClassLoader();
            Path jsonFilePath = Paths.get(classLoader.getResource("json/" + decodedFileName + ".json").getPath());

            // Read and parse the JSON content
            String jsonContent = Files.readString(jsonFilePath);
            return JsonParser.parseString(jsonContent).getAsJsonArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonArray(); // Return an empty array in case of an error
        }
    }

    private static String convertToJson(List<Lesson> lessons) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(lessons);
    }

    private static class Lesson {
        private final String id;
        private final String name;
        private final List<Quiz> quizes;

        public Lesson(String id, String name, List<Quiz> quizes) {
            this.id = id;
            this.name = name;
            this.quizes = quizes;
        }
    }

    private static class Quiz {
        private final String id;
        private final String parentId;
        private final String name;
        private final JsonArray data;

        public Quiz(String id, String parentId, String name, JsonArray data) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.data = data;
        }
    }
}
