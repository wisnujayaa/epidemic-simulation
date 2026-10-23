import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/**
 * Vaccinator adalah Actor yang berkeliling world untuk mengimunisasi
 * Person yang berstatus Susceptible secara langsung, mencegah mereka
 * sakit sama sekali (tanpa melalui fase Infected).
 *
 * Vaccinator membawa stok vaksin terbatas (5 dosis). Setiap berhasil
 * memvaksin satu orang, Vaccinator memasuki masa cooldown singkat
 * (diam, tidak bergerak maupun memvaksin) sebelum melanjutkan
 * perjalanannya. Begitu stok habis, Vaccinator berpindah mencari
 * Hospital terdekat untuk mengisi ulang persediaan (lihat VaccinatorState
 * untuk detail siklus keadaannya).
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class Vaccinator extends Actor
{
    /** Jumlah dosis vaksin yang tersisa, maksimum 5 per pengisian. */
    private int jumlahVaksin = 5;
    /** Keadaan Vaccinator saat ini (NORMAL, MENUJU_RS, atau RESUPPLY). */
    private VaccinatorState state = VaccinatorState.NORMAL;
    /** Penghitung cycle yang sudah berjalan selama proses resupply di RS. */
    private int resupplyCycle = 0;
    /** Lama proses resupply di Hospital, dalam jumlah cycle. */
    private static final int RESUPPLY_DURATION = 30;
    /** Hospital tujuan yang dipilih sekali saat mulai menuju RS (state MENUJU_RS). */
    private Hospital targetHospital = null;
    /** Sisa cycle Vaccinator harus diam setelah berhasil memvaksin seseorang. */
    private int vaksinasiCooldown = 0;

    /**
     * Membuat Vaccinator baru dengan arah hadap acak dan memasang
     * gambar dokter yang sudah diskalakan ke ukuran tampil di world.
     */
    public Vaccinator() {
        turn(Greenfoot.getRandomNumber(360));
        GreenfootImage img = new GreenfootImage("doctor.png");
        img.scale(40, 40);
        setImage(img);
    }

    /**
     * Dipanggil otomatis tiap cycle. Jika sedang cooldown, Vaccinator
     * diam sepenuhnya (tidak bergerak maupun memvaksin) sampai cooldown
     * habis. Selain itu, perilaku bercabang berdasarkan state saat ini:
     * NORMAL (berkeliling dan mencoba memvaksin), MENUJU_RS (bergerak
     * terarah ke Hospital tujuan), atau RESUPPLY (diam, mengisi ulang
     * stok vaksin).
     */
    public void act() {
        if (vaksinasiCooldown > 0) {
            vaksinasiCooldown--;
            return;
        }
        
        if (state == VaccinatorState.NORMAL) {
            move();
            vaccine();
            if (jumlahVaksin == 0) {
                state = VaccinatorState.MENUJU_RS;
                targetHospital = findNearestHospital();
            }
        }
        else if (state == VaccinatorState.MENUJU_RS) {
            goToNearestHospital(targetHospital);
            if (tibaDiRS(targetHospital)) {
                state = VaccinatorState.RESUPPLY;
            }
        }
        else if (state == VaccinatorState.RESUPPLY) {
            resupplyCycle++;
            if (resupplyCycle==30) {
                resupplyCycle = 0;
                jumlahVaksin = 5;
                state = VaccinatorState.NORMAL;
            }
        }
    }

    /**
     * Mengembalikan jumlah dosis vaksin yang masih tersisa.
     *
     * @return sisa dosis vaksin
     */
    public int getJumlahVaksin() {
        return jumlahVaksin;
    }

    /**
     * Mengatur pergerakan acak Vaccinator selama dalam state NORMAL:
     * berbalik arah di tepi world, maju 4 pixel, dan sesekali berbelok
     * sedikit secara acak agar pola jelajahnya tidak monoton.
     */
    public void move() {
        if (isAtEdge()) {
            turn(180);
        }
        
        move(4);
        
        if (Greenfoot.getRandomNumber(100)<20) {
            turn(Greenfoot.getRandomNumber(61)-30);
        }
    }

    /**
     * Mencari Hospital terdekat dari posisi Vaccinator saat ini dengan
     * membandingkan jarak ke seluruh Hospital di world, lalu memilih
     * yang jaraknya paling kecil.
     *
     * @return Hospital terdekat, atau null jika tidak ada Hospital sama sekali
     */
    public Hospital findNearestHospital() {
        List<Hospital> hospitals = getWorld().getObjects(Hospital.class);
        Hospital nearestHospital = null;
        double minDistance = Double.MAX_VALUE;
        
        if (!hospitals.isEmpty()) {
            for (Hospital hospital: hospitals) {
                double distance = getDistance(hospital);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestHospital = hospital;
                }
            }
        }
        
        return nearestHospital;
    }

    /**
     * Menghitung jarak Euclidean antara Vaccinator ini dengan sebuah
     * Hospital. Greenfoot tidak menyediakan method jarak antar Actor
     * secara bawaan, sehingga dihitung manual dengan rumus Pythagoras.
     *
     * @param hospital Hospital tujuan yang ingin diukur jaraknya
     * @return jarak dalam pixel
     */
    public double getDistance(Hospital hospital) {
        int dx = hospital.getX() - getX();
        int dy = hospital.getY() - getY();
        
        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Menggerakkan Vaccinator satu langkah (4 pixel) menuju Hospital
     * tujuan, dengan terlebih dahulu menghadap ke arahnya.
     *
     * @param hospital Hospital tujuan yang sedang dituju
     */
    public void goToNearestHospital(Hospital hospital) {
        int hospitalX = hospital.getX();
        int hospitalY = hospital.getY();
        
        turnTowards(hospitalX, hospitalY);
        move(4);
    }

    /**
     * Mengecek apakah Vaccinator sudah cukup dekat (dalam radius 30 pixel)
     * dengan Hospital tujuan untuk dianggap "tiba" dan mulai resupply.
     * Memakai jarak dekat (bukan tabrakan langsung) supaya gambar
     * Vaccinator tetap terlihat jelas, tidak menumpuk persis di atas
     * gambar Hospital.
     *
     * @param hospital Hospital tujuan yang sedang dituju
     * @return true jika sudah cukup dekat untuk dianggap tiba
     */
    public boolean tibaDiRS(Hospital hospital) {
        if (getDistance(hospital) < 30) {
            return true;
        }
        return false;
    }

    /**
     * Mencoba memvaksin satu orang Susceptible yang sedang bertabrakan
     * dengan Vaccinator ini. Dibatasi maksimal satu orang per cycle
     * (loop berhenti segera setelah satu orang berhasil divaksin),
     * supaya proses vaksinasi terasa bertahap, bukan borongan sekaligus
     * saat Vaccinator melewati kerumunan padat. Setelah berhasil,
     * Vaccinator memasuki masa cooldown sebelum bisa memvaksin lagi.
     */
    public void vaccine() {
        List<Person> people = getIntersectingObjects(Person.class);
    
        for (Person person : people) {
            if (jumlahVaksin <= 0) {
                break;
            }
            if (person.getStatus() == InfectionStatus.SUSCEPTIBLE) {
                person.vaksinasi();
                jumlahVaksin--;
                vaksinasiCooldown = 5;
                break;
            }
        }
    }
}