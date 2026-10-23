/**
 * Movable adalah interface yang mewajibkan implementasinya menyediakan
 * cara untuk bergerak (move()) dan menentukan kapan/ke arah mana harus
 * berbelok (turnDirection()). Diimplementasikan oleh Ambulance (class
 * abstract), yang menyediakan implementasi move() berbasis sistem
 * 4-arah secara bersama untuk seluruh subclassnya, sementara
 * turnDirection() didefinisikan berbeda-beda oleh tiap subclass sesuai
 * pola pergerakannya masing-masing (zigzag horizontal, zigzag
 * vertikal, atau pathfinding cerdas).
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public interface Movable  
{
    /** Menggerakkan objek satu langkah sesuai aturan pergerakannya. */
    void move();
    /** Menentukan arah baru saat objek perlu berbelok. */
    void turnDirection();
}