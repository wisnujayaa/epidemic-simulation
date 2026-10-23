import greenfoot.*;

/**
 * AmbulanceZigZagHorizontal bergerak dalam pola zigzag dengan sumbu
 * utama horizontal: kiri-kanan (EAST/WEST) sampai tepi, lalu bergeser
 * 30 pixel secara vertikal (SOUTH/NORTH) sebelum berbalik arah horizontal.
 * Merupakan pasangan dari AmbulanceZigZagVertikal dengan sumbu ditukar.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public class AmbulanceZigZagHorizontal extends Ambulance
{
    /** Arah vertikal pergeseran "zig" saat ini (SOUTH atau NORTH).
     *  Dibalik oleh setVerticalDirection() saat mentok di tepi vertikal. */
    private Direction vertikalDirection = Direction.SOUTH;

    /** Arah horizontal gerak utama saat ini (EAST atau WEST).
     *  Dibalik setiap kali turnDirection() dipanggil. */
    private Direction horizontalDirection = Direction.EAST;

    /**
     * Membuat AmbulanceZigZagHorizontal baru, mulai bergerak ke EAST.
     */
    public AmbulanceZigZagHorizontal() {
        super(Direction.EAST);
        setDirection(Direction.EAST);
    }

    /**
     * Dipanggil saat mentok di tepi horizontal. Bergeser 30 pixel ke
     * arah vertikal saat ini, lalu membalik arah horizontal utama.
     * setVerticalDirection() dipanggil dulu untuk memastikan arah
     * vertikal tidak mentok di tepi world.
     */
    @Override
    public void turnDirection() {
        setVerticalDirection();
        setDirection(vertikalDirection);
        move(30);
        horizontalDirection = (horizontalDirection == Direction.EAST)
                               ? Direction.WEST : Direction.EAST;
        setDirection(horizontalDirection);
    }

    /**
     * Membalik vertikalDirection jika arah saat ini sudah mentok
     * di tepi world. Mengecek tanpa benar-benar berpindah — arah hadap
     * dikembalikan ke semula setelah pengecekan.
     */
    public void setVerticalDirection() {
        Direction current = getDirection();

        setDirection(Direction.SOUTH);
        boolean canGoSouth = canMove();

        setDirection(Direction.NORTH);
        boolean canGoNorth = canMove();

        setDirection(current);

        if (vertikalDirection == Direction.SOUTH && !canGoSouth) {
            vertikalDirection = Direction.NORTH;
        } else if (vertikalDirection == Direction.NORTH && !canGoNorth) {
            vertikalDirection = Direction.SOUTH;
        }
    }
}