package ResourcePack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ResourcePackManager {

    private final CarcerWorldCore plugin;

    private final File resourcePackFolder;
    private final File generatedFolder;
    private final File basePack;
    private final File modelEnginePack;
    private final File outputPack;
    private final File publishedStateFile;

    private final WeaponSkinImporter weaponSkinImporter;

    private volatile String currentSHA1;
    private volatile Set<String> skinIds = Set.of();
    private volatile PublishedPack publishedPack;

    private record PublishedPack(String url, String sha1) {}

    public ResourcePackManager(CarcerWorldCore plugin) {
        this.plugin = plugin;

        resourcePackFolder =
                new File(plugin.getDataFolder(), "resourcepack");

        generatedFolder =
                new File(resourcePackFolder, "generated");

        basePack =
                new File(resourcePackFolder, "base.zip");

        modelEnginePack = new File(
                plugin.getDataFolder().getParentFile(),
                "ModelEngine/resource pack.zip"
        );

        outputPack =
                new File(generatedFolder, "CarcerWorldPack.zip");

        publishedStateFile =
                new File(generatedFolder, "published.properties");

        if (!generatedFolder.isDirectory() && !generatedFolder.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create resource pack folder: " + generatedFolder
            );
        }

        weaponSkinImporter = new WeaponSkinImporter(resourcePackFolder);

        loadExistingPack();
        loadPublishedState();
    }

    public synchronized boolean rebuildPack() {
        if (!basePack.isFile()) {
            plugin.getLogger().severe(
                    "Missing base resource pack: " + basePack
            );
            return false;
        }

        // Preserve the requirement in your existing implementation.
        if (!modelEnginePack.isFile()) {
            plugin.getLogger().severe(
                    "Missing ModelEngine resource pack: " + modelEnginePack
            );
            return false;
        }

        Path temporary = null;

        try {
            Map<String, byte[]> entries = new LinkedHashMap<>();

            readZip(basePack, entries, false);
            readZip(modelEnginePack, entries, true);

            Set<String> imported =
                    weaponSkinImporter.importSkins(entries);

            if (!entries.containsKey("pack.mcmeta")) {
                throw new IOException(
                        "Merged pack has no pack.mcmeta at its root."
                );
            }

            temporary = Files.createTempFile(
                    generatedFolder.toPath(), "carcer-build-", ".zip"
            );

            writeZip(temporary, entries);

            String newHash = calculateSHA1(temporary);

            // Replace the output only after the new ZIP is complete.
            replaceFile(temporary, outputPack.toPath());
            temporary = null;

            currentSHA1 = newHash;
            skinIds = Set.copyOf(imported);

            plugin.getLogger().info(
                    "CarcerWorld pack rebuilt with "
                            + imported.size() + " imported weapon skin(s)."
            );

            plugin.getLogger().info(
                    "Output: " + outputPack.getAbsolutePath()
            );

            plugin.getLogger().info("SHA-1: " + currentSHA1);

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(
                    Level.SEVERE, "Resource pack rebuild failed.", e
            );
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    plugin.getLogger().warning(
                            "Could not remove temporary pack: " + temporary
                    );
                }
            }
        }
    }

    private void readZip(
            File source,
            Map<String, byte[]> entries,
            boolean replaceExisting
    ) throws IOException {

        try (ZipFile zip = new ZipFile(source)) {
            Enumeration<? extends ZipEntry> enumeration = zip.entries();

            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();

                if (entry.isDirectory()) continue;

                String path = normalizePath(entry.getName());
                String lower = path.toLowerCase(Locale.ROOT);

                if (lower.startsWith("__macosx/")
                        || lower.endsWith(".ds_store")) {
                    continue;
                }

                if (!replaceExisting && entries.containsKey(path)) {
                    throw new IOException(
                            "Duplicate entry in " + source.getName() + ": " + path
                    );
                }

                if (replaceExisting && entries.containsKey(path)
                        && !path.equals("pack.mcmeta")
                        && !path.equals("pack.png")) {

                    plugin.getLogger().warning(
                            "Pack conflict; using ModelEngine asset: " + path
                    );
                }

                try (InputStream input = zip.getInputStream(entry)) {
                    entries.put(path, input.readAllBytes());
                }
            }
        }
    }

    private String normalizePath(String raw) throws IOException {
        String path = raw.replace('\\', '/');

        if (path.startsWith("/") || path.contains(":")) {
            throw new IOException("Invalid ZIP path: " + raw);
        }

        for (String segment : path.split("/")) {
            if (segment.equals("..") || segment.equals(".")) {
                throw new IOException("Invalid ZIP path: " + raw);
            }
        }

        return path;
    }

    private void writeZip(Path destination, Map<String, byte[]> entries)
            throws IOException {

        try (ZipOutputStream output = new ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(destination))
        )) {
            // Stable order and timestamps prevent unnecessary hash changes.
            for (String path : new TreeSet<>(entries.keySet())) {
                ZipEntry entry = new ZipEntry(path);
                entry.setTime(0L);

                output.putNextEntry(entry);
                output.write(entries.get(path));
                output.closeEntry();
            }
        }
    }

    public synchronized boolean publishPack() {
        if (!outputPack.isFile()) {
            plugin.getLogger().severe(
                    "No generated pack exists. Run /carcerpack rebuild first."
            );
            return false;
        }

        try {
            String hash = calculateSHA1(outputPack.toPath());

            GitHubReleaseUploader uploader =
                    new GitHubReleaseUploader(plugin);

            String url = uploader.uploadResourcePack(outputPack, hash);

            if (url == null || url.isBlank()) {
                throw new IOException("Uploader returned an empty URL.");
            }

            PublishedPack state = new PublishedPack(url, hash);

            currentSHA1 = hash;
            publishedPack = state;

            try {
                savePublishedState(state);
            } catch (IOException e) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "Pack uploaded, but publication state could not be saved. "
                                + "Publish again after the next restart.",
                        e
                );
            }

            plugin.getLogger().info("Resource pack published: " + url);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(
                    Level.SEVERE, "Resource pack publication failed.", e
            );
            return false;
        }
    }

    public synchronized boolean rebuildAndPublish() {
        return rebuildPack() && publishPack();
    }

    @SuppressWarnings("deprecation")
    public void sendPack(Player player) {
        PublishedPack state = publishedPack;

        if (state == null) {
            player.sendMessage(
                    "§cThe server resource pack has not been published yet."
            );
            return;
        }

        boolean required = plugin.getConfig().getBoolean(
                "resource-pack.required", true
        );

        String prompt = plugin.getConfig().getString(
                "resource-pack.prompt",
                "CarcerWorld requires its custom resource pack."
        );

        player.setResourcePack(
                state.url(),
                HexFormat.of().parseHex(state.sha1()),
                prompt,
                required
        );
    }

    public void sendPackToEveryone() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendPack(player);
        }
    }

    private void loadExistingPack() {
        if (!outputPack.isFile()) return;

        try {
            currentSHA1 = calculateSHA1(outputPack.toPath());

            Set<String> loaded = new TreeSet<>();
            String prefix =
                    "assets/" + WeaponSkinImporter.NAMESPACE + "/items/";

            try (ZipFile zip = new ZipFile(outputPack)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    String path = entries.nextElement().getName();

                    if (!path.startsWith(prefix) || !path.endsWith(".json")) {
                        continue;
                    }

                    String id = path.substring(
                            prefix.length(), path.length() - ".json".length()
                    );

                    if (id.matches("[a-z0-9_]+")) {
                        loaded.add(id);
                    }
                }
            }

            skinIds = Set.copyOf(loaded);
        } catch (Exception e) {
            plugin.getLogger().log(
                    Level.WARNING, "Could not read existing resource pack.", e
            );
        }
    }

    private void loadPublishedState() {
        if (!publishedStateFile.isFile()) {
            plugin.getLogger().info(
                    "No saved publication state. Run /carcerpack publish "
                            + "or /carcerpack reload before sending the pack."
            );
            return;
        }

        try (InputStream input =
                     Files.newInputStream(publishedStateFile.toPath())) {

            Properties properties = new Properties();
            properties.load(input);

            String url = properties.getProperty("url", "");
            String hash = properties.getProperty("sha1", "");

            if (!url.startsWith("https://")
                    || !hash.matches("[a-fA-F0-9]{40}")) {
                throw new IOException("Invalid published.properties contents.");
            }

            publishedPack = new PublishedPack(url, hash);
        } catch (Exception e) {
            plugin.getLogger().log(
                    Level.WARNING, "Could not load publication state.", e
            );
        }
    }

    private void savePublishedState(PublishedPack state) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("url", state.url());
        properties.setProperty("sha1", state.sha1());

        Path temporary = Files.createTempFile(
                generatedFolder.toPath(), "carcer-published-", ".tmp"
        );

        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "Last successfully published pack");
            }

            replaceFile(temporary, publishedStateFile.toPath());
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void replaceFile(Path source, Path destination)
            throws IOException {

        try {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private String calculateSHA1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream input = new BufferedInputStream(
                Files.newInputStream(file)
        )) {
            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1) {
                digest.update(buffer, 0, count);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    public File getBasePack() {
        return basePack;
    }

    public File getModelEnginePack() {
        return modelEnginePack;
    }

    public File getOutputPack() {
        return outputPack;
    }

    public File getWeaponSkinsFolder() {
        return weaponSkinImporter.getFolder();
    }

    public Set<String> getSkinIds() {
        return skinIds;
    }

    public boolean hasSkin(String id) {
        return skinIds.contains(id);
    }

    public String getCurrentSHA1() {
        return currentSHA1;
    }

    public String getCurrentDownloadURL() {
        PublishedPack state = publishedPack;
        return state == null ? null : state.url();
    }

    public boolean hasPublishedPack() {
        return publishedPack != null;
    }
}