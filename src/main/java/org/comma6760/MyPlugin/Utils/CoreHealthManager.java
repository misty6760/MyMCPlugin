package org.comma6760.MyPlugin.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Interaction;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CoreHealthManager {

    private static final Map<Interaction, EnderCrystal> coreMap = new HashMap<>();
    private static final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    private static final Objective objective;

    static {
        Objective obj = scoreboard.getObjective("coreHealth");
        if (obj == null) {
            // 수정: "dummy" 문자열 대신 Criteria.DUMMY 사용
            obj = scoreboard.registerNewObjective("coreHealth", Criteria.DUMMY, Component.text("Core Health"));
            obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        objective = obj;
    }

    public static void registerCore(Interaction interaction, EnderCrystal crystal, int initialHealth) {
        coreMap.put(interaction, crystal);
        Score score = objective.getScore(interaction.getUniqueId().toString());
        score.setScore(initialHealth);
    }

    public static int damageCore(Interaction interaction) {
        String key = interaction.getUniqueId().toString();
        Score score = objective.getScore(key);
        int newHealth = score.getScore() - 1;
        score.setScore(newHealth);
        return newHealth;
    }

    public static void destroyCore(Interaction interaction) {
        EnderCrystal crystal = coreMap.get(interaction);
        Location loc = interaction.getLocation().add(0, 1, 0);
        World world = loc.getWorld();

        if (world != null) {
            world.spawnParticle(Particle.EXPLOSION, loc, 1);
            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        }

        interaction.remove();
        if (crystal != null && !crystal.isDead()) crystal.remove();

        coreMap.remove(interaction);
        Objects.requireNonNull(objective.getScoreboard()).resetScores(interaction.getUniqueId().toString());

        Bukkit.broadcast(Component.text("코어가 파괴되었습니다!", NamedTextColor.GOLD));
    }

    public static boolean isCore(Interaction interaction) {
        return coreMap.containsKey(interaction);
    }
}
