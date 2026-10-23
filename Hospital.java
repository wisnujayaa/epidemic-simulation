import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Hospital adalah Actor statis yang merawat Person berstatus Infected
 * yang berada di sekitarnya, dengan kapasitas terbatas. Setiap Hospital
 * melacak jumlah pasiennya sendiri secara independen (currentOccupants
 * bukan static), sehingga beberapa Hospital di world masing-masing
 * punya kapasitas dan keterisian sendiri-sendiri.
 *
 * Logika kapan Person dianggap "dirawat" dan kapan harus admit/discharge
 * sepenuhnya ditentukan oleh Person (lewat isTreatmentPeriod() dan
 * treatmentProcess()); Hospital di sini hanya menyediakan dan menjaga
 * data kapasitasnya.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class Hospital extends Actor
{
    /** Jumlah maksimum pasien yang bisa ditampung satu Hospital secara bersamaan. */
    private static final int MAX_CAPACITY = 30;
    /** Jumlah pasien yang sedang dirawat di Hospital ini saat ini. */
    private int currentOccupants = 0;

    /**
     * Membuat Hospital baru dan memasang gambar rumah sakit yang sudah
     * diskalakan ke ukuran tampil di world.
     */
    public Hospital() {
        GreenfootImage img = new GreenfootImage("hospital.png");
        img.scale(100, 100);
        setImage(img);
    }
    
    /**
     * Act - do whatever the Hospital wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
    }

    /**
     * Mengecek apakah Hospital ini masih punya tempat untuk pasien baru.
     *
     * @return true jika currentOccupants masih di bawah MAX_CAPACITY
     */
    public boolean hasSpace() {
        return currentOccupants < MAX_CAPACITY;
    }

    /**
     * Menambah jumlah pasien yang dirawat sebanyak satu, hanya jika
     * masih ada tempat tersedia. Pengaman hasSpace() di sini memastikan
     * currentOccupants tidak akan pernah melebihi MAX_CAPACITY meskipun
     * dipanggil berulang.
     */
    public void admitPatient() {
        if (hasSpace()) {
            currentOccupants++;
        }
    }

    /**
     * Mengurangi jumlah pasien yang dirawat sebanyak satu, hanya jika
     * masih ada pasien (currentOccupants lebih besar dari nol).
     * Pengaman ini mencegah currentOccupants menjadi negatif, yang
     * tidak bermakna secara logis (rumah sakit tidak bisa punya
     * "minus satu pasien").
     */
    public void dischargePatient() {
        if (currentOccupants>0) {
            currentOccupants--;
        }
    }
        
}