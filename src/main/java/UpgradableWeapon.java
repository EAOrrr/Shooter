public interface UpgradableWeapon extends Weapon {
    WeaponStats getStats();

    void applyStats(WeaponStats newStats);
}
