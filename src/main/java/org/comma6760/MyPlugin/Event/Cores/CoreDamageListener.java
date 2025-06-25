package org.comma6760.MyPlugin.Event.Cores;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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

    // 코어 체력 정보를 플레이어에게 보여줄 반경 (5블럭 이내)
    private static final double DISPLAY_RADIUS = 5.0;
    // 체력 정보를 갱신해서 보여줄 간격 (틱 단위, 20틱 = 1초)
    private static final long DISPLAY_INTERVAL_TICKS = 1L;

    // 코어별 체력 표시 작업을 관리하기 위한 Map (코어 UUID -> 작업)
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    /**
     * 코어가 플레이어에게 공격받았을 때 처리하는 이벤트 핸들러
     */
    @EventHandler
    public void onCoreHit(EntityDamageByEntityEvent event) {
        // 공격받은 엔티티가 코어용 Interaction 엔티티인지 체크
        if (!(event.getEntity() instanceof Interaction interaction)) return;
        // 공격한 주체가 플레이어인지 체크
        if (!(event.getDamager() instanceof Player player)) return;

        // 해당 Interaction이 등록된 코어인지 확인
        if (!CoreHealthManager.isCore(interaction)) {
            return;
        }

        // 코어 소속 팀 확인
        String coreTeam = CoreHealthManager.getTeamId(interaction);
        if (coreTeam == null) {
            return;
        }

        // 플레이어가 속한 팀과 코어 팀이 같은지 확인 (같으면 데미지 불가)
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var playerTeam = scoreboard.getEntryTeam(player.getName());
        if (playerTeam != null && playerTeam.getName().equals(coreTeam)) {
            // 같은 팀 코어 공격 시 경고 메시지 출력
            player.sendActionBar(Component.text("같은 팀의 코어에는 피해를 줄 수 없습니다!", NamedTextColor.RED));

            // 노트블럭 베이스 드럼 소리 재생 (음높이 0.5)
            player.playSound(
                    player.getLocation(),
                    "minecraft:block.note_block.bass",
                    SoundCategory.MASTER,
                    1f,
                    0.5f
            );

            event.setCancelled(true); // 데미지 취소
            return;
        }

        // 코어는 직접적인 데미지를 받지 않도록 이벤트 데미지 취소 처리
        event.setCancelled(true);

        double damage = event.getDamage();

        // 공격 성공 시 타격 사운드 재생
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f);

        // 코어 체력 감소 처리 및 변경된 체력 반환
        int newHealth = CoreHealthManager.damageCore(interaction, damage);

        // 체력이 0 이하가 되면 코어 파괴 처리
        if (newHealth <= 0) {
            stopCoreWatcher(interaction.getUniqueId());
            CoreHealthManager.destroyCore(interaction);
            return;
        }

        // 코어 체력 표시 작업 시작 (해당 코어에 대해)
        startCoreWatcher(interaction);
    }

    /**
     * 코어 체력 상태를 1초마다 주변 플레이어에게 보여주는 작업을 시작
     * @param interaction 코어 Interaction 엔티티
     */
    private void startCoreWatcher(Interaction interaction) {
        UUID id = interaction.getUniqueId();
        // 이미 작업이 실행 중이면 중복 실행 방지
        if (activeTasks.containsKey(id)) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // 코어가 유효하지 않거나 파괴되었으면 작업 종료
                if (!interaction.isValid() || !CoreHealthManager.isCore(interaction)) {
                    cancel();
                    activeTasks.remove(id);
                    return;
                }

                int health = CoreHealthManager.getHealth(interaction);
                Location coreLoc = interaction.getLocation();

                // 모든 온라인 플레이어 중 코어 반경 5블럭 이내인 플레이어에게 체력 표시
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().equals(coreLoc.getWorld())) continue;
                    if (player.getLocation().distance(coreLoc) <= DISPLAY_RADIUS) {
                        player.sendActionBar(Component.text("코어 체력: " + health, NamedTextColor.RED));
                    }
                }
            }
        };

        // 작업을 0틱 후 시작하여 20틱마다 반복 실행 (1초마다)
        task.runTaskTimer(Main.getInstance(), 0L, DISPLAY_INTERVAL_TICKS);
        activeTasks.put(id, task);
    }

    /**
     * 코어 체력 표시 작업을 중지하고 관리 목록에서 제거
     * @param id 코어 Interaction UUID
     */
    private void stopCoreWatcher(UUID id) {
        BukkitRunnable task = activeTasks.remove(id);
        if (task != null) task.cancel();
    }
}
