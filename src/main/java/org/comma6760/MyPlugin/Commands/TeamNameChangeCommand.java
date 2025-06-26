package org.comma6760.MyPlugin.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamNameChangeCommand implements CommandExecutor, TabCompleter {

    private static final List<String> TEAMS = Arrays.asList("red", "blue", "green");

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String @NotNull [] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("사용법: /team_name_change <red|blue|green> <새 이름>");
            return true;
        }

        String teamId = args[0].toLowerCase();
        if (!TEAMS.contains(teamId)) {
            sender.sendMessage("팀은 red, blue, green 중 하나여야 합니다.");
            return true;
        }

        // 나머지 인자들 합쳐서 팀 이름으로 만듦 (띄어쓰기 포함 가능)
        StringBuilder newNameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            newNameBuilder.append(args[i]).append(" ");
        }
        String newName = newNameBuilder.toString().trim();

        // 실제 팀 이름 변경 호출 (TeamNameManager 활용 등)
        // TeamNameManager.setTeamName(teamId, newName);

        sender.sendMessage(teamId + " 팀의 이름을 '" + newName + "'(으)로 변경했습니다.");

        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String @NotNull [] args) {

        // 첫 번째 인자 입력 중일 때만 팀 목록 자동완성
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String team : TEAMS) {
                if (team.startsWith(partial)) {
                    completions.add(team);
                }
            }
            return completions;
        }

        // 두 번째 인자 이후는 자동완성 없음 (팀 이름 자유 입력)
        return List.of();
    }
}
