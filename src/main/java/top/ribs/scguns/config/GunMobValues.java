package top.ribs.scguns.config;

import net.minecraft.world.Difficulty;
import top.ribs.scguns.Config;

public class GunMobValues {
    public static boolean enabled = true;
    public static double gunnerSpawnChance = 0.3;
    public static boolean scaleToDifficulty = true;
    public static boolean elitesEnabled = true;
    public static double eliteChance = 0.2;

    public static void init() {
        enabled = Config.COMMON.gunnerMobs.gunnerMobSpawning.get();
        gunnerSpawnChance = Config.COMMON.gunnerMobs.gunnerSpawnChance.get();
        scaleToDifficulty = Config.COMMON.gunnerMobs.scaleToDifficulty.get();
        elitesEnabled = Config.COMMON.gunnerMobs.eliteSpawning.get();
        eliteChance = Config.COMMON.gunnerMobs.eliteChance.get();
    }

    public static double getGunnerSpawnChance(Difficulty difficulty) {
        return clampChance(scaleToDifficulty ? gunnerSpawnChance * getDifficultyMultiplier(difficulty) : gunnerSpawnChance);
    }

    public static double getThematicGunnerSpawnChance(double baseChance, Difficulty difficulty) {
        return clampChance(baseChance * getGunnerSpawnChance(difficulty));
    }

    public static double getEliteChance(Difficulty difficulty) {
        return clampChance(scaleToDifficulty ? eliteChance * getDifficultyMultiplier(difficulty) : eliteChance);
    }

    private static double getDifficultyMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0.5;
            case EASY -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.5;
        };
    }

    private static double clampChance(double chance) {
        return Math.max(0.0D, Math.min(1.0D, chance));
    }
}
