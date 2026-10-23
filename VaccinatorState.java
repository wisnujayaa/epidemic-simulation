/**
 * VaccinatorState merepresentasikan tiga kemungkinan keadaan seorang
 * Vaccinator dalam siklus persediaan vaksinnya:
 *   - NORMAL    : berkeliling secara acak, mencoba memvaksin Person
 *                 Susceptible yang ditemui
 *   - MENUJU_RS : stok vaksin habis, bergerak terarah menuju Hospital
 *                 terdekat untuk mengisi ulang persediaan
 *   - RESUPPLY  : sudah tiba di Hospital, diam selama beberapa cycle
 *                 sambil mengisi ulang stok vaksin
 *
 * Dipakai sebagai pengganti pendekatan boolean ganda, karena enum
 * memastikan Vaccinator hanya bisa berada pada tepat satu dari ketiga
 * keadaan ini di satu waktu, mencegah kombinasi keadaan yang tidak
 * valid (misalnya "sedang resupply" tapi "belum kehabisan vaksin")
 * yang bisa terjadi jika dipakai dua boolean terpisah yang harus
 * dijaga konsistensinya secara manual.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public enum VaccinatorState {
    NORMAL,
    MENUJU_RS,
    RESUPPLY
}