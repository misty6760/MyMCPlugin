package org.comma6760.MyPlugin.Event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.SoundCategory;

import java.util.Collections;
import java.util.List;

/**
 * 플레이어가 특정 상자 아이템을 우클릭하면 가상 GUI 인벤토리를 열고
 * 팀을 선택할 수 있게 해주는 리스너 클래스입니다.
 */
public class ChestDetectRightClickListener implements Listener {

    /**
     * 플레이어가 아이템을 우클릭했을 때 실행되는 이벤트입니다.
     * 이 이벤트는 팀 선택 GUI를 여는 트리거 역할을 합니다.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 우클릭한 경우만 처리 (공중 또는 블록)
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();

            // 아이템이 상자인 경우만 처리
            if (item.getType() == Material.CHEST) {
                ItemMeta meta = item.getItemMeta();

                // 메타 정보가 존재하고 이름 + 설명이 있는 경우
                if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                    // 예상되는 아이템 이름 (스타일 포함)
                    Component displayName = meta.displayName();
                    Component expectedName = Component.text("팀 선택하기")
                            .color(NamedTextColor.GOLD)
                            .decoration(TextDecoration.ITALIC, false)
                            .decoration(TextDecoration.BOLD, true);

                    // 예상되는 lore 내용
                    List<Component> lore = meta.lore();
                    Component expectedLoreLine = Component.text("팀을 선택하려면 우클릭을 하세요")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false);

                    // 이름과 lore가 모두 예상과 일치할 때만 실행
                    if (displayName != null && displayName.equals(expectedName)
                            && lore != null && !lore.isEmpty()
                            && lore.getFirst().equals(expectedLoreLine)) {

                        // 아이템의 기본 행동(예: 설치 등) 방지
                        event.setCancelled(true);

                        // 새로운 가상 인벤토리 생성 (3행 27칸, 제목 포함)
                        Inventory virtualChest = Bukkit.createInventory(
                                null,
                                27,
                                Component.text("팀 선택하기").color(NamedTextColor.GOLD)
                        );

                        // --- 염료 아이템 생성 및 설정 (각 팀용) ---
                        ItemStack redDye = new ItemStack(Material.RED_DYE);
                        ItemMeta redMeta = redDye.getItemMeta();
                        redMeta.displayName(Component.text("빨간 팀").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        redDye.setItemMeta(redMeta);

                        ItemStack blueDye = new ItemStack(Material.BLUE_DYE);
                        ItemMeta blueMeta = blueDye.getItemMeta();
                        blueMeta.displayName(Component.text("파란 팀").color(NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
                        blueDye.setItemMeta(blueMeta);

                        ItemStack greenDye = new ItemStack(Material.GREEN_DYE);
                        ItemMeta greenMeta = greenDye.getItemMeta();
                        greenMeta.displayName(Component.text("초록 팀").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                        greenDye.setItemMeta(greenMeta);

                        // --- GUI 배경용 블랙 유리판 설정 (장식 목적) ---
                        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                        ItemMeta blackMeta = blackPane.getItemMeta();
                        blackMeta.displayName(Component.empty()); // 이름 숨기기
                        blackMeta.lore(Collections.emptyList()); // 설명 제거
                        blackMeta.addItemFlags(
                                ItemFlag.HIDE_ATTRIBUTES,
                                ItemFlag.HIDE_ENCHANTS,
                                ItemFlag.HIDE_UNBREAKABLE
                        );
                        blackPane.setItemMeta(blackMeta);

                        // GUI 채우기: 11,13,15 슬롯 제외한 나머지에 검정 유리
                        for (int i = 0; i < virtualChest.getSize(); i++) {
                            if (i != 11 && i != 13 && i != 15) {
                                virtualChest.setItem(i, blackPane);
                            }
                        }

                        // 각 팀 버튼 배치 (가운데 정렬)
                        virtualChest.setItem(11, redDye);
                        virtualChest.setItem(13, blueDye);
                        virtualChest.setItem(15, greenDye);

                        // GUI 열기
                        player.openInventory(virtualChest);
                    }
                }
            }
        }
    }

    /**
     * 팀 선택 GUI에서 플레이어가 아이템을 클릭했을 때 실행되는 이벤트입니다.
     * 팀 변경 및 사운드, 메시지 처리 등을 담당합니다.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // GUI 제목 확인 (정확히 일치해야 함)
        if (!event.getView().title().equals(Component.text("팀 선택하기").color(NamedTextColor.GOLD))) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        Material type = clicked.getType();

        // 유리판 클릭은 무시
        if (type == Material.BLACK_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        // 클릭된 아이템에 따라 팀 결정
        String targetTeam;
        NamedTextColor msgColor;
        String joinMsg;

        if (type == Material.RED_DYE) {
            targetTeam = "red";
            msgColor = NamedTextColor.RED;
            joinMsg = "빨간 팀에 참가했습니다!";
        } else if (type == Material.BLUE_DYE) {
            targetTeam = "blue";
            msgColor = NamedTextColor.BLUE;
            joinMsg = "파란 팀에 참가했습니다!";
        } else if (type == Material.GREEN_DYE) {
            targetTeam = "green";
            msgColor = NamedTextColor.GREEN;
            joinMsg = "초록 팀에 참가했습니다!";
        } else {
            event.setCancelled(true); // 그 외 클릭은 방지
            return;
        }

        event.setCancelled(true); // 아이템 이동 방지

        // 플레이어가 이미 해당 팀에 속해 있는지 확인
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var playerTeam = scoreboard.getEntryTeam(player.getName());
        if (playerTeam != null && playerTeam.getName().equals(targetTeam)) {
            player.sendMessage(Component.text("이미 해당 팀에 속해 있습니다!", NamedTextColor.GRAY));
            player.playSound(
                    player.getLocation(),
                    "minecraft:block.note_block.bass",
                    SoundCategory.MASTER,
                    1f,
                    0.5f
            );
            return;
        }

        // 기존 팀에서 제거
        for (var team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }

        player.closeInventory(); // GUI 닫기

        // 실제 팀 추가 명령어 실행
        player.performCommand("team join " + targetTeam);

        // 사운드 및 메시지 출력 (성공 시)
        player.playSound(
                player.getLocation(),
                "minecraft:entity.player.levelup",
                SoundCategory.MASTER,
                1f,
                2f
        );
        player.sendMessage(Component.text(joinMsg).color(msgColor));
    }
}
