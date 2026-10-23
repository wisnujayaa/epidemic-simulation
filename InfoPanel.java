import greenfoot.*;
import java.util.List;

/**
 * InfoPanel adalah overlay informasi real-time yang ditampilkan di pojok
 * kiri atas world. Menampilkan statistik run yang sedang berjalan dalam
 * satu panel semi-transparan, mencakup data epidemiologi (R0, jumlah
 * infected, immune) dan data karantina (penghuni RumahKarantina dan
 * RumahIsolasi saat ini).
 *
 * Data dibagi dua sumber: data yang hanya diketahui MyWorld (run, total
 * run, maxInfected, percentIsolating) dikirim lewat update() setiap cycle,
 * sedangkan data lain (R0, karantina, immune) diambil langsung dari world
 * oleh InfoPanel sendiri saat redraw().
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class InfoPanel extends Actor {

    /** Lebar canvas panel dalam pixel. */
    private static final int CHART_W = 300;
    /** Tinggi canvas panel dalam pixel. */
    private static final int CHART_H = 150;

    /** Run yang sedang berjalan (1-based, untuk tampilan). */
    private int run;
    /** Jumlah Infected tertinggi yang pernah tercatat di run ini. */
    private int maxInfected;
    /** Persentase populasi yang isIsolating di run ini. */
    private int percentIsolating;
    /** Total jumlah run dalam eksperimen (panjang PERCENT_ISOLATING). */
    private int totalRun;

    /**
     * Membuat InfoPanel baru dengan canvas kosong berukuran CHART_W x CHART_H.
     * Semua field diinisialisasi ke 0 — akan diisi oleh update() pertama
     * yang dipanggil MyWorld di cycle pertama.
     */
    public InfoPanel() {
        setImage(new GreenfootImage(CHART_W, CHART_H));
        run = 0;
        maxInfected = 0;
        percentIsolating = 0;
        totalRun = 0;
    }

    /** @return lebar canvas InfoPanel dalam pixel */
    public static int getChartW() { return CHART_W; }

    /** @return tinggi canvas InfoPanel dalam pixel */
    public static int getChartH() { return CHART_H; }

    /**
     * Memperbarui data yang hanya diketahui MyWorld, lalu menggambar
     * ulang seluruh panel. Dipanggil oleh MyWorld.act() setiap cycle.
     *
     * @param run              run yang sedang berjalan (1-based)
     * @param totalRun         total jumlah run dalam eksperimen
     * @param maxInfected      jumlah Infected tertinggi di run ini
     * @param percentIsolating persentase populasi yang isIsolating
     */
    public void update(int run, int totalRun, int maxInfected, int percentIsolating) {
        this.run = run;
        this.totalRun = totalRun;
        this.maxInfected = maxInfected;
        this.percentIsolating = percentIsolating;
        redraw();
    }

    /**
     * Menggambar ulang seluruh isi panel setiap cycle. Mengambil data
     * R0 dari EpidemicChart (default 0.0 jika belum tersedia), lalu
     * menggambar background semi-transparan, border, dan semua baris
     * teks informasi. Peringatan "⚠ WABAH BERKEMBANG" hanya ditampilkan
     * saat R0 > 1.
     */
    private void redraw() {
        GreenfootImage img = getImage();
        img.clear();

        img.setColor(new Color(255, 255, 255, 180));
        img.fillRect(0, 0, CHART_W, CHART_H);

        img.setColor(Color.BLACK);
        img.drawRect(0, 0, CHART_W - 1, CHART_H - 1);
        img.drawLine(0, CHART_H - 1, CHART_W - 1, CHART_H - 1);

        double R0 = 0.0;
        List<EpidemicChart> charts = getWorld().getObjects(EpidemicChart.class);
        if (!charts.isEmpty()) {
            R0 = charts.get(0).getR0();
        }

        int x = 10;
        int y = 20;

        img.drawString("Run " + run + "/" + totalRun + " | Isolating: " + percentIsolating + "%", x, y);
        img.drawString("Infected: " + Person.getNumInfected(), x, y + 16);
        img.drawString("Max: " + maxInfected, x, y + 32);
        img.drawString("R0: " + String.format("%.2f", R0), x, y + 48);
        img.drawString("Di Karantina: " + getTotalKarantina(), x, y + 64);
        img.drawString("Di Isolasi: " + getTotalIsolasi(), x, y + 80);
        img.drawString("Immune: " + recordPercentImmune(), x, y + 96);
        if (R0 > 1) {
            img.drawString("⚠ WABAH BERKEMBANG", x, y + 120);
        }
    }

    /**
     * Menghitung total penghuni di semua RumahKarantina di world
     * dengan menjumlahkan getCurrentOccupants() dari setiap instance.
     *
     * @return total Person SUSCEPTIBLE yang sedang di karantina
     */
    public int getTotalKarantina() {
        int total = 0;
        for (RumahKarantina r : getWorld().getObjects(RumahKarantina.class)) {
            total += r.getCurrentOccupants();
        }
        return total;
    }

    /**
     * Menghitung total penghuni di semua RumahIsolasi di world
     * dengan menjumlahkan getCurrentOccupants() dari setiap instance.
     *
     * @return total Person INFECTED yang sedang di isolasi
     */
    public int getTotalIsolasi() {
        int total = 0;
        for (RumahIsolasi r : getWorld().getObjects(RumahIsolasi.class)) {
            total += r.getCurrentOccupants();
        }
        return total;
    }

    /**
     * Menghitung persentase populasi yang berstatus IMMUNE saat ini.
     * Dibagi dengan total populasi dari MyWorld.getNumberOfPeople()
     * agar akurat meskipun populasi berubah antar run.
     *
     * @return string persentase immune, misalnya "23.4%"
     */
    private String recordPercentImmune() {
        int immune = 0;
        for (Person person : getWorld().getObjects(Person.class)) {
            if (person.getStatus() == InfectionStatus.IMMUNE) {
                immune++;
            }
        }
        return String.format("%.1f%%", (double) immune / MyWorld.getNumberOfPeople() * 100);
    }
}