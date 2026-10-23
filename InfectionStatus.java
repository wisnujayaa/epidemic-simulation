/**
 * InfectionStatus merepresentasikan tiga kemungkinan status infeksi
 * seorang Person dalam model epidemi SIR: SUSCEPTIBLE (rentan, belum
 * pernah terinfeksi), INFECTED (sedang terinfeksi), dan IMMUNE (sudah
 * sembuh dan kebal). Setiap status membawa nama file gambar yang
 * sesuai, dipakai oleh Person.setStatus() untuk mengganti tampilan
 * visual Person sesuai statusnya saat ini.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public enum InfectionStatus  
{
    SUSCEPTIBLE("susceptible.png"),
    INFECTED("infected.png"),
    IMMUNE("immune.png");

    /** Nama file gambar yang merepresentasikan status ini secara visual. */
    private final String infectionImg;

    /**
     * Membuat konstanta enum dengan nama file gambar yang sesuai.
     *
     * @param infectionImg nama file gambar untuk status ini
     */
    private InfectionStatus(String infectionImg) {
        this.infectionImg = infectionImg;
    }

    /**
     * Mengembalikan nama file gambar yang merepresentasikan status ini.
     *
     * @return nama file gambar
     */
    public String getInfectionImage() {
        return infectionImg;
    }
}