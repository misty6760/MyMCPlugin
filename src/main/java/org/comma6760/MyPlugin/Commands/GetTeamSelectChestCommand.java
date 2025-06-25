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

/**
 * 이 클래스는 /select_team 명령어를 통해
 * 플레이어에게 팀 선택 GUI를 열 수 있는 특수 아이템을 지급하는 명령어 핸들러입니다.
 */
public class GetTeamSelectChestCommand implements CommandExecutor {

    /**
     * /select_team 명령어가 실행되었을 때 호출됩니다.
     *
     * @param sender 명령어를 실행한 주체 (플레이어 혹은 콘솔)
     * @param command 실행된 명령어 객체
     * @param label 명령어 라벨 (입력된 명령어 이름)
     * @param args 명령어에 전달된 추가 인수
     * @return 명령어 성공 여부
     */
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String @NotNull [] args) {

        // 플레이어가 아닌 경우 (콘솔 등)에는 명령어를 사용할 수 없음
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("이 명령어는 플레이어만 사용할 수 있습니다.")
                    .color(NamedTextColor.RED));
            return true;
        }

        // 체스트 아이템 생성
        ItemStack specialChest = new ItemStack(Material.CHEST);

        // 아이템 메타 정보 가져오기
        ItemMeta meta = specialChest.getItemMeta();
        if (meta == null) return false;

        // 체스트의 이름 설정 (텍스트 꾸미기: 골드색, 볼드, 이탤릭 해제)
        meta.displayName(
                Component.text("팀 선택하기")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true)
        );

        // 설명(lore) 설정 - 회색 텍스트, 이탤릭 없음
        meta.lore(List.of(
                Component.text("팀을 선택하려면 우클릭을 하세요")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        // 설정된 메타를 아이템에 적용
        specialChest.setItemMeta(meta);

        // 아이템을 플레이어 인벤토리에 추가
        player.getInventory().addItem(specialChest);

        // 안내 메시지 출력
        player.sendMessage(Component.text("팀 선택 상자를 받았습니다!")
                .color(NamedTextColor.GREEN));

        return true;
    }
}
