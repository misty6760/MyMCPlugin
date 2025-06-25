package org.comma6760.MyPlugin.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetTeamSelectChestCommand implements CommandExecutor {

    /**
     * /select_team 명령어 실행 시 호출됨
     * 플레이어에게 "팀 선택하기" 이름과 설명이 붙은 특별한 체스트 아이템을 지급함
     */
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String @NotNull [] args) {
        // 명령어 실행자가 플레이어인지 확인, 콘솔 등은 불가
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("이 명령어는 플레이어만 사용할 수 있습니다.").color(NamedTextColor.RED));
            return true;
        }

        // 체스트 아이템 생성
        ItemStack specialChest = new ItemStack(Material.CHEST);
        ItemMeta meta = specialChest.getItemMeta();

        // 메타데이터가 없으면 실패 처리
        if (meta == null) return false;

        // 체스트 이름 설정: "팀 선택하기" (골드색, 볼드, 이탤릭 해제)
        meta.displayName(
                Component.text("팀 선택하기")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true)
        );

        // 설명(로어) 설정: "팀을 선택하려면 우클릭을 하세요" (회색, 이탤릭 해제)
        meta.lore(List.of(
                Component.text("팀을 선택하려면 우클릭을 하세요")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        // 아이템에 메타 적용
        specialChest.setItemMeta(meta);

        // 플레이어 인벤토리에 아이템 추가
        player.getInventory().addItem(specialChest);

        // 지급 완료 메시지 출력 (초록색)
        player.sendMessage(Component.text("팀 선택 상자를 받았습니다!").color(NamedTextColor.GREEN));

        return true;
    }
}
