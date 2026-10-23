import greenfoot.*;

/**
 * Child merepresentasikan anak dari seorang Person dalam simulasi.
 * Child selalu mengikuti parent-nya dengan bergerak menuju posisi
 * di sebelah kanan parent setiap cycle, sehingga secara visual
 * terlihat "menempel" pada parent-nya.
 *
 * Child mewarisi seluruh logika epidemi dari Person (infeksi,
 * karantina, vaksinasi, dst), dengan dua perbedaan utama:
 * ukuran visualnya lebih kecil (13x13 vs 20x20 pixel) dan masa
 * infeksinya setengah dari parent — mencerminkan daya tahan tubuh
 * anak yang berbeda dengan orang dewasa.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class Child extends Person
{
    /** Person yang menjadi parent dari Child ini. Dipakai oleh move()
     *  untuk menentukan arah gerak setiap cycle. Tidak pernah null
     *  setelah konstruksi, tapi parent bisa saja sudah tidak ada di
     *  world (dihapus saat reset) — selalu cek parent.getWorld() != null
     *  sebelum mengakses posisinya. */
    private Person parent;

    /**
     * Membuat Child baru yang terhubung ke parent-nya. Memanggil
     * super(isolating) agar Child mewarisi flag isIsolating dari
     * parent, sehingga perilaku karantinanya konsisten.
     *
     * @param isolating flag isolating yang diwariskan dari parent
     * @param parent    Person yang menjadi parent Child ini
     */
    public Child(boolean isolating, Person parent) {
        super(isolating);
        this.parent = parent;
    }

    /**
     * Mengganti gambar Child sesuai status infeksinya, diskalakan
     * ke 13x13 pixel (lebih kecil dari Person dewasa 20x20) agar
     * terlihat berbeda secara visual.
     *
     * @param status status infeksi yang menentukan gambar
     */
    @Override
    public void setScaleAndImage(InfectionStatus status) {
        GreenfootImage img = new GreenfootImage(status.getInfectionImage());
        img.scale(13, 13);
        setImage(img);
    }

    /**
     * Menggerakkan Child menuju posisi di sebelah kanan parent (offset
     * +16 pixel pada sumbu X) sejauh 4 pixel per cycle. Pengecekan
     * parent.getWorld() != null diperlukan karena parent bisa dihapus
     * dari world saat reset tanpa referensi Java-nya ikut jadi null.
     */
    @Override
    public void move() {
        if (parent != null && parent.getWorld() != null) {
            turnTowards(parent.getX() + 16, parent.getY());
            move(4);
        }
    }

    /**
     * Mengembalikan lama infeksi Child: setengah dari parent.
     * High risk: 300 cycle, normal: 150 cycle. Nilai ini dipakai
     * oleh Person.setStatus(INFECTED) untuk menginisialisasi
     * INFECTION_TIME saat Child tertular.
     *
     * @return lama infeksi dalam cycle
     */
    @Override
    public int getInfectionTime() {
        return super.getInfectionTime() / 2;
    }
}