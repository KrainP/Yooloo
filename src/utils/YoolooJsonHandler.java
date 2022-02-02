package utils;

import allgemein.Konstanten;
import common.YoolooSpieler;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Diese Klasse stellt alle JSON benötigten Methoden bereit
 *
 * @author Pascal Krain
 * @since 02.02.2022
 */
public class YoolooJsonHandler {

    /**
     * Holt das JSON File des Spielers
     * @return Das JSON Object des Spielers
     * @throws Exception IOException
     * @since 01.02.2022
     */
    public JSONObject holeClientJSON(YoolooSpieler meinSpieler) throws Exception {
        JSONObject json = null;
        String username = meinSpieler.getName();

        File resDir = new File(Konstanten.RES_PATH);

        if (!resDir.exists()) {
            boolean mkdirs = resDir.mkdirs();
        }

        String jsonPath = Konstanten.RES_PATH + "/" + username + ".json";
        File jsonFile = new File(jsonPath);
        try {
            if (!jsonFile.exists()) {
                boolean createFile = jsonFile.createNewFile();
                json = new JSONObject();
                json.put(Konstanten.JSON_USERNAME, username);
                json.put(Konstanten.JSON_HIGHSCORE, 0);
                json.put(Konstanten.JSON_FARBE, "");

                schreibeJSON(jsonPath, json);
            } else {
                String text = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
                json = new JSONObject(text);
            }
        } catch (IOException e) {
            YoolooLogger.error(e.toString());
            e.printStackTrace();
        }

        return json;
    }

    /**
     * Aktualisiert die max. Punkte des Spielers
     * @param meinSpieler für die
     * @since 02.02.2022
     * @throws Exception IOException
     */
    public void aktualisierePunkte(YoolooSpieler meinSpieler) throws Exception {
        JSONObject json;
        String username = meinSpieler.getName();

        String jsonPath = Konstanten.RES_PATH + "/" + username + ".json";
        File jsonFile = new File(jsonPath);

        String text = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
        json = new JSONObject(text);
        int aktPunkte = meinSpieler.getPunkte();
        int aktHighscore = json.getInt(Konstanten.JSON_HIGHSCORE);
        if (aktPunkte > aktHighscore) {
            json.put(Konstanten.JSON_HIGHSCORE, aktPunkte);
            schreibeJSON(jsonPath, json);
        }
    }

    /**
     * Schreibt den JSON String in das dementsprechende File
     * @param path Pfad zum JSON Ordner
     * @param json Das zu schreibende JSON
     * @throws Exception IOException
     */
    private void schreibeJSON(String path, JSONObject json) throws Exception {
        File jsonFile = new File(path);
        try (FileWriter file = new FileWriter(jsonFile)) {
            file.write(json.toString());
            file.flush();
        }
    }
}
