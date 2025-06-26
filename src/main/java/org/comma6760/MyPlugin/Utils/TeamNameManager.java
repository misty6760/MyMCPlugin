package org.comma6760.MyPlugin.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 팀 이름을 저장하고 관리하는 클래스
 * 초기 기본 이름을 세팅하며,
 * 플레이어가 명령어로 팀 이름을 변경할 수 있도록 지원함
 */
public class TeamNameManager {

    private static final Map<String, String> teamNames = new HashMap<>();

    static {
        // 기본 팀 이름 설정
        teamNames.put("red", "빨강 팀");
        teamNames.put("blue", "파랑 팀");
        teamNames.put("green", "초록 팀");
    }

    /**
     * 팀 ID로 팀 이름 변경
     * @param teamId 팀 식별자
     * @param newName 새 팀 이름
     */
    public static void setTeamName(String teamId, String newName) {
        teamNames.put(teamId, newName);
    }

    /**
     * 팀 ID로부터 팀 이름 조회
     * @param teamId 팀 식별자
     * @return 저장된 팀 이름, 없으면 teamId 반환
     */
    public static String getTeamName(String teamId) {
        return teamNames.getOrDefault(teamId, teamId);
    }
}
