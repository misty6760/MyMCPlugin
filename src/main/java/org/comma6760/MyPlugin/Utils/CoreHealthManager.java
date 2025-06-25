package org.comma6760.MyPlugin.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoreHealthManager {

    // Interaction(코어) UUID와 EnderCrystal 엔티티를 연결해 저장
    // EnderCrystal은 코어의 시각적 표시 및 효과에 사용됨
    private static final Map<UUID, EnderCrystal> coreMap = new HashMap<>();

    // Interaction UUID와 해당 코어의 현재 체력을 저장
    private static final Map<UUID, Integer> healthMap = new HashMap<>();

    // Interaction UUID와 코어가 속한 팀 ID를 저장 (예: "red", "blue", "green")
    private static final Map<UUID, String> teamMap = new HashMap<>();

    // 경고 메시지를 이미 보냈는지 추적
    private static final Map<UUID, Boolean> warnedMap = new HashMap<>();


    /**
     * 코어 엔티티와 관련 데이터를 새로 등록
     * @param interaction 코어 역할을 하는 Interaction 엔티티
     * @param crystal 시각 효과용 EnderCrystal 엔티티
     * @param initialHealth 코어의 시작 체력
     * @param teamId 코어가 속한 팀 식별자 문자열
     */
    public static void registerCore(Interaction interaction, EnderCrystal crystal, int initialHealth, String teamId) {
        UUID id = interaction.getUniqueId();
        coreMap.put(id, crystal);
        healthMap.put(id, initialHealth);
        teamMap.put(id, teamId);
        warnedMap.put(id, false); // 경고 여부 초기화
    }

    /**
     * 주어진 Interaction 엔티티가 현재 코어로 등록되어 있는지 확인
     * @param interaction 검사 대상 엔티티
     * @return 등록된 코어라면 true, 아니면 false
     */
    public static boolean isCore(Interaction interaction) {
        return coreMap.containsKey(interaction.getUniqueId());
    }

    /**
     * 코어의 현재 체력을 반환
     * @param interaction 체력을 조회할 코어 엔티티
     * @return 등록된 코어면 현재 체력, 아니면 0 반환
     */
    public static int getHealth(Interaction interaction) {
        return healthMap.getOrDefault(interaction.getUniqueId(), 0);
    }

    /**
     * 코어에 데미지를 적용하고 새 체력을 반환
     * @param interaction 데미지를 입힐 코어 엔티티
     * @param damage 입힐 데미지 양 (double 타입이지만 내부적으로 정수 처리)
     * @return 데미지 적용 후 남은 체력 (0 이하인 경우 0으로 고정)
     */
    public static int damageCore(Interaction interaction, double damage) {
        UUID id = interaction.getUniqueId();
        int currentHealth = healthMap.getOrDefault(id, 0);
        int newHealth = currentHealth - (int) damage;
        if (newHealth < 0) newHealth = 0;

        // 경고 메시지를 한 번만 출력
        if (currentHealth > 50 && newHealth <= 50 && !warnedMap.getOrDefault(id, false)) {
            String teamId = getTeamId(interaction);
            Bukkit.broadcast(Component.text("경고! " + teamId + " 팀의 코어 체력이 50 이하입니다!", NamedTextColor.RED));
            warnedMap.put(id, true); // 경고 상태 기록
        }

        healthMap.put(id, newHealth);
        return newHealth;
    }

    /**
     * 코어 파괴 시 호출: 이펙트와 사운드 재생, 관련 엔티티 제거, 저장된 데이터 삭제, 서버 방송
     * @param interaction 파괴할 코어 엔티티
     */
    public static void destroyCore(Interaction interaction) {
        UUID id = interaction.getUniqueId();
        EnderCrystal crystal = coreMap.get(id);
        Location loc = interaction.getLocation().add(0, 1, 0);  // 효과 위치는 코어 위쪽
        World world = loc.getWorld();

        if (world != null) {
            world.spawnParticle(Particle.EXPLOSION, loc, 1);   // 폭발 파티클
            world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f); // 폭발 소리
        }

        interaction.remove();  // 코어 Interaction 제거
        if (crystal != null && !crystal.isDead()) {
            crystal.remove();   // 관련 엔더 크리스탈 제거
        }

        // 내부 데이터에서 해당 코어 정보 삭제
        coreMap.remove(id);
        healthMap.remove(id);
        teamMap.remove(id);
        warnedMap.remove(id); // 경고 상태도 함께 삭제

        // 모든 플레이어에게 코어 파괴 메시지 방송
        Bukkit.broadcast(Component.text("코어가 파괴되었습니다!", NamedTextColor.GOLD));
    }

    /**
     * 코어가 속한 팀 식별자 반환
     * @param interaction 팀 ID를 조회할 코어 엔티티
     * @return 팀 ID 문자열 (없으면 null 반환)
     */
    public static String getTeamId(Interaction interaction) {
        return teamMap.get(interaction.getUniqueId());
    }

    /**
     * 특정 팀에 이미 코어가 존재하는지 확인
     * @param teamId 팀 식별자 ("red", "blue", "green" 등)
     * @return 이미 코어가 있으면 true, 없으면 false 반환
     */
    public static boolean hasCoreForTeam(String teamId) {
        return teamMap.containsValue(teamId);
    }
}
