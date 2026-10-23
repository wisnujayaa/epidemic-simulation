import greenfoot.*;

/**
 * RumahIsolasi adalah tempat isolasi khusus untuk Person yang berstatus
 * INFECTED dan isIsolating, saat kondisi wabah memenuhi shouldQuarantine().
 * Berbeda dari Hospital yang aktif mempercepat penyembuhan, RumahIsolasi
 * hanya menahan Person di satu tempat — mencegah mereka berkeliaran dan
 * menulari orang lain.
 *
 * Kapasitasnya (10) lebih kecil dari RumahKarantina (30) karena jumlah
 * orang INFECTED yang isIsolating pada satu waktu relatif lebih sedikit
 * dibanding SUSCEPTIBLE.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class RumahIsolasi extends QuarantineBuilding
{
    /** Kapasitas maksimum RumahIsolasi. Lebih kecil dari RumahKarantina
     *  karena diperuntukkan hanya untuk Person INFECTED yang isIsolating. */
    private static final int MAX_CAPACITY = 10;

    /**
     * Membuat RumahIsolasi baru dengan gambar yang diskalakan ke 50x50 pixel.
     */
    public RumahIsolasi() {
        GreenfootImage img = new GreenfootImage("rumahisolasi.png");
        img.scale(50, 50);
        setImage(img);
    }

    /**
     * Mengembalikan kapasitas maksimum RumahIsolasi.
     *
     * @return 10
     */
    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }
}