import greenfoot.*;

/**
 * RumahKarantina adalah tempat karantina untuk Person yang berstatus
 * SUSCEPTIBLE dan isIsolating, saat kondisi wabah memenuhi shouldQuarantine().
 * Person yang berhasil masuk akan diam di sini sampai wabah mereda
 * (shouldQuarantine() kembali false), melindungi diri dari kemungkinan
 * tertular selama wabah aktif.
 *
 * Kapasitasnya (30) lebih besar dari RumahIsolasi karena target penghuninya
 * adalah seluruh populasi SUSCEPTIBLE yang isIsolating — yang jumlahnya
 * jauh lebih banyak dibanding INFECTED yang isIsolating.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class RumahKarantina extends QuarantineBuilding
{
    /** Kapasitas maksimum RumahKarantina. Lebih besar dari RumahIsolasi
     *  karena diperuntukkan untuk seluruh populasi SUSCEPTIBLE yang
     *  isIsolating saat wabah aktif. */
    private static final int MAX_CAPACITY = 30;

    /**
     * Membuat RumahKarantina baru dengan gambar yang diskalakan ke 50x50 pixel.
     */
    public RumahKarantina() {
        GreenfootImage img = new GreenfootImage("rumahkarantina.png");
        img.scale(50, 50);
        setImage(img);
    }

    /**
     * Mengembalikan kapasitas maksimum RumahKarantina.
     *
     * @return 30
     */
    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }
}