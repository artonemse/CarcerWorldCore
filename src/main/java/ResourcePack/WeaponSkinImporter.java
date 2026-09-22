package ResourcePack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class WeaponSkinImporter {

    public static final String NAMESPACE = "carcer_skins";

    private static final Pattern VALID_ID =
            Pattern.compile("[a-z0-9_]+");

    private final File folder;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public WeaponSkinImporter(File resourcePackFolder) {
        folder = new File(resourcePackFolder, "weapon-skins");

        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create weapon skin folder: " + folder
            );
        }
    }

    public File getFolder() {
        return folder;
    }

    public Set<String> importSkins(Map<String, byte[]> entries)
            throws IOException {

        File[] directories = folder.listFiles(File::isDirectory);

        if (directories == null) {
            throw new IOException("Could not read skin folder: " + folder);
        }

        Arrays.sort(directories, Comparator.comparing(File::getName));

        Set<String> imported = new LinkedHashSet<>();

        for (File directory : directories) {
            String id = directory.getName();

            if (!VALID_ID.matcher(id).matches()) {
                throw new IOException(
                        "Invalid skin folder '" + id
                                + "'. Use lowercase letters, numbers and underscores."
                );
            }

            File textureFile = new File(directory, "texture.png");

            if (!textureFile.isFile()) {
                throw new IOException(
                        "Missing texture.png in skin folder: " + id
                );
            }

            if (ImageIO.read(textureFile) == null) {
                throw new IOException(
                        "Cannot read texture.png for skin: " + id
                );
            }

            String textureLocation = NAMESPACE + ":item/" + id;
            String modelLocation = NAMESPACE + ":item/" + id;

            JsonObject model = loadModel(directory);

            // Each imported skin uses one texture. Keep the supplied
            // geometry/display settings, but point layer0 to our copied PNG.
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", textureLocation);
            textures.addProperty("particle", textureLocation);
            model.add("textures", textures);

            JsonObject modelReference = new JsonObject();
            modelReference.addProperty("type", "minecraft:model");
            modelReference.addProperty("model", modelLocation);

            JsonObject itemDefinition = new JsonObject();
            itemDefinition.add("model", modelReference);

            putUnique(
                    entries,
                    "assets/" + NAMESPACE + "/textures/item/" + id + ".png",
                    Files.readAllBytes(textureFile.toPath())
            );

            putUnique(
                    entries,
                    "assets/" + NAMESPACE + "/models/item/" + id + ".json",
                    jsonBytes(model)
            );

            putUnique(
                    entries,
                    "assets/" + NAMESPACE + "/items/" + id + ".json",
                    jsonBytes(itemDefinition)
            );

            // Preserve animation/filter metadata when supplied.
            File textureMetadata =
                    new File(directory, "texture.png.mcmeta");

            if (textureMetadata.isFile()) {
                validateJsonObject(textureMetadata);

                putUnique(
                        entries,
                        "assets/" + NAMESPACE
                                + "/textures/item/" + id + ".png.mcmeta",
                        Files.readAllBytes(textureMetadata.toPath())
                );
            }

            imported.add(id);
        }

        return Set.copyOf(imported);
    }

    private JsonObject loadModel(File directory) throws IOException {
        File modelFile = new File(directory, "model.json");

        if (!modelFile.isFile()) {
            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:item/handheld");
            return model;
        }

        JsonObject model = validateJsonObject(modelFile);

        if (model.has("overrides")) {
            throw new IOException(
                    directory.getName()
                            + "/model.json must be the sword model itself, "
                            + "not a vanilla item override file."
            );
        }

        if (model.has("model")) {
            throw new IOException(
                    directory.getName()
                            + "/model.json is an item definition. "
                            + "Supply the model from assets/.../models/item/ instead."
            );
        }

        if (!model.has("parent") && !model.has("elements")) {
            throw new IOException(
                    directory.getName()
                            + "/model.json needs a parent or elements."
            );
        }

        // This importer intentionally supports single-texture weapons.
        // Reject additional texture slots instead of silently breaking them.
        if (model.has("textures")) {
            JsonObject textures;

            try {
                textures = model.getAsJsonObject("textures");
            } catch (RuntimeException e) {
                throw new IOException("Invalid textures in " + modelFile, e);
            }

            for (String key : textures.keySet()) {
                if (!key.equals("layer0") && !key.equals("particle")) {
                    throw new IOException(
                            directory.getName()
                                    + " uses texture slot '" + key
                                    + "'. This importer supports layer0 only."
                    );
                }
            }
        }

        return model;
    }

    private JsonObject validateJsonObject(File file) throws IOException {
        try {
            String text = Files.readString(
                    file.toPath(), StandardCharsets.UTF_8
            );

            JsonElement parsed = JsonParser.parseString(text);

            if (!parsed.isJsonObject()) {
                throw new IOException("Expected a JSON object in " + file);
            }

            return parsed.getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IOException(
                    "Invalid JSON in " + file + ": " + e.getMessage(), e
            );
        }
    }

    private byte[] jsonBytes(JsonObject object) {
        return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
    }

    private void putUnique(
            Map<String, byte[]> entries,
            String path,
            byte[] contents
    ) throws IOException {

        if (entries.containsKey(path)) {
            throw new IOException(
                    "Duplicate imported asset: " + path
                            + ". Remove its older copy from the source pack."
            );
        }

        entries.put(path, contents);
    }
}
