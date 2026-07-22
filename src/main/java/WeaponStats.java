public record WeaponStats(int streamCount, double spreadWidth, double cooldownInterval, int bulletDamage) {
    public WeaponStats {
        if (streamCount <= 0) {
            throw new IllegalArgumentException("streamCount must be greater than 0");
        }
        if (spreadWidth < 0) {
            throw new IllegalArgumentException("spreadWidth must be greater than or equal to 0");
        }
        if (cooldownInterval <= 0) {
            throw new IllegalArgumentException("cooldownInterval must be greater than 0");
        }
        if (bulletDamage <= 0) {
            throw new IllegalArgumentException("bulletDamage must be greater than 0");
        }
    }
}
