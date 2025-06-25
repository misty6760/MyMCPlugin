package org.comma6760.MyPlugin.Event.Cores;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.comma6760.MyPlugin.Utils.CoreHealthManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CorePlaceListener implements Listener {

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        // 플레이어가 오른쪽 클릭(공중 또는 블록)했는지 확인
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 손에 든 아이템이 없으면 처리 중단
        if (item.getType() == Material.AIR) return;

        Material itemType = item.getType();

        // 코어 설치용 배너인지 확인 (빨강, 파랑, 초록 배너만 허용)
        if (!(itemType == Material.RED_BANNER || itemType == Material.BLUE_BANNER || itemType == Material.GREEN_BANNER)) {
            return;
        }

        // 아이템에 커스텀 이름이 없는 경우 무시
        if (!(item.hasItemMeta() && item.getItemMeta().hasDisplayName())) return;

        // 배너별로 예상되는 이름과 실제 이름이 일치하는지 확인하여 정품인지 판별
        Component expectedName = getComponent(itemType);
        Component actualName = Objects.requireNonNull(item.getItemMeta().displayName());
        if (!actualName.equals(expectedName)) return;

        // 배너 유형에 따른 팀 ID 결정
        String teamId;
        switch (itemType) {
            case RED_BANNER -> teamId = "red";
            case BLUE_BANNER -> teamId = "blue";
            case GREEN_BANNER -> teamId = "green";
            default -> throw new IllegalStateException("알 수 없는 배너 타입: " + itemType);
        }

        // 해당 팀에 이미 코어가 설치되어 있으면 설치 불가 처리
        if (CoreHealthManager.hasCoreForTeam(teamId)) {
            // 팀 이름(한국어) 매핑
            String teamName = switch (teamId) {
                case "red" -> "빨간";
                case "blue" -> "파란";
                case "green" -> "초록";
                default -> "";
            };

            // 플레이어에게 중복 설치 불가 메시지 출력
            player.sendActionBar(Component.text("이미 " + teamName + " 팀의 코어가 설치되어 있습니다!", NamedTextColor.RED));

            // 노트블럭 베이스드럼 소리 재생 (음높이 0.5)
            player.playSound(
                    player.getLocation(),
                    org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASEDRUM,
                    1.0f,      // 볼륨
                    12 / 24f   // 음높이 0.5 (12번째 음계)
            );

            event.setCancelled(true); // 이벤트 취소
            return;
        }

        Location placeLocation;
        if (event.getClickedBlock() != null) {
            // 클릭한 블록 위 1칸 중앙에 설치 위치 지정
            placeLocation = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
        } else {
            World world = player.getWorld();
            Block blockBelow = world.getBlockAt(
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY() - 1,
                    player.getLocation().getBlockZ()
            );

            // 발 밑이 공기인 경우(공중) 설치 불가 처리
            if (blockBelow.getType().isAir()) {
                player.sendActionBar(Component.text("공중에서는 설치할 수 없습니다!", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            // 블록 위 1칸 중앙에 설치 위치 지정
            placeLocation = blockBelow.getLocation().add(0.5, 1, 0.5);
        }

        // 설치 위치 바로 아래 블록이 통과 가능한 블록일 경우 설치 불가 처리
        if (placeLocation.clone().subtract(0, 1, 0).getBlock().isPassable()) {
            player.sendActionBar(Component.text("공중에는 설치할 수 없습니다!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // 엔더 크리스탈 생성 (코어 시각 효과)
        EnderCrystal crystal = player.getWorld().spawn(placeLocation, EnderCrystal.class);
        crystal.setShowingBottom(false); // 엔더 크리스탈 바닥 제거

        // Interaction 엔티티 생성 (코어 판정을 위한 실제 엔티티)
        Location interactionLoc = placeLocation.clone().subtract(0, 0.005, 0);
        Interaction interaction = (Interaction) player.getWorld().spawnEntity(interactionLoc, EntityType.INTERACTION);
        interaction.setInteractionWidth(2.029f);   // 상호작용 영역 너비
        interaction.setInteractionHeight(2.029f);  // 상호작용 영역 높이
        interaction.setInvulnerable(true);          // 공격 불가능 설정

        // 코어로 등록 (체력 초기값 10)
        CoreHealthManager.registerCore(interaction, crystal, 100, teamId);

        event.setCancelled(true); // 블록 설치 이벤트 취소 (중복 처리 방지)

        // 설치 성공 메시지 출력 (초록색)
        player.sendActionBar(Component.text("팀 코어가 설치되었습니다!").color(NamedTextColor.GREEN));
    }

    /**
     * 배너 종류별로 코어 설치용 이름(컴포넌트)를 반환
     * @param itemType 배너 머티리얼 종류
     * @return 해당 배너용 코어 이름 컴포넌트
     */
    private static Component getComponent(Material itemType) {
        return switch (itemType) {
            case RED_BANNER -> Component.text("빨간 팀 코어 설정").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
            case BLUE_BANNER -> Component.text("파란 팀 코어 설정").color(NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false);
            case GREEN_BANNER -> Component.text("초록 팀 코어 설정").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
            default -> Component.empty();
        };
    }
}
