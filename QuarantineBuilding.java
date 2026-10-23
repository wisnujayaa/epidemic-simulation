import greenfoot.*;

/**
 * QuarantineBuilding adalah base class abstract untuk semua bangunan
 * karantina dalam simulasi. Menyediakan logika kapasitas yang dipakai
 * bersama oleh RumahKarantina (untuk Person SUSCEPTIBLE yang isIsolating)
 * dan RumahIsolasi (untuk Person INFECTED yang isIsolating).
 *
 * Setiap subclass menentukan kapasitas maksimumnya sendiri lewat
 * getMaxCapacity(), sehingga base class tidak perlu tahu nilai spesifiknya.
 * Pola ini memungkinkan Person menggunakan tipe QuarantineBuilding secara
 * polymorphic — findNearestBuilding() dan goToQuarantineCentre() bekerja
 * untuk kedua jenis bangunan tanpa perlu tahu tipe konkretnya.
 *
 * Jaminan kapasitas: currentOccupants tidak pernah melebihi getMaxCapacity()
 * (dijaga oleh admitPeople()) dan tidak pernah negatif (dijaga oleh
 * dischargePeople()).
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public abstract class QuarantineBuilding extends Actor
{
    /** Jumlah penghuni yang sedang berada di bangunan ini saat ini.
     *  Selalu dalam rentang [0, getMaxCapacity()]. */
    private int currentOccupants = 0;

    /**
     * Mengembalikan kapasitas maksimum bangunan ini. Wajib diimplementasikan
     * oleh setiap subclass karena setiap jenis bangunan punya kapasitas
     * yang berbeda — RumahKarantina lebih kecil, RumahIsolasi lebih besar.
     *
     * @return jumlah maksimum penghuni yang bisa ditampung
     */
    public abstract int getMaxCapacity();

    /**
     * Mengecek apakah bangunan ini masih punya tempat untuk penghuni baru.
     *
     * @return true jika currentOccupants masih di bawah getMaxCapacity()
     */
    public boolean hasSpace() {
        return currentOccupants < getMaxCapacity();
    }

    /**
     * Menambah jumlah penghuni sebanyak satu. Pengaman hasSpace() memastikan
     * currentOccupants tidak pernah melebihi getMaxCapacity() meskipun
     * dipanggil berulang. Dipanggil oleh Person.goToQuarantineCentre()
     * saat Person tiba dan berhasil masuk.
     */
    public void admitPeople() {
        if (hasSpace()) {
            currentOccupants++;
        }
    }

    /**
     * Mengurangi jumlah penghuni sebanyak satu. Pengaman currentOccupants > 0
     * mencegah nilai negatif. Dipanggil oleh Person.resetQuarantineState()
     * saat Person keluar dari karantina — baik secara normal (wabah mereda)
     * maupun paksa (status berubah jadi INFECTED atau IMMUNE).
     */
    public void dischargePeople() {
        if (currentOccupants > 0) {
            currentOccupants--;
        }
    }

    /**
     * Mengembalikan jumlah penghuni saat ini. Dipakai oleh InfoPanel
     * untuk menampilkan statistik karantina secara real-time.
     *
     * @return jumlah penghuni yang sedang berada di bangunan ini
     */
    public int getCurrentOccupants() {
        return currentOccupants;
    }
}