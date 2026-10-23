import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.List;

/**
 * EpidemicChart adalah Actor yang menggambar grafik kurva epidemi (model SIR)
 * secara real-time di dalam world simulasi, sekaligus menghitung dan
 * menampilkan R0 (angka reproduksi dasar) serta mensimulasikan reinfeksi
 * berkelanjutan.
 *
 * Grafik menampilkan tiga garis sepanjang waktu:
 *   - Biru  : jumlah orang Susceptible (rentan, belum terinfeksi)
 *   - Merah : jumlah orang Infected (sedang terinfeksi)
 *   - Hijau : jumlah orang Immune (sudah sembuh dan kebal)
 *
 * Setiap beberapa cycle, chart mencatat jumlah orang pada tiap status,
 * lalu menggambar ulang seluruh kurva pada sebuah GreenfootImage.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class EpidemicChart extends Actor {
    /** Riwayat jumlah orang Susceptible pada tiap sample. */
    private ArrayList<Integer> historyS = new ArrayList<>();
    /** Riwayat jumlah orang Infected pada tiap sample. */
    private ArrayList<Integer> historyI = new ArrayList<>();
    /** Riwayat jumlah orang Immune pada tiap sample. */
    private ArrayList<Integer> historyR = new ArrayList<>();

    /** Lebar canvas grafik dalam pixel. */
    private static final int CHART_W = 300;
    /** Tinggi canvas grafik dalam pixel. */
    private static final int CHART_H = 150;
    /** Jumlah cycle antar pencatatan data (sampling). */
    private static final int SAMPLE_INTERVAL = 5;

    /** Penghitung jumlah cycle yang sudah berjalan. */
    private int cycleCounter = 0;
    /** Jumlah total orang di simulasi, dipakai untuk skala sumbu Y. */
    private int totalPeople;

    /** Jumlah total orang yang sudah pernah sembuh dari infeksi. */
    private int totalRecovered = 0;
    /** Jumlah total orang yang ditulari, dijumlahkan dari semua laporan sembuh. */
    private int totalSpread = 0;

    /** Cycle mulai berlakunya reinfeksi berkelanjutan. */
    private static final int MUTATION_CYCLE = 800;

    /**
     * Membuat objek EpidemicChart dan menyiapkan canvas kosong.
     *
     * @param totalPeople jumlah total orang di simulasi (untuk skala sumbu Y)
     */
    public EpidemicChart(int totalPeople) {
        this.totalPeople = totalPeople;
        setImage(new GreenfootImage(CHART_W, CHART_H));
    }

    /**
     * Dipanggil otomatis tiap cycle. Mencatat data tiap SAMPLE_INTERVAL cycle,
     * menggambar ulang grafik tiap cycle, dan memeriksa kemungkinan
     * reinfeksi setiap cycle setelah cycleCounter mencapai MUTATION_CYCLE.
     */
    public void act() {
        cycleCounter++;
        if (cycleCounter % SAMPLE_INTERVAL == 0) {
            recordSample();
        }
        redraw();

        if (cycleCounter >= MUTATION_CYCLE) {
            checkReinfection();
        }
    }

    /**
     * Mengembalikan tinggi canvas grafik. Berguna bagi class lain
     * (misalnya MyWorld) untuk menghitung posisi penempatan chart.
     *
     * @return tinggi canvas dalam pixel
     */
    public static int getChartH() {
        return CHART_H;
    }

    /**
     * Mengembalikan lebar canvas grafik.
     *
     * @return lebar canvas dalam pixel
     */
    public static int getChartW() {
        return CHART_W;
    }

    /**
     * Menghitung jumlah orang pada tiap status (Susceptible, Infected, Immune)
     * dengan memeriksa seluruh objek Person di world, lalu menyimpan hasilnya
     * ke dalam ArrayList riwayat masing-masing.
     */
    private void recordSample() {
        int s = 0;
        int i = 0;
        int r = 0;

        for (Person person : getWorld().getObjects(Person.class)) {
            InfectionStatus status = person.getStatus();
            switch (status) {
                case InfectionStatus.SUSCEPTIBLE:
                    s++;
                    break;
                case InfectionStatus.INFECTED:
                    i++;
                    break;
                case InfectionStatus.IMMUNE:
                    r++;
                    break;
                default:
                    break;
            }
        }

        historyS.add(s);
        historyI.add(i);
        historyR.add(r);
    }

    /**
     * Mengubah indeks sample (urutan waktu) menjadi koordinat X pixel.
     * Sample pertama dipetakan ke x = 0 (kiri), sample terakhir ke
     * x = CHART_W (kanan), sisanya tersebar merata secara proporsional.
     *
     * @param index indeks sample yang ingin dipetakan
     * @param total jumlah total sample saat ini
     * @return koordinat X dalam pixel
     */
    private int mapX(int index, int total) {
        if (total <= 1) return 0;
        return (int) ((double) index / (total - 1) * CHART_W);
    }

    /**
     * Mengubah jumlah orang menjadi koordinat Y pixel. Karena pixel Y di layar
     * bertambah ke arah bawah, hasilnya dibalik dengan CHART_H - (...) agar
     * nilai yang besar muncul di bagian atas grafik.
     *
     * @param count jumlah orang pada suatu status
     * @return koordinat Y dalam pixel
     */
    private int mapY(int count) {
        return CHART_H - (int) ((double) count / totalPeople * CHART_H);
    }

    /**
     * Menggambar satu kurva dengan menghubungkan tiap pasang titik data
     * yang berurutan menggunakan garis lurus. Loop dimulai dari indeks 1
     * karena satu garis membutuhkan dua titik (sebelumnya dan sekarang).
     *
     * @param img   canvas tempat menggambar
     * @param data  riwayat data yang akan digambar
     * @param color warna garis kurva
     */
    private void drawCurve(GreenfootImage img, ArrayList<Integer> data, Color color) {
        img.setColor(color);
        int n = data.size();
        for (int idx = 1; idx < n; idx++) {
            int x1 = mapX(idx - 1, n);
            int y1 = mapY(data.get(idx - 1));
            int x2 = mapX(idx, n);
            int y2 = mapY(data.get(idx));
            img.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Menggambar ulang seluruh isi grafik: background semi-transparan, border,
     * garis sumbu, ketiga kurva (S, I, R), label sumbu, legenda, dan angka R0.
     * Canvas dibersihkan dengan clear() terlebih dahulu agar tidak ada
     * sisa gambar lama yang menumpuk.
     */
    private void redraw() {
        GreenfootImage img = getImage();
        img.clear();

        img.setColor(new Color(255, 255, 255, 180));
        img.fillRect(0, 0, CHART_W, CHART_H);

        img.setColor(Color.BLACK);
        img.drawRect(0, 0, CHART_W - 1, CHART_H - 1);
        img.drawLine(0, CHART_H - 1, CHART_W - 1, CHART_H - 1);

        drawCurve(img, historyI, Color.RED);
        drawCurve(img, historyS, Color.BLUE);
        drawCurve(img, historyR, Color.GREEN);

        drawAxisLabels(img);
        drawLegend(img);
        drawR0(img);
    }

    /**
     * Menulis label angka pada sumbu Y: "0" di bagian bawah dan
     * jumlah total orang di bagian atas.
     *
     * @param img canvas tempat menulis label
     */
    private void drawAxisLabels(GreenfootImage img) {
        img.setColor(Color.BLACK);
        img.drawString("0", 2, CHART_H - 2);
        img.drawString("" + totalPeople, 2, 12);
    }

    /**
     * Menggambar legenda kecil di pojok kanan atas yang menjelaskan
     * arti tiap warna: S (biru), I (merah), R (hijau).
     *
     * @param img canvas tempat menggambar legenda
     */
    private void drawLegend(GreenfootImage img) {
        int legendX = CHART_W - 60;
        int legendY = 10;

        img.setColor(Color.BLUE);
        img.fillRect(legendX, legendY, 8, 8);
        img.setColor(Color.BLACK);
        img.drawString("S", legendX + 12, legendY + 8);

        img.setColor(Color.RED);
        img.fillRect(legendX, legendY + 14, 8, 8);
        img.setColor(Color.BLACK);
        img.drawString("I", legendX + 12, legendY + 22);

        img.setColor(Color.GREEN);
        img.fillRect(legendX, legendY + 28, 8, 8);
        img.setColor(Color.BLACK);
        img.drawString("R", legendX + 12, legendY + 36);
    }

    /**
     * Menerima laporan dari Person yang baru sembuh: mencatat satu orang
     * sembuh, dan menambahkan jumlah orang yang sempat ditulari olehnya
     * ke total keseluruhan. Dipanggil oleh Person.reportRecovery().
     *
     * @param spreadCount jumlah orang yang ditulari oleh Person yang sembuh ini
     */
    public void reportRecovery(int spreadCount) {
        totalRecovered++;
        totalSpread += spreadCount;
    }

    /**
     * Menghitung R0 (angka reproduksi dasar): rata-rata jumlah orang yang
     * ditulari oleh satu orang infected sebelum infeksinya berakhir.
     *
     * Indikator tingkat penularan:
     *   R0 kurang dari 1 : wabah cenderung mereda
     *   R0 sama dengan 1 : penyebaran stabil
     *   R0 lebih dari 1  : wabah sedang berkembang
     *
     * @return nilai R0 saat ini, atau 0.0 jika belum ada yang sembuh
     */
    public double getR0() {
        if (totalRecovered == 0) {
            return 0.0;
        }
        return (double) totalSpread / totalRecovered;
    }

    /**
     * Menulis angka R0 saat ini di bagian atas tengah canvas.
     *
     * @param img canvas tempat menulis angka R0
     */
    private void drawR0(GreenfootImage img) {
        img.setColor(Color.BLACK);
        double r0 = getR0();
        String teks = "R0 = " + String.format("%.2f", r0);
        img.drawString(teks, CHART_W / 2 - 30, 12);
    }

    /**
     * Memeriksa kemungkinan reinfeksi: setiap cycle setelah MUTATION_CYCLE,
     * setiap orang Immune punya peluang kecil (0.1% per cycle) untuk
     * kehilangan kekebalannya dan kembali menjadi Susceptible. Proses ini
     * berkelanjutan (dipanggil tiap cycle, bukan sekali saja), sehingga
     * sistem perlahan bergerak menuju kondisi endemic equilibrium, mirip
     * penyakit endemik di dunia nyata yang tidak pernah benar-benar hilang
     * namun juga tidak meledak kembali.
     */
    private void checkReinfection() {
        List<Person> people = getWorld().getObjects(Person.class);

        for (Person person : people) {
            if (person.getStatus() == InfectionStatus.IMMUNE && Greenfoot.getRandomNumber(1000) < 1) {
                person.setStatus(InfectionStatus.SUSCEPTIBLE);
            }
        }
    }
}