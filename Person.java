import greenfoot.*;
import java.util.List;

/**
 * Person merepresentasikan satu individu dalam simulasi epidemi SIR
 * (Susceptible-Infected-Immune). Setiap Person bergerak acak di world,
 * dapat menulari atau tertular Person lain saat berpapasan, dan dapat
 * dirawat di Hospital atau disembuhkan oleh Ambulance maupun Vaccinator.
 *
 * Perilaku pergerakan Person ditentukan oleh kombinasi tiga faktor:
 * status infeksi (SUSCEPTIBLE/INFECTED/IMMUNE), flag isIsolating yang
 * ditetapkan saat lahir, dan kondisi wabah saat ini (shouldQuarantine()).
 * Person yang isIsolating akan mencari QuarantineBuilding yang sesuai
 * saat wabah membesar, dan kembali bergerak bebas saat wabah mereda.
 *
 * Sebagian populasi ditandai sebagai "high risk" sejak lahir, yang
 * membuat mereka lebih mudah tertular dan butuh waktu sembuh lebih lama
 * dibanding populasi biasa.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class Person extends Actor
{
    /** Sisa cycle sebelum Person sembuh secara alami dari infeksi.
     *  Di-set ulang setiap kali status berubah menjadi INFECTED. */
    private int INFECTION_TIME;

    /** Jumlah total Person yang sedang berstatus INFECTED saat ini,
     *  dibagikan oleh semua instance. Di-increment saat setStatus(INFECTED)
     *  dan di-decrement saat sembuh. Dipakai MyWorld untuk menampilkan
     *  statistik dan memicu spawn Ambulance baru. */
    private static int numInfected = 0;

    /** Penghitung mundur untuk masa rawat di Hospital. Di-set ke
     *  treatmentDuration saat Person pertama kali INFECTED, lalu
     *  dihitung mundur oleh treatmentProcess() setiap cycle. */
    private int actCycle;

    /** Status infeksi Person saat ini. Perubahan status selalu lewat
     *  setStatus() agar numInfected dan state karantina selalu konsisten. */
    private InfectionStatus status;

    /** Jumlah orang yang berhasil ditulari oleh Person ini sejak infeksi
     *  terakhirnya. Di-reset ke 0 setiap kali reportRecovery() dipanggil,
     *  sehingga akurat untuk perhitungan R0 meski terjadi reinfeksi. */
    private int peopleInfectedByMe = 0;

    /** Menandai apakah Person ini termasuk kelompok rentan (high risk).
     *  Ditentukan secara acak saat lahir (peluang 30%). High risk berarti
     *  peluang tertular lebih tinggi (90% vs 50%) dan masa rawat lebih lama. */
    private boolean isHighRisk;

    /** Lama masa rawat di Hospital: 60 cycle jika high risk, 30 jika tidak.
     *  Juga menentukan panjang INFECTION_TIME lewat getInfectionTime(). */
    private int treatmentDuration;

    /** Sisa cycle Person harus diam setelah divaksin. Selama > 0,
     *  Person tidak bergerak maupun melakukan aksi apapun di act(). */
    private int vaccineCycle = 0;

    /** Menandai apakah Person ini memilih untuk membatasi gerak (isolating).
     *  Ditetapkan saat lahir berdasarkan persentase run saat ini di MyWorld,
     *  tidak berubah sepanjang hidup Person. Person yang isIsolating akan
     *  mencari QuarantineBuilding saat wabah membesar, dan diam di tempat
     *  jika tidak ada tempat tersedia. */
    private boolean isIsolating;

    /** Menandai apakah Person ini sedang berada di dalam QuarantineBuilding.
     *  Hanya true jika admitPeople() sudah dipanggil ke targetRumah —
     *  jaminan: isQuarantined == true selalu berarti targetRumah != null.
     *  Di-reset ke false oleh resetQuarantineState() saat status berubah
     *  menjadi INFECTED/IMMUNE atau saat wabah mereda. */
    private boolean isQuarantined;

    /** QuarantineBuilding yang sedang dituju atau ditempati Person ini.
     *  null jika Person tidak sedang dalam proses menuju atau berada di
     *  karantina. Dipakai oleh resetQuarantineState() untuk memanggil
     *  dischargePeople() ke bangunan yang tepat saat Person keluar. */
    private QuarantineBuilding targetRumah = null;

    /** Batas jumlah Infected yang memicu perilaku karantina. Saat
     *  numInfected melebihi nilai ini ATAU R0 > 1, Person yang isIsolating
     *  mulai mencari QuarantineBuilding. Nilai 50 dipilih sebagai ~16%
     *  dari total populasi 301 orang — cukup signifikan untuk dianggap
     *  wabah, tidak terlalu sensitif terhadap kasus sporadis. */
    private static final int QUARANTINE_THRESHOLD = 70;

    /**
     * Membuat Person baru dengan arah gerak acak, status awal SUSCEPTIBLE,
     * dan menentukan secara acak (peluang 30%) apakah Person ini termasuk
     * kelompok high risk, yang menentukan lama masa rawat dan peluang
     * tertularnya kelak.
     *
     * @param isolating true jika Person ini memilih membatasi gerak
     */
    public Person(boolean isolating) {
        turn(Greenfoot.getRandomNumber(360));
        setStatus(InfectionStatus.SUSCEPTIBLE);
        isHighRisk = Greenfoot.getRandomNumber(100) < 30;
        treatmentDuration = isHighRisk ? 60 : 30;
        isIsolating = isolating;
    }

    /**
     * Dipanggil otomatis oleh Greenfoot saat Person ditambahkan ke world.
     * Ada peluang 10% Person ini memiliki Child yang langsung ditempatkan
     * di sebelahnya. Child mewarisi flag isIsolating dari parent-nya.
     *
     * @param world world tempat Person ditambahkan
     */
    public void addedToWorld(World world) {
        boolean hasChild = Greenfoot.getRandomNumber(100) < 10;
        if (hasChild) {
            Child child = new Child(isIsolating, this);
            getWorld().addObject(child, getX() + 16, getY());
        }
    }

    /**
     * Mengatur ulang penghitung global numInfected ke nol.
     * Harus dipanggil setiap kali simulasi dimulai ulang (populate())
     * agar statistik tidak carry-over dari run sebelumnya.
     */
    public static void resetNumInfected() {
        numInfected = 0;
    }

    /**
     * Dipanggil otomatis tiap cycle. Urutan prioritas:
     * 1. Masa rawat Hospital → treatmentProcess(), stop.
     * 2. Masa diam pasca vaksin → vaccinationProcess(), stop.
     * 3. handleMovement() — bergerak sesuai status dan kondisi wabah.
     * 4. Jika INFECTED → tularkan dan hitung mundur waktu infeksi.
     * 5. Jika ada Ambulance di dekat → terima perawatan.
     */
    public void act() {
        if (isTreatmentPeriod()) {
            treatmentProcess();
            return;
        }

        if (isBeingVaccinated()) {
            vaccinationProcess();
            return;
        }

        handleMovement();

        if (status == InfectionStatus.INFECTED) {
            infectOthers();
            countDownInfectionTime();
        }

        if (findAmbulance()) {
            handleAmbulanceTreatment();
        }
    }

    /**
     * Menggerakkan Person satu langkah secara acak: berbalik di tepi world,
     * maju 4 pixel, dan sesekali (peluang 20%) berbelok sedikit agar
     * pola jelajahnya tidak monoton.
     */
    public void move() {
        if (isAtEdge()) {
            turn(180);
        }
        move(4);
        if (Greenfoot.getRandomNumber(100) < 20) {
            turn(Greenfoot.getRandomNumber(61) - 30);
        }
    }

    /**
     * Mengelola pergerakan Person setiap cycle berdasarkan status infeksi,
     * flag isIsolating, dan kondisi wabah saat ini.
     *
     * Jika sedang di karantina (isQuarantined): cek apakah wabah sudah
     * mereda — jika ya, keluar dari karantina lewat resetQuarantineState().
     * Jika isIsolating dan wabah aktif (shouldQuarantine()) dan status
     * SUSCEPTIBLE atau INFECTED: cari QuarantineBuilding terdekat yang
     * available dan bergerak ke sana. Jika tidak ada yang available,
     * Person diam di tempat. Selain kondisi di atas: bergerak random.
     */
    private void handleMovement() {
        if (isQuarantined) {
            if (!shouldQuarantine()) {
                resetQuarantineState();
            }
            return;
        }

        boolean shouldSeekBuilding =
            (status == InfectionStatus.SUSCEPTIBLE || status == InfectionStatus.INFECTED)
            && isIsolating
            && shouldQuarantine();

        if (shouldSeekBuilding) {
            QuarantineBuilding rumah = findNearestBuilding();
            if (rumah != null) {
                goToQuarantineCentre(rumah);
            }
        } else {
            move();
        }
    }

    /**
     * Mengecek apakah kondisi wabah memenuhi syarat untuk memicu
     * perilaku karantina. Dua kondisi dengan OR — salah satu cukup:
     * numInfected melampaui QUARANTINE_THRESHOLD (wabah sudah besar),
     * ATAU R0 > 1 (wabah sedang berkembang). Mengembalikan false jika
     * EpidemicChart belum tersedia di world (awal simulasi).
     *
     * @return true jika kondisi wabah memerlukan karantina
     */
    private boolean shouldQuarantine() {
        List<EpidemicChart> charts = getWorld().getObjects(EpidemicChart.class);
        if (!charts.isEmpty()) {
            return numInfected > QUARANTINE_THRESHOLD || charts.get(0).getR0() > 1;
        }
        return false;
    }

    /**
     * Mencari QuarantineBuilding terdekat yang masih punya kapasitas.
     * SUSCEPTIBLE mencari RumahKarantina; INFECTED mencari RumahIsolasi.
     * Hanya mempertimbangkan bangunan yang hasSpace() == true.
     *
     * @return QuarantineBuilding terdekat yang available, atau null jika
     *         semua penuh atau tidak ada bangunan di world
     */
    public QuarantineBuilding findNearestBuilding() {
        List<QuarantineBuilding> perumahan;

        if (status == InfectionStatus.SUSCEPTIBLE) {
            perumahan = (List<QuarantineBuilding>)(List<?>)
                        getWorld().getObjects(RumahKarantina.class);
        } else {
            perumahan = (List<QuarantineBuilding>)(List<?>)
                        getWorld().getObjects(RumahIsolasi.class);
        }

        QuarantineBuilding rumahTerdekat = null;
        double minDistance = Double.MAX_VALUE;

        for (QuarantineBuilding rumah : perumahan) {
            double distance = getDistance(rumah);
            if (distance < minDistance && rumah.hasSpace()) {
                minDistance = distance;
                rumahTerdekat = rumah;
            }
        }

        return rumahTerdekat;
    }

    /**
     * Mengelola proses perjalanan dan masuk ke QuarantineBuilding.
     * Jika belum tiba: bergerak menuju rumah. Jika sudah tiba:
     * - hasSpace() → admit, simpan referensi, set isQuarantined = true.
     * - penuh → reset targetRumah dan isQuarantined agar cycle berikutnya
     *   findNearestBuilding() mencari bangunan lain secara otomatis.
     *
     * @param rumah QuarantineBuilding yang sedang dituju
     */
    private void goToQuarantineCentre(QuarantineBuilding rumah) {
        if (hasArrived(rumah)) {
            if (rumah.hasSpace()) {
                targetRumah = rumah;
                rumah.admitPeople();
                isQuarantined = true;
            } else {
                targetRumah = null;
                isQuarantined = false;
            }
        } else {
            moveTowards(rumah);
        }
    }

    /**
     * Mereset semua state yang berkaitan dengan karantina. Memanggil
     * dischargePeople() ke targetRumah jika tidak null (agar kapasitas
     * bangunan ikut berkurang), lalu mereset targetRumah dan isQuarantined.
     * Dipanggil saat Person keluar karantina secara normal (wabah mereda)
     * maupun paksa (status berubah jadi INFECTED atau IMMUNE).
     */
    private void resetQuarantineState() {
        if (targetRumah != null) {
            targetRumah.dischargePeople();
        }
        targetRumah = null;
        isQuarantined = false;
    }

    /**
     * Menggerakkan Person lurus menuju QuarantineBuilding target,
     * 4 pixel per cycle.
     *
     * @param rumah QuarantineBuilding tujuan
     */
    public void moveTowards(QuarantineBuilding rumah) {
        turnTowards(rumah.getX(), rumah.getY());
        move(4);
    }

    /**
     * Mengecek apakah Person sudah cukup dekat dengan QuarantineBuilding
     * untuk dianggap "tiba" (dalam radius 30 pixel).
     *
     * @param rumah QuarantineBuilding yang dituju
     * @return true jika jarak kurang dari 30 pixel
     */
    public boolean hasArrived(QuarantineBuilding rumah) {
        return getDistance(rumah) < 30;
    }

    /**
     * Menghitung jarak Euclidean ke QuarantineBuilding menggunakan
     * rumus Pythagoras. Greenfoot tidak menyediakan method jarak antar
     * Actor secara bawaan.
     *
     * @param rumah QuarantineBuilding yang ingin diukur jaraknya
     * @return jarak dalam pixel
     */
    public double getDistance(QuarantineBuilding rumah) {
        int dx = rumah.getX() - getX();
        int dy = rumah.getY() - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Mengeset status infeksi Person dan memperbarui semua state yang
     * bergantung padanya. Selalu gunakan method ini — jangan set field
     * status langsung — agar numInfected dan state karantina tetap konsisten.
     *
     * Jaminan:
     * - INFECTED: INFECTION_TIME dan actCycle di-reset, numInfected++,
     *   state karantina di-reset (Person keluar dari karantina).
     * - IMMUNE (dari INFECTED): numInfected--, state karantina di-reset.
     * - SUSCEPTIBLE (dari INFECTED): numInfected--, actCycle di-reset.
     *
     * @param status status infeksi baru
     */
    public void setStatus(InfectionStatus status) {
        InfectionStatus prevStatus = this.status;
        this.status = status;
        setScaleAndImage(status);

        if (status == InfectionStatus.INFECTED) {
            INFECTION_TIME = getInfectionTime();
            actCycle = treatmentDuration;
            numInfected++;
            resetQuarantineState();
        } else if (status == InfectionStatus.IMMUNE &&
                   prevStatus == InfectionStatus.INFECTED) {
            numInfected--;
            resetQuarantineState();
        } else if (status == InfectionStatus.SUSCEPTIBLE) {
            actCycle = treatmentDuration;
            if (prevStatus == InfectionStatus.INFECTED) numInfected--;
        }
    }

    /**
     * Mengganti gambar Person sesuai status infeksinya saat ini,
     * diskalakan ke 20x20 pixel.
     *
     * @param status status infeksi yang menentukan gambar yang ditampilkan
     */
    public void setScaleAndImage(InfectionStatus status) {
        GreenfootImage img = new GreenfootImage(status.getInfectionImage());
        img.scale(20, 20);
        setImage(img);
    }

    /**
     * Mengembalikan lama waktu infeksi dalam cycle: 10 kali treatmentDuration.
     * High risk: 600 cycle, normal: 300 cycle.
     *
     * @return lama infeksi dalam cycle
     */
    public int getInfectionTime() {
        return 10 * treatmentDuration;
    }

    /**
     * Menghitung mundur INFECTION_TIME dan memanggil heal() setiap cycle
     * selama Person berstatus INFECTED.
     */
    public void countDownInfectionTime() {
        if (status == InfectionStatus.INFECTED) {
            INFECTION_TIME--;
            heal();
        }
    }

    /**
     * Memeriksa apakah Person sudah waktunya sembuh. Ada dua jalur:
     * INFECTION_TIME habis → langsung IMMUNE; actCycle habis (selesai
     * rawat di Hospital) → 30% IMMUNE, 70% kembali SUSCEPTIBLE.
     * reportRecovery() hanya dipanggil jika benar-benar sembuh.
     */
    public void heal() {
        if (status == InfectionStatus.INFECTED) {
            boolean sembuh = false;

            if (INFECTION_TIME == 0) {
                setStatus(InfectionStatus.IMMUNE);
                sembuh = true;
            } else if (actCycle == 0) {
                boolean isImmune = Greenfoot.getRandomNumber(100) < 30;
                if (isImmune) setStatus(InfectionStatus.IMMUNE);
                else setStatus(InfectionStatus.SUSCEPTIBLE);
                sembuh = true;
            }

            if (sembuh) {
                reportRecovery();
            }
        }
    }

    /**
     * Mencoba menulari satu Person lain yang sedang bersinggungan.
     * Dibatasi satu orang per cycle oleh getOneIntersectingObject().
     * Jika berhasil menulari, peopleInfectedByMe bertambah satu untuk
     * keperluan perhitungan R0.
     */
    public void infectOthers() {
        Person otherPerson = (Person) getOneIntersectingObject(Person.class);
        if (otherPerson != null) {
            boolean terinfeksi = otherPerson.infect();
            if (terinfeksi) {
                peopleInfectedByMe++;
            }
        }
    }

    /**
     * Mencoba menulari Person ini. Hanya berhasil jika SUSCEPTIBLE,
     * dengan peluang 90% untuk high risk dan 50% untuk normal.
     * isHighRisk yang diperiksa adalah milik Person yang akan ditulari
     * (penerima infeksi), bukan penularnya.
     *
     * @return true jika berhasil tertular
     */
    public boolean infect() {
        if (status == InfectionStatus.SUSCEPTIBLE) {
            int peluang = isHighRisk ? 90 : 50;
            if (Greenfoot.getRandomNumber(100) < peluang) {
                setStatus(InfectionStatus.INFECTED);
                return true;
            }
        }
        return false;
    }

    /**
     * Mengecek apakah objek saat ini sedang dalam masa perawatan Hospital.
     * Untuk Person yang baru masuk (actCycle == treatmentDuration), Hospital
     * harus punya tempat. Untuk yang sudah di tengah masa rawat, pengecekan
     * kapasitas dilewati — slot mereka sudah terisi sejak masuk.
     *
     * @return true jika sedang dalam masa perawatan di Hospital
     */
    public boolean isTreatmentPeriod() {
        Hospital hospital = getHospital();
        if (hospital == null) return false;

        boolean punyaTempat = (actCycle == treatmentDuration) ?
                               hospital.hasSpace() : true;

        return status == InfectionStatus.INFECTED && actCycle > 0 && punyaTempat;
    }

    /**
     * Menghitung mundur actCycle selama masa rawat di Hospital.
     * admitPatient() dipanggil tepat saat masuk (actCycle == treatmentDuration),
     * dischargePatient() saat keluar (actCycle == 0). heal() dipanggil
     * setiap cycle untuk mengecek apakah sudah waktunya sembuh.
     */
    public void treatmentProcess() {
        Hospital hospital = getHospital();
        if (hospital == null) return;

        if (actCycle == treatmentDuration) {
            hospital.admitPatient();
        }

        actCycle--;

        if (actCycle == 0) {
            hospital.dischargePatient();
        }

        heal();
    }

    /**
     * Mencari Hospital pertama dalam radius 40 pixel dari Person ini.
     *
     * @return Hospital terdekat dalam radius, atau null jika tidak ada
     */
    public Hospital getHospital() {
        List<Hospital> hospital = getObjectsInRange(40, Hospital.class);
        return hospital.isEmpty() ? null : hospital.get(0);
    }

    /**
     * Mengecek apakah ada Ambulance dalam radius 40 pixel.
     * Mendeteksi semua subclass Ambulance secara polymorphic.
     *
     * @return true jika ada Ambulance di sekitar
     */
    public boolean findAmbulance() {
        List<Ambulance> ambulance = getObjectsInRange(40, Ambulance.class);
        return !ambulance.isEmpty();
    }

    /**
     * Menyembuhkan Person secara langsung jika INFECTED dan bertemu
     * Ambulance — tanpa melalui masa rawat Hospital. Status kembali
     * ke SUSCEPTIBLE (bukan IMMUNE) karena Ambulance hanya meredakan
     * gejala, tidak memberikan kekebalan jangka panjang.
     */
    public void handleAmbulanceTreatment() {
        if (status == InfectionStatus.INFECTED) {
            setStatus(InfectionStatus.SUSCEPTIBLE);
            reportRecovery();
        }
    }

    /**
     * Melaporkan kesembuhan ke EpidemicChart untuk perhitungan R0,
     * lalu mereset peopleInfectedByMe ke 0 agar akurat jika Person
     * terinfeksi lagi di kemudian hari (reinfeksi).
     */
    private void reportRecovery() {
        List<EpidemicChart> charts = getWorld().getObjects(EpidemicChart.class);
        if (!charts.isEmpty()) {
            charts.get(0).reportRecovery(peopleInfectedByMe);
        }
        peopleInfectedByMe = 0;
    }

    /**
     * Mengecek apakah Person sedang dalam masa diam pasca vaksinasi.
     *
     * @return true jika vaccineCycle masih > 0
     */
    public boolean isBeingVaccinated() {
        return vaccineCycle > 0;
    }

    /**
     * Menghitung mundur masa diam pasca vaksinasi satu cycle.
     */
    public void vaccinationProcess() {
        vaccineCycle--;
    }

    /**
     * Memvaksin Person ini: status langsung jadi IMMUNE tanpa melalui
     * fase INFECTED, dan Person diam selama 5 cycle sebagai representasi
     * waktu proses vaksinasi. Dipanggil oleh Vaccinator.vaccine().
     */
    public void vaksinasi() {
        setStatus(InfectionStatus.IMMUNE);
        vaccineCycle = 5;
    }

    /** @return status infeksi Person saat ini */
    public InfectionStatus getStatus() { return status; }

    /** @return true jika Person termasuk kelompok high risk */
    public boolean isHighRisk() { return isHighRisk; }

    /** @return jumlah total Person yang sedang INFECTED di seluruh simulasi */
    public static int getNumInfected() { return numInfected; }

    /** @return jumlah orang yang ditulari Person ini sejak infeksi terakhir */
    public int getPeopleInfectedByMe() { return peopleInfectedByMe; }

    /** Mengatur ulang numInfected ke 0. Dipanggil di awal setiap run. */
    public static void reset() { numInfected = 0; }
}