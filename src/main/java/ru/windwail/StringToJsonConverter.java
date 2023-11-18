package ru.windwail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StringToJsonConverter {
    public static void main(String[] args) {
        try {
            createBuildFolder();
            clearBuildFolder();
            convertFilesInResources();
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

    private static void createBuildFolder() throws IOException {
        // Create the 'build' folder if it doesn't exist
        Path buildFolderPath = Paths.get("build");
        if (!Files.exists(buildFolderPath)) {
            Files.createDirectories(buildFolderPath);
        }
    }

    private static void convertFilesInResources() throws IOException {
        // Get the list of input files in the resources folder
        List<Path> inputFiles = getFilesInResources("input");

        // Process each input file
        for (Path inputFile : inputFiles) {
            String input = readFile(inputFile);
            List<WordData> wordDataList = convertToWordDataList(input);
            String jsonResult = convertToJson(wordDataList);

            // Write the JSON result to the build folder
            String outputFileName = "output_" + inputFile.getFileName().toString().replace(".txt", ".json");
            writeFile(Paths.get("build", outputFileName), jsonResult);
        }
    }

    private static List<Path> getFilesInResources(String folder) throws IOException {
        // Get the list of input files in the resources folder
        ClassLoader classLoader = StringToJsonConverter.class.getClassLoader();
        Path resourcesPath = Paths.get(classLoader.getResource(folder).getPath());
        List<Path> inputFiles = new ArrayList<>();

        Files.list(resourcesPath)
                .filter(Files::isRegularFile)
                .forEach(inputFiles::add);

        return inputFiles;
    }

    private static String readFile(Path filePath) throws IOException {
        // Read the content of the input file
        return Files.readString(filePath);
    }

    private static void writeFile(Path filePath, String content) throws IOException {
        // Write the content to the output file
        Files.writeString(filePath, content);
    }

    private static List<WordData> convertToWordDataList(String input) {
        List<WordData> wordDataList = new ArrayList<>();
        Scanner scanner = new Scanner(input);
        String audioFile = scanner.nextLine().trim();

        int id = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                id++;
                String[] parts = line.split("\\s+");
                String word = parts[0];
                String translation = (parts.length > 3) ? parts[3] : "";
                double playFrom = Double.parseDouble(parts[1]);
                double playTo = Double.parseDouble(parts[2]);

                WordData wordData = new WordData(Integer.toString(id), word, translation, audioFile, playFrom, playTo);
                wordDataList.add(wordData);
            }
        }

        return wordDataList;
    }

    private static String convertToJson(List<WordData> wordDataList) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray dataArray = new JsonArray();

        for (WordData wordData : wordDataList) {
            JsonObject wordObject = new JsonObject();
            wordObject.addProperty("id", wordData.getId());
            wordObject.addProperty("word", wordData.getWord());
            wordObject.addProperty("translation", wordData.getTranslation());
            wordObject.addProperty("audioFile", wordData.getAudioFile());
            wordObject.addProperty("playFrom", wordData.getPlayFrom());
            wordObject.addProperty("playTo", wordData.getPlayTo());

            dataArray.add(wordObject);
        }

        return gson.toJson(dataArray);
    }

    private static class WordData {
        private final String id;
        private final String word;
        private final String translation;
        private final String audioFile;
        private final double playFrom;
        private final double playTo;

        public WordData(String id, String word, String translation, String audioFile, double playFrom, double playTo) {
            this.id = id;
            this.word = word;
            this.translation = translation;
            this.audioFile = audioFile;
            this.playFrom = playFrom;
            this.playTo = playTo;
        }

        public String getId() {
            return id;
        }

        public String getWord() {
            return word;
        }

        public String getTranslation() {
            return translation;
        }

        public String getAudioFile() {
            return audioFile;
        }

        public double getPlayFrom() {
            return playFrom;
        }

        public double getPlayTo() {
            return playTo;
        }
    }
}
