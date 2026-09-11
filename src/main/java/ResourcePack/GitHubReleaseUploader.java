package ResourcePack;

import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubReleaseUploader {

    private final CarcerWorldCore plugin;
    private final HttpClient client;

    private final String owner;
    private final String repository;
    private final String releaseTag;
    private final String token;

    public GitHubReleaseUploader(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.owner = plugin.getConfig().getString("github.owner", "");
        this.repository = plugin.getConfig().getString("github.repository", "");
        this.releaseTag = plugin.getConfig().getString("github.release-tag", "resourcepack");
        this.token = plugin.getConfig().getString("github.token", "");

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String uploadResourcePack(File file, String sha1) throws Exception {
        validateConfig();

        long releaseId = getReleaseId();
        String fileName = "CarcerWorldPack-" + sha1 + ".zip";
        List<ReleaseAsset> assets = getReleaseAssets(releaseId);

        for (ReleaseAsset asset : assets) {
            if (!asset.name().equalsIgnoreCase(fileName)) continue;

            plugin.getLogger().info("Resource pack already exists on GitHub: " + fileName);
            deleteOldResourcePacks(assets, asset.id());

            return createDownloadURL(fileName);
        }

        uploadAsset(releaseId, file, fileName);

        List<ReleaseAsset> updatedAssets = getReleaseAssets(releaseId);
        long currentAssetId = -1;

        for (ReleaseAsset asset : updatedAssets) {
            if (!asset.name().equalsIgnoreCase(fileName)) continue;

            currentAssetId = asset.id();
            break;
        }

        deleteOldResourcePacks(updatedAssets, currentAssetId);

        return createDownloadURL(fileName);
    }

    private long getReleaseId() throws Exception {
        String encodedTag = encode(releaseTag);
        String url = "https://api.github.com/repos/" + owner + "/" + repository + "/releases/tags/" + encodedTag;

        HttpRequest request = createRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) throw new IllegalStateException("GitHub release tag '" + releaseTag + "' was not found.");
        if (response.statusCode() != 200) throw new IllegalStateException("GitHub returned HTTP " + response.statusCode() + " while locating the resource pack release.");

        Pattern pattern = Pattern.compile("\"assets_url\"\\s*:\\s*\"https://api\\.github\\.com/repos/[^\"]+/releases/(\\d+)/assets\"");
        Matcher matcher = pattern.matcher(response.body());

        if (!matcher.find()) throw new IllegalStateException("Could not determine the GitHub release ID.");

        return Long.parseLong(matcher.group(1));
    }

    private List<ReleaseAsset> getReleaseAssets(long releaseId) throws Exception {
        String url = "https://api.github.com/repos/" + owner + "/" + repository + "/releases/" + releaseId + "/assets?per_page=100";

        HttpRequest request = createRequest(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) throw new IllegalStateException("GitHub returned HTTP " + response.statusCode() + " while reading release assets.");

        List<ReleaseAsset> assets = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"url\"\\s*:\\s*\"https://api\\.github\\.com/repos/[^\"]+/releases/assets/(\\d+)\".*?\"name\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response.body());

        while (matcher.find()) {
            long id = Long.parseLong(matcher.group(1));
            String name = matcher.group(2);

            assets.add(new ReleaseAsset(id, name));
        }

        return assets;
    }

    private void uploadAsset(long releaseId, File file, String fileName) throws Exception {
        String encodedName = encode(fileName);
        String url = "https://uploads.github.com/repos/" + owner + "/" + repository + "/releases/" + releaseId + "/assets?name=" + encodedName;

        HttpRequest request = createRequest(url)
                .header("Content-Type", "application/zip")
                .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new IllegalStateException("GitHub upload failed with HTTP " + response.statusCode() + ": " + response.body());
        }

        plugin.getLogger().info("Uploaded resource pack to GitHub: " + fileName);
    }

    private void deleteOldResourcePacks(List<ReleaseAsset> assets, long currentAssetId) {
        for (ReleaseAsset asset : assets) {
            if (asset.id() == currentAssetId) continue;
            if (!asset.name().startsWith("CarcerWorldPack-")) continue;
            if (!asset.name().endsWith(".zip")) continue;

            try {
                deleteAsset(asset.id());
            } catch (Exception e) {
                plugin.getLogger().warning("Could not delete old resource pack " + asset.name() + ": " + e.getMessage());
            }
        }
    }

    private void deleteAsset(long assetId) throws Exception {
        String url = "https://api.github.com/repos/" + owner + "/" + repository + "/releases/assets/" + assetId;

        HttpRequest request = createRequest(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204 && response.statusCode() != 404) {
            throw new IllegalStateException("GitHub returned HTTP " + response.statusCode() + " while deleting an old resource pack.");
        }
    }

    private HttpRequest.Builder createRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + token)
                .header("X-GitHub-Api-Version", "2026-03-10")
                .header("User-Agent", "CarcerWorldCore");
    }

    private String createDownloadURL(String fileName) {
        return "https://github.com/" + owner + "/" + repository + "/releases/download/" + releaseTag + "/" + fileName;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void validateConfig() {
        if (owner == null || owner.isBlank()) throw new IllegalStateException("github.owner is missing from config.yml.");
        if (repository == null || repository.isBlank()) throw new IllegalStateException("github.repository is missing from config.yml.");
        if (releaseTag == null || releaseTag.isBlank()) throw new IllegalStateException("github.release-tag is missing from config.yml.");
        if (token == null || token.isBlank()) throw new IllegalStateException("github.token is missing from the server config.yml.");
    }

    private record ReleaseAsset(long id, String name) {
    }
}
