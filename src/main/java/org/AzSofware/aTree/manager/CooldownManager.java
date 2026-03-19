package org.AzSofware.aTree.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player cooldowns for TreeCapitator.
 * Uses System.currentTimeMillis() for accuracy.
 */
public class CooldownManager {

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    /**
     * Check if a player is on cooldown.
     *
     * @param uuid     Player UUID
     * @param seconds  Cooldown duration in seconds
     * @return true if on cooldown
     */
    public boolean isOnCooldown(UUID uuid, int seconds) {
        Long last = cooldowns.get(uuid);
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < (seconds * 1000L);
    }

    /**
     * Get remaining cooldown in seconds (decimal).
     *
     * @param uuid     Player UUID
     * @param seconds  Cooldown duration in seconds
     * @return remaining seconds, 0 if no cooldown
     */
    public double getRemainingSeconds(UUID uuid, int seconds) {
        Long last = cooldowns.get(uuid);
        if (last == null) return 0;
        long elapsed = System.currentTimeMillis() - last;
        long totalMs = seconds * 1000L;
        double remaining = (totalMs - elapsed) / 1000.0;
        return Math.max(0, remaining);
    }

    /**
     * Apply cooldown for a player (set timestamp to now).
     */
    public void applyCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    /**
     * Remove cooldown entry (cleanup on logout).
     */
    public void remove(UUID uuid) {
        cooldowns.remove(uuid);
    }

    /**
     * Clear all cooldowns.
     */
    public void clear() {
        cooldowns.clear();
    }
}
