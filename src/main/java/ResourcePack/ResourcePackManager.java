package ResourcePack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ResourcePackManager {

    private final CarcerWorldCore plugin;

    private final File resourcePackFolder;
    private final File generatedFolder;
    private final File basePack;
    private final File outputPack;
    private final File modelEnginePack;

    private String currentSHA1;
    private byte[] currentSHA1Bytes;
    private String currentDownloadURL;

    public ResourcePackManager(CarcerWorldCore plugin) {
        this.plugin = plugin;

        resourcePackFolder = new File(plugin.getDataFolder(), "resourcepack");
        generatedFolder = new File(resourcePackFolder, "generated");
        basePack = new File(resourcePackFolder, "base.zip");
        outputPack = new File(generatedFolder, "CarcerWorldPack.zip");

        File pluginsFolder = plugin.getDataFolder().getParentFile();
        modelEnginePack = new File(pluginsFolder, "ModelEngine/resource pack.zip");

        resourcePackFolder.mkdirs();
        generatedFolder.mkdirs();

        loadExistingPack();
    }

    public synchronized boolean rebuildPack() {
        if (!basePack.exists()) {
            plugin.getLogger().severe("Missing base resource pack: " + basePack.getAbsolutePath());
            return false;
        }

        if (!modelEnginePack.exists()) {
            plugin.getLogger().severe("Missing ModelEngine resource pack: " + modelEnginePack.getAbsolutePath());
            return false;
        }

        try {
            File tempPack = new File(generatedFolder, "CarcerWorldPack.tmp.zip");

            Files.deleteIfExists(tempPack.toPath());

            Set<String> modelEngineEntries = readEntryNames(modelEnginePack);

            try (ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempPack)))) {
                copyZip(basePack, output, modelEngineEntries, false);
                copyZip(modelEnginePack, output, Collections.emptySet(), true);
            }

            Files.deleteIfExists(outputPack.toPath());
            Files.move(tempPack.toPath(), outputPack.toPath());

            currentSHA1Bytes = calculateSHA1Bytes(outputPack);
            currentSHA1 = bytesToHex(currentSHA1Bytes);
            currentDownloadURL = createDownloadURL(currentSHA1);

            plugin.getLogger().info("CarcerWorld resource pack rebuilt.");
            plugin.getLogger().info("SHA-1: " + currentSHA1);
            plugin.getLogger().info("Output: " + outputPack.getAbsolutePath());

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to rebuild CarcerWorld resource pack.");
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean publishPack() {
        if (!outputPack.exists()) {
            plugin.getLogger().severe("CarcerWorldPack.zip does not exist. Rebuild it first.");
            return false;
        }

        try {
            if (currentSHA1 == null || currentSHA1Bytes == null) {
                currentSHA1Bytes = calculateSHA1Bytes(outputPack);
                currentSHA1 = bytesToHex(currentSHA1Bytes);
            }

            GitHubReleaseUploader uploader = new GitHubReleaseUploader(plugin);
            currentDownloadURL = uploader.uploadResourcePack(outputPack, currentSHA1);

            plugin.getLogger().info("Resource pack published successfully.");
            plugin.getLogger().info("Download URL: " + currentDownloadURL);

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to publish resource pack.");
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean rebuildAndPublish() {
        if (!rebuildPack()) return false;

        return publishPack();
    }

    @SuppressWarnings("deprecation")
    public void sendPack(Player player) {
        if (currentSHA1 == null || currentSHA1Bytes == null || currentDownloadURL == null) {
            plugin.getLogger().warning("Could not send resource pack to " + player.getName() + " because no published pack is available.");
            return;
        }

        boolean required = plugin.getConfig().getBoolean("resource-pack.required", true);
        String prompt = plugin.getConfig().getString("resource-pack.prompt", "CarcerWorld requires its custom resource pack.");

        player.setResourcePack(currentDownloadURL, currentSHA1Bytes, prompt, required);
    }

    public void sendPackToEveryone() {
        for (Player player : Bukkit.getOnlinePlayers()) sendPack(player);
    }

    private void loadExistingPack() {
        if (!outputPack.exists()) return;

        try {
            currentSHA1Bytes = calculateSHA1Bytes(outputPack);
            currentSHA1 = bytesToHex(currentSHA1Bytes);
            currentDownloadURL = createDownloadURL(currentSHA1);

            plugin.getLogger().info("Loaded existing CarcerWorld resource pack.");
            plugin.getLogger().info("SHA-1: " + currentSHA1);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load existing resource pack: " + e.getMessage());
        }
    }

    private Set<String> readEntryNames(File zipFile) throws IOException {
        Set<String> entries = new HashSet<>();

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> enumeration = zip.entries();

            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();

                if (entry.isDirectory()) continue;

                entries.add(normalizePath(entry.getName()));
            }
        }

        return entries;
    }

    private void copyZip(File source, ZipOutputStream output, Set<String> skipEntries, boolean modelEngine) throws IOException {
        try (ZipFile zip = new ZipFile(source)) {
            Enumeration<? extends ZipEntry> enumeration = zip.entries();

            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();

                if (entry.isDirectory()) continue;

                String path = normalizePath(entry.getName());

                if (shouldIgnore(path)) continue;

                if (skipEntries.contains(path)) {
                    if (!path.equalsIgnoreCase("pack.mcmeta") && !path.equalsIgnoreCase("pack.png")) {
                        plugin.getLogger().warning("Resource pack conflict, ModelEngine version will be used: " + path);
                    }

                    continue;
                }

                ZipEntry newEntry = new ZipEntry(path);
                newEntry.setTime(entry.getTime());

                output.putNextEntry(newEntry);

                try (InputStream input = new BufferedInputStream(zip.getInputStream(entry))) {
                    input.transferTo(output);
                }

                output.closeEntry();
            }
        }

        plugin.getLogger().info("Merged " + (modelEngine ? "ModelEngine" : "Carcer base") + " resource pack.");
    }

    private boolean shouldIgnore(String path) {
        String lower = path.toLowerCase(Locale.ROOT);

        if (lower.startsWith("__macosx/")) return true;
        if (lower.endsWith(".ds_store")) return true;

        return false;
    }

    private String normalizePath(String path) {
        String normalized = path.replace("\\", "/");

        while (normalized.startsWith("/")) normalized = normalized.substring(1);

        return normalized;
    }

    private byte[] calculateSHA1Bytes(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
        }

        return digest.digest();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (byte value : bytes) builder.append(String.format("%02x", value));

        return builder.toString();
    }

    private String createDownloadURL(String sha1) {
        String owner = plugin.getConfig().getString("github.owner", "");
        String repository = plugin.getConfig().getString("github.repository", "");
        String tag = plugin.getConfig().getString("github.release-tag", "resourcepack");

        return "https://github.com/" + owner + "/" + repository + "/releases/download/" + tag + "/CarcerWorldPack-" + sha1 + ".zip";
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

    public String getCurrentSHA1() {
        return currentSHA1;
    }

    public String getCurrentDownloadURL() {
        return currentDownloadURL;
    }
}