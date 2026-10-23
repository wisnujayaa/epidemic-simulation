import greenfoot.*;
import java.util.List;

/**
 * MyWorld adalah world tempat seluruh simulasi epidemi berlangsung.
 * Menjalankan eksperimen 5 run secara berurutan dengan persentase
 * isolating yang berbeda (0%, 25%, 50%, 75%, 90%), menggambar kurva
 * infected tiap run ke background dengan warna berbeda untuk perbandingan
 * visual, dan mengelola spawn dinamis AmbulanceSmartPathfinding sesuai
 * skala wabah.
 *
 * Setiap run dimulai dengan populate() yang membuat seluruh populasi
 * dari nol, dan berakhir saat xOffset mencapai lebar world (semua
 * pixel horizontal sudah terisi kurva). Run berikutnya dimulai otomatis
 * lewat nextRun() → reset() → populate().
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class MyWorld extends World
{
    /** Jumlah Person yang dibuat per run, tidak termasuk pasien nol. */
    private static final int NUMBER_OF_PEOPLE = 300;
    /** Jumlah Hospital yang ditempatkan per run. */
    private static final int NUMBER_OF_HOSPITAL = 3;
    /** Jumlah Ambulance zigzag yang ditempatkan di awal tiap run.
     *  AmbulanceSmartPathfinding tidak termasuk — spawn dinamis. */
    private static final int NUMBER_OF_AMBULANCE = 5;
    /** Jumlah Vaccinator yang ditempatkan per run. */
    private static final int NUMBER_OF_VACCINATOR = 3;
    /** Jumlah RumahKarantina (untuk SUSCEPTIBLE) yang ditempatkan per run. */
    private static final int NUMBER_OF_RUMAHKARANTINA = 5;
    /** Jumlah RumahIsolasi (untuk INFECTED) yang ditempatkan per run. */
    private static final int NUMBER_OF_RUMAHISOLASI = 5;

    /** Persentase isolating untuk masing-masing run, dijalankan berurutan. */
    private static final int[] PERCENT_ISOLATING = {0, 25, 50, 75, 90};

    /** Warna kurva untuk masing-masing run, berurutan sesuai PERCENT_ISOLATING. */
    private static final Color[] COLORS = {
        Color.DARK_GRAY, Color.GRAY, Color.BLUE, Color.MAGENTA, Color.RED
    };

    /** Skala vertikal kurva infected di background (pixel per orang). */
    private static final int Y_SCALE = 2;

    /** Indeks run yang sedang berjalan (0-based). */
    private int run = 0;

    /** Posisi horizontal pixel berikutnya yang akan digambar kurva.
     *  Di-reset ke 0 setiap run baru. Run berakhir saat xOffset >= getWidth(). */
    private int xOffset = 0;

    /** Jumlah Infected tertinggi yang pernah tercatat di run saat ini.
     *  Di-reset ke 0 setiap run baru lewat reset(). */
    private int maxInfected = 0;

    /** Jumlah AmbulanceSmartPathfinding yang sudah di-spawn di run ini.
     *  Dipakai oleh checkAndSpawnAmbulance() untuk menentukan threshold
     *  spawn berikutnya. Di-reset ke 0 setiap run baru. */
    private int ambulanceSpawned = 0;

    /** Referensi ke InfoPanel yang ditampilkan di pojok kiri atas.
     *  Di-set ulang setiap populate() karena removeObjects() menghapusnya. */
    private InfoPanel infoPanel;

    /**
     * Membuat world berukuran 1000x600 pixel dan memulai run pertama.
     * Background dipasang dengan transparansi 200 agar kurva perbandingan
     * yang digambar ke background tetap terlihat di bawah Actor.
     */
    public MyWorld() {
        super(1000, 600, 1, true);
        Person.resetNumInfected();
        populate(PERCENT_ISOLATING[run]);
        GreenfootImage bg = new GreenfootImage("cell.jpg");
        bg.setTransparency(200);
        setBackground(bg);
    }

    /**
     * Mengembalikan total populasi termasuk pasien nol (NUMBER_OF_PEOPLE + 1).
     * Dipakai oleh InfoPanel untuk menghitung persentase immune secara akurat.
     *
     * @return total jumlah Person di world per run
     */
    public static int getNumberOfPeople() {
        return NUMBER_OF_PEOPLE + 1;
    }

    /**
     * Dipanggil otomatis tiap cycle. Guard di awal mencegah eksekusi
     * setelah run terakhir selesai (saat Greenfoot.stop() belum sempat
     * menghentikan cycle yang sedang berjalan). Urutan: update statistik,
     * cek apakah run selesai, spawn ambulance, update InfoPanel.
     */
    public void act() {
        if (run >= PERCENT_ISOLATING.length) return;

        displayInfo();

        if (xOffset >= getWidth()) {
            nextRun();
            return;
        }

        checkAndSpawnAmbulance();
        infoPanel.update(run + 1, PERCENT_ISOLATING.length, maxInfected, PERCENT_ISOLATING[run]);
    }

    /**
     * Memperbarui maxInfected dan menggambar satu titik kurva infected
     * ke background di posisi xOffset saat ini dengan warna run ini.
     * Label "% isolating" juga digambar ulang setiap cycle agar tetap
     * terlihat meskipun kurva lain menimpa area yang sama.
     */
    public void displayInfo() {
        if (Person.getNumInfected() > maxInfected) {
            maxInfected = Person.getNumInfected();
        }
        plotValue(Person.getNumInfected());
    }

    /**
     * Menggambar satu kolom kurva infected ke background pada posisi
     * xOffset saat ini, lalu menggeser xOffset satu pixel ke kanan.
     * Tinggi kolom proporsional terhadap jumlah Infected (Y_SCALE pixel
     * per orang, dihitung dari bawah).
     *
     * @param value jumlah Infected saat ini
     */
    public void plotValue(int value) {
        int yOffset = getHeight() - value * Y_SCALE;
        getBackground().setColor(COLORS[run]);
        getBackground().drawString("% isolating: " + PERCENT_ISOLATING[run] + "%",
                                   700, 20 + 25 * run);
        getBackground().drawLine(xOffset, yOffset, xOffset, yOffset - 4);
        xOffset++;
    }

    /**
     * Menambah satu AmbulanceSmartPathfinding setiap kali numInfected
     * naik kelipatan 50. Menggunakan ambulanceSpawned sebagai penghitung
     * agar threshold tidak dipicu berulang di cycle yang sama.
     * Ambulance ditempatkan di posisi acak di world.
     */
    public void checkAndSpawnAmbulance() {
        if (Person.getNumInfected() >= (ambulanceSpawned + 1) * 50) {
            int x = Greenfoot.getRandomNumber(getWidth());
            int y = Greenfoot.getRandomNumber(getHeight());
            addObject(new AmbulanceSmartPathfinding(), x, y);
            ambulanceSpawned++;
        }
    }

    /**
     * Melanjutkan ke run berikutnya. Jika semua run sudah selesai,
     * hapus semua Actor dan hentikan simulasi. Jika belum, reset state
     * run saat ini dan mulai run baru dengan persentase isolating berikutnya.
     */
    public void nextRun() {
        run++;
        if (run == PERCENT_ISOLATING.length) {
            removeObjects(getObjects(Actor.class));
            Greenfoot.stop();
        } else {
            reset();
        }
    }

    /**
     * Mereset state run saat ini dan memulai populasi baru. Urutan
     * penting: xOffset dan ambulanceSpawned di-reset sebelum populate()
     * agar cycle pertama run baru tidak langsung trigger nextRun()
     * atau spawn ambulance yang tidak seharusnya. maxInfected di-reset
     * setelah populate() agar InfoPanel menampilkan nilai yang akurat.
     */
    public void reset() {
        xOffset = 0;
        ambulanceSpawned = 0;
        maxInfected = 0;
        populate(PERCENT_ISOLATING[run]);
    }

    /**
     * Mengisi world dengan seluruh populasi awal satu run:
     * - Satu pasien nol di tengah world, langsung berstatus INFECTED
     *   lewat setStatus() (bukan infect() yang probabilistik)
     * - NUMBER_OF_PEOPLE Person acak, isIsolating ditentukan probabilistik
     *   sesuai percentIsolating
     * - NUMBER_OF_HOSPITAL Hospital di posisi acak
     * - NUMBER_OF_AMBULANCE-1 Ambulance zigzag (horizontal atau vertikal,
     *   dipilih acak 50/50) di posisi acak
     * - EpidemicChart di pojok kiri bawah
     * - NUMBER_OF_VACCINATOR Vaccinator di posisi acak
     * - NUMBER_OF_RUMAHKARANTINA RumahKarantina di posisi acak
     * - NUMBER_OF_RUMAHISOLASI RumahIsolasi di posisi acak
     * - InfoPanel di pojok kiri atas
     *
     * removeObjects() dipanggil di awal untuk membersihkan sisa run
     * sebelumnya, termasuk infoPanel lama.
     *
     * @param percentIsolating persentase Person yang isIsolating (0-100)
     */
    public void populate(int percentIsolating) {
        removeObjects(getObjects(Actor.class));
        Person.resetNumInfected();

        Person pasienNol = new Person(false);
        pasienNol.setStatus(InfectionStatus.INFECTED);
        addObject(pasienNol, getWidth() / 2, getHeight() / 2);

        for (int i = 0; i < NUMBER_OF_PEOPLE; i++) {
            boolean isolating = Greenfoot.getRandomNumber(100) < percentIsolating;
            addObject(new Person(isolating),
                      Greenfoot.getRandomNumber(getWidth()),
                      Greenfoot.getRandomNumber(getHeight()));
        }

        for (int j = 0; j < NUMBER_OF_HOSPITAL; j++) {
            addObject(new Hospital(),
                      Greenfoot.getRandomNumber(getWidth()),
                      Greenfoot.getRandomNumber(getHeight()));
        }

        for (int k = 0; k < NUMBER_OF_AMBULANCE - 1; k++) {
            int x = Greenfoot.getRandomNumber(getWidth());
            int y = Greenfoot.getRandomNumber(getHeight());
            Ambulance ambulance = (Greenfoot.getRandomNumber(2) == 0)
                ? new AmbulanceZigZagHorizontal()
                : new AmbulanceZigZagVertikal();
            addObject(ambulance, x, y);
        }

        EpidemicChart chart = new EpidemicChart(NUMBER_OF_PEOPLE + 1);
        addObject(chart,
                  EpidemicChart.getChartW() / 2,
                  getHeight() - EpidemicChart.getChartH() / 2);

        for (int m = 0; m < NUMBER_OF_VACCINATOR; m++) {
            addObject(new Vaccinator(),
                      Greenfoot.getRandomNumber(getWidth()),
                      Greenfoot.getRandomNumber(getHeight()));
        }

        for (int i = 0; i < NUMBER_OF_RUMAHKARANTINA; i++) {
            addObject(new RumahKarantina(),
                      Greenfoot.getRandomNumber(getWidth()),
                      Greenfoot.getRandomNumber(getHeight()));
        }

        for (int i = 0; i < NUMBER_OF_RUMAHISOLASI; i++) {
            addObject(new RumahIsolasi(),
                      Greenfoot.getRandomNumber(getWidth()),
                      Greenfoot.getRandomNumber(getHeight()));
        }

        infoPanel = new InfoPanel();
        addObject(infoPanel, InfoPanel.getChartW() / 2, InfoPanel.getChartH() / 2);
    }
}