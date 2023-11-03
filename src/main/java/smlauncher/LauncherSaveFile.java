package smlauncher;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LauncherSaveFile {
    static LauncherSaveFile inst;
    public static LauncherSaveFile getInstance(){
        if(inst == null){
            try {
                inst = LauncherSaveFile.load("./launcherSettings.json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return inst;
    }
    // Installation dir
    // JRE7 Dir
    // JRE18 Dir
    // Max/Initial memory
    // Server port
    // Repair
    // Extra JVM arguments
    // Extra Game arguments
    public String installationDir = ".";
    public String jre7Dir = "./dep/jre7";
    public String jre18Dir  = "./dep/jre18";
    public int maxMemory = 2048;
    public int initialMemory = 2048;
    public int serverPort = 4242;
    public String extraJVMArguments;
    public String extraGameArguments;

    public int version;
    public void save(String filePath) throws IOException {
        JSONObject jsonObject = saveFileToJson();
        try (FileWriter fileWriter = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            fileWriter.write(jsonObject.toString());
        }
    }

    public static LauncherSaveFile load(String filePath) throws IOException {
        if (!new File(filePath).exists()) {
            // Create blank.
            return new LauncherSaveFile();
        }
        try (FileReader fileReader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            JSONTokener jsonTokener = new JSONTokener(fileReader);
            JSONObject jsonObject = new JSONObject(jsonTokener);
            return jsonToSaveFile(jsonObject);
        }
    }

    private JSONObject saveFileToJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("installationDir", installationDir);
        jsonObject.put("jre7Dir", jre7Dir);
        jsonObject.put("jre18Dir", jre18Dir);
        jsonObject.put("maxMemory", maxMemory);
        jsonObject.put("initialMemory", initialMemory);
        jsonObject.put("serverPort", serverPort);
        jsonObject.put("extraJVMArguments", extraJVMArguments);
        jsonObject.put("extraGameArguments", extraGameArguments);
        jsonObject.put("version", version);
        return jsonObject;
    }

    private static LauncherSaveFile jsonToSaveFile(JSONObject jsonObject) {
        LauncherSaveFile sf = new LauncherSaveFile();
        sf.installationDir = jsonObject.getString("installationDir");
        sf.jre7Dir = jsonObject.getString("jre7Dir");
        sf.jre18Dir = jsonObject.getString("jre18Dir");
        sf.maxMemory = jsonObject.getInt("maxMemory");
        sf.initialMemory = jsonObject.getInt("initialMemory");
        sf.serverPort = jsonObject.getInt("serverPort");
        sf.extraJVMArguments = jsonObject.getString("extraJVMArguments");
        sf.extraGameArguments = jsonObject.getString("extraGameArguments");
        sf.version = jsonObject.getInt("version");
        return sf;
    }
}
