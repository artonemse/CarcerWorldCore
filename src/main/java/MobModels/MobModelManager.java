package MobModels;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.Locale;
import java.util.Set;

public class MobModelManager {

    private static final Set<String> MODEL_IDS = Set.of(
            "wandering_dead",
            "graveborn",
            "forsaken_archer",
            "death_crawler",
            "graveborn_reaver",

            "blackthorn_bandit",
            "blackthorn_raider",
            "roadside_cutthroat",
            "blackthorn_stalker",

            "drowned_raider",
            "drowned_corsair",
            "blacktide_raider",
            "dockside_cutthroat",
            "barnacle_brute",

            "fallen_guard",
            "broken_knight",
            "royal_archer",
            "oathbreaker",
            "royal_wraith",

            "tidewalker",
            "saltborn",
            "sea_wraith",
            "abyssal_raider",
            "deepwater_marauder",

            "sanctum_guardian",
            "ancient_knight",
            "sanctum_wraith",
            "fallen_disciple",
            "oathbound_sentinel",

            "thornbound_warden"
    );

    private final CarcerWorldCore plugin;
    private final NamespacedKey mobTypeKey;

    public MobModelManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.mobTypeKey = new NamespacedKey(plugin, "mob_type_id");
    }

    public boolean applyModel(LivingEntity entity, String mobId) {
        if (entity == null || mobId == null || mobId.isBlank()) return false;

        String modelId = normalizeId(mobId);

        if (!MODEL_IDS.contains(modelId)) {
            plugin.getLogger().warning("No custom model is registered for mob ID '" + modelId + "'.");
            return false;
        }

        entity.getPersistentDataContainer().set(mobTypeKey, PersistentDataType.STRING, modelId);

        try {
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(entity);
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);

            modeledEntity.addModel(activeModel, true);
            return true;
        } catch (Exception exception) {
            plugin.getLogger().severe(
                    "Could not apply Model Engine model '" + modelId
                            + "' to entity " + entity.getUniqueId() + "."
            );
            exception.printStackTrace();
            return false;
        }
    }

    public boolean applyStoredModel(LivingEntity entity) {
        if (entity == null) return false;

        String mobId = entity.getPersistentDataContainer().get(mobTypeKey, PersistentDataType.STRING);
        if (mobId == null || mobId.isBlank()) return false;

        return applyModel(entity, mobId);
    }

    public boolean hasModel(String mobId) {
        return mobId != null && MODEL_IDS.contains(normalizeId(mobId));
    }

    public String getModelId(String mobId) {
        if (!hasModel(mobId)) return null;
        return normalizeId(mobId);
    }

    private String normalizeId(String id) {
        return id.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
    }
}
