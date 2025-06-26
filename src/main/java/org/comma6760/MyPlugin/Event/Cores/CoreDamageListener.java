package org.comma6760.MyPlugin.Event.Cores;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.comma6760.MyPlugin.Utils.CoreHealthManager;
import org.comma6760.MyPlugin.Main.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoreDamageListener implements Listener {

    private static final double DISPLAY_RADIUS = 5.0;
    private static final long DISPLAY_INTERVAL_TICKS = 1L;

    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    @EventHandler
    public void onCoreHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        if (!CoreHealthManager.isCore(interaction)) return;

        String coreTeam = CoreHealthManager.getTeamId(interaction);
        if (coreTeam == null) return;

        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var playerTeam = scoreboard.getEntryTeam(player.getName());
        if (playerTeam != null && playerTeam.getName().equals(coreTeam)) {
            player.sendActionBar(Component.text("같은 팀의 코어에는 피해를 줄 수 없습니다!", NamedTextColor.RED));
            player.playSound(
                    player.getLocation(),
                    "minecraft:block.note_block.bass",
                    SoundCategory.MASTER,
                    1f,
                    0.5f
            );
            event.setCancelled(true);
            return;
        }

        // 코어는 직접적인 데미지를 받지 않도록 이벤트 데미지 취소
        event.setCancelled(true);

        // 플레이어 공격력 계산 (무기 속성 포함)
        double damage = getPlayerAttackDamage(player);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f);

        int newHealth = CoreHealthManager.damageCore(interaction, damage);

        if (newHealth <= 0) {
            stopCoreWatcher(interaction.getUniqueId());
            CoreHealthManager.destroyCore(interaction);
            return;
        }

        startCoreWatcher(interaction);
    }

    private double getPlayerAttackDamage(Player player) {
        var attr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        return (attr != null) ? attr.getValue() : 1.0;
    }

    private void startCoreWatcher(Interaction interaction) {
        UUID id = interaction.getUniqueId();
        if (activeTasks.containsKey(id)) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!interaction.isValid() || !CoreHealthManager.isCore(interaction)) {
                    cancel();
                    activeTasks.remove(id);
                    return;
                }

                int health = CoreHealthManager.getHealth(interaction);
                Location coreLoc = interaction.getLocation();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().equals(coreLoc.getWorld())) continue;
                    if (player.getLocation().distance(coreLoc) <= DISPLAY_RADIUS) {
                        player.sendActionBar(Component.text("코어 체력: " + health, NamedTextColor.RED));
                    }
                }
            }
        };

        task.runTaskTimer(Main.getInstance(), 0L, DISPLAY_INTERVAL_TICKS);
        activeTasks.put(id, task);
    }

    private void stopCoreWatcher(UUID id) {
        BukkitRunnable task = activeTasks.remove(id);
        if (task != null) task.cancel();
    }
}
