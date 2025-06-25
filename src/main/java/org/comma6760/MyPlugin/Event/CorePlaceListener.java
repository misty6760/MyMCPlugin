package org.comma6760.MyPlugin.Event;

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
        // 우클릭만 처리 (공기, 블럭)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) return;

        Material itemType = item.getType();

        // 빨간, 파란, 초록 배너만 처리
        if (!(itemType == Material.RED_BANNER
                || itemType == Material.BLUE_BANNER
                || itemType == Material.GREEN_BANNER)) {
            return;
        }

        // 아이템 메타 및 이름 확인
        if (!(item.hasItemMeta() && item.getItemMeta().hasDisplayName())) return;

        // 이름이 정확히 일치하는지 비교 (Component 객체 비교)
        Component expectedName = getComponent(itemType);
        Component actualName = Objects.requireNonNull(item.getItemMeta().displayName());
        if (!actualName.equals(expectedName)) return;

        // 설치 위치 결정
        Location placeLocation;

        if (event.getClickedBlock() != null) {
            // 클릭한 블럭 위 중앙
            placeLocation = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
        } else {
            // 클릭한 블럭이 없으면 플레이어 발 아래 블럭 위 중앙
            World world = player.getWorld();
            Block blockBelow = world.getBlockAt(
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY() - 1,
                    player.getLocation().getBlockZ()
            );

            // 공중 설치 불가 (발 아래 블럭이 Air면 설치 불가)
            if (blockBelow.getType().isAir()) {
                player.sendActionBar(Component.text("공중에서는 설치할 수 없습니다!", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            placeLocation = blockBelow.getLocation().add(0.5, 1, 0.5);
        }

        // 설치 위치 바로 아래 블럭이 통과 가능한 경우도 설치 불가로 처리
        if (placeLocation.clone().subtract(0, 1, 0).getBlock().isPassable()) {
            player.sendActionBar(Component.text("공중에는 설치할 수 없습니다!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // 엔더 크리스탈 설치
        // 엔더 크리스탈 설치
        EnderCrystal crystal = player.getWorld().spawn(placeLocation, EnderCrystal.class);
        crystal.setShowingBottom(false);

        // Interaction 엔티티 설치 (보호용)
        Location interactionLoc = placeLocation.clone().subtract(0, 0.005, 0);
        Interaction interaction = (Interaction) player.getWorld().spawnEntity(interactionLoc, EntityType.INTERACTION);
        interaction.setInteractionWidth(2.029f);
        interaction.setInteractionHeight(2.029f);
        interaction.setInvulnerable(true);

        // 🟢 여기서 코어 등록
        CoreHealthManager.registerCore(interaction, crystal, 10); // 초기 체력 10

        // 이벤트 취소 및 메시지 출력
        event.setCancelled(true);
        player.sendActionBar(Component.text("팀 코어가 설치되었습니다!").color(NamedTextColor.GREEN));

    }

    private static @NotNull Component getComponent(Material itemType) {
        if (itemType == Material.RED_BANNER) {
            return Component.text("빨간 팀 코어 설정")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false);
        } else if (itemType == Material.BLUE_BANNER) {
            return Component.text("파란 팀 코어 설정")
                    .color(NamedTextColor.BLUE)
                    .decoration(TextDecoration.ITALIC, false);
        } else {
            return Component.text("초록 팀 코어 설정")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false);
        }
    }
}
