import greenfoot.*;

/**
 * AmbulanceZigZagVertikal bergerak dalam pola zigzag dengan sumbu
 * utama vertikal: naik-turun (NORTH/SOUTH) sampai tepi, lalu bergeser
 * 30 pixel secara horizontal (EAST/WEST) sebelum berbalik arah vertikal.
 * Pola ini memastikan Ambulance menyusuri seluruh world secara sistematis.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class AmbulanceZigZagVertikal extends Ambulance
{
    /** Arah horizontal pergeseran "zig" saat ini (EAST atau WEST).
     *  Dibalik oleh setHorizontalDirection() saat mentok di tepi horizontal. */
    private Direction horizontalDirection = Direction.EAST;

    /** Arah vertikal gerak utama saat ini (SOUTH atau NORTH).
     *  Dibalik setiap kali turnDirection() dipanggil. */
    private Direction vertikalDirection = Direction.SOUTH;

    /**
     * Membuat AmbulanceZigZagVertikal baru, mulai bergerak ke SOUTH.
     */
    public AmbulanceZigZagVertikal() {
        super(Direction.SOUTH);
        setDirection(Direction.SOUTH);
    }

    /**
     * Dipanggil saat mentok di tepi vertikal. Bergeser 30 pixel ke
     * arah horizontal saat ini, lalu membalik arah vertikal utama.
     * setHorizontalDirection() dipanggil dulu untuk memastikan arah
     * horizontal tidak mentok di tepi world.
     */
    @Override
    public void turnDirection() {
        setHorizontalDirection();
        setDirection(horizontalDirection);
        move(30);
        vertikalDirection = (vertikalDirection == Direction.SOUTH)
                            ? Direction.NORTH : Direction.SOUTH;
        setDirection(vertikalDirection);
    }

    /**
     * Membalik horizontalDirection jika arah saat ini sudah mentok
     * di tepi world. Mengecek tanpa benar-benar berpindah — arah hadap
     * dikembalikan ke semula setelah pengecekan.
     */
    public void setHorizontalDirection() {
        Direction current = getDirection();

        setDirection(Direction.EAST);
        boolean canGoEast = canMove();

        setDirection(Direction.WEST);
        boolean canGoWest = canMove();

        setDirection(current);

        if (horizontalDirection == Direction.EAST && !canGoEast) {
            horizontalDirection = Direction.WEST;
        } else if (horizontalDirection == Direction.WEST && !canGoWest) {
            horizontalDirection = Direction.EAST;
        }
    }
}