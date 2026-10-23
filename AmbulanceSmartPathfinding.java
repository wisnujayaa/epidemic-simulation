import greenfoot.*;
import java.util.List;

/**
 * AmbulanceSmartPathfinding adalah Ambulance yang aktif mengejar Person
 * INFECTED terdekat yang tidak sedang dirawat di Hospital, alih-alih
 * bergerak dalam pola tetap. Target dicari ulang setiap cycle sehingga
 * Ambulance otomatis beralih ke Infected lain yang lebih dekat.
 *
 * Meng-extend Ambulance (bukan Actor langsung) agar terdeteksi oleh
 * Person.findAmbulance() yang mencari Ambulance.class secara polymorphic.
 * Sistem gerak 4-arah milik Ambulance tidak dipakai — move() dan
 * turnDirection() di-override total.
 *
 * Jumlah instance di world bertambah dinamis: MyWorld menambah satu
 * AmbulanceSmartPathfinding setiap kali numInfected naik kelipatan 50,
 * sehingga respons medis meningkat seiring skala wabah.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class AmbulanceSmartPathfinding extends Ambulance {

    /**
     * Membuat AmbulanceSmartPathfinding baru. Direction.SOUTH hanya
     * untuk memenuhi syarat constructor Ambulance — tidak pernah dipakai
     * karena move() di-override total.
     */
    public AmbulanceSmartPathfinding() {
        super(Direction.SOUTH);
    }

    /**
     * Mencari Person INFECTED terdekat yang tidak sedang dirawat di
     * Hospital (isTreatmentPeriod() == false). Person yang sedang di
     * Hospital dilewati karena sudah ditangani — Ambulance lebih berguna
     * mengejar yang masih berkeliaran.
     *
     * @return Person INFECTED terdekat yang available, atau null jika
     *         tidak ada
     */
    public Person findNearestInfected() {
        List<Person> people = getWorld().getObjects(Person.class);
        Person closestPerson = null;
        double shortestDistance = Double.MAX_VALUE;

        for (Person person : people) {
            if (person.getStatus() == InfectionStatus.INFECTED
                    && !person.isTreatmentPeriod()) {
                double distance = getDistance(person);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closestPerson = person;
                }
            }
        }

        return closestPerson;
    }

    /**
     * Menghitung jarak Euclidean ke Person menggunakan rumus Pythagoras.
     *
     * @param person Person yang ingin diukur jaraknya
     * @return jarak dalam pixel
     */
    public double getDistance(Person person) {
        int dx = getX() - person.getX();
        int dy = getY() - person.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Menggerakkan Ambulance bebas menuju Person INFECTED terdekat,
     * 8 pixel per cycle. Lebih cepat dari Ambulance zigzag (10 pixel)
     * agar bisa mengejar target yang bergerak. Diam di tempat jika
     * tidak ada target.
     */
    @Override
    public void move() {
        Person target = findNearestInfected();
        if (target != null) {
            turnTowards(target.getX(), target.getY());
            move(8);
        }
    }

    /**
     * No-op — AmbulanceSmartPathfinding tidak memakai sistem 4-arah
     * maupun konsep "mentok di tepi". Diimplementasikan hanya karena
     * diwajibkan oleh Ambulance sebagai abstract method.
     */
    @Override
    public void turnDirection() {}
}