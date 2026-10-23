import greenfoot.*;

/**
 * Ambulance adalah base class abstract untuk semua kendaraan medis
 * dalam simulasi. Menyediakan sistem gerak 4-arah (NORTH/SOUTH/EAST/WEST)
 * yang dipakai bersama oleh AmbulanceZigZagHorizontal dan
 * AmbulanceZigZagVertikal, sementara AmbulanceSmartPathfinding
 * meng-override move() total dengan gerak bebas menuju target.
 *
 * Setiap cycle, act() mengecek apakah Ambulance masih bisa maju —
 * jika tidak (mentok di tepi), turnDirection() dipanggil untuk menentukan
 * arah baru sebelum move() dieksekusi. Logika turnDirection() berbeda
 * tiap subclass sesuai pola pergerakannya.
 *
 * Person mendeteksi Ambulance lewat getObjectsInRange(Ambulance.class)
 * secara polymorphic — semua subclass terdeteksi tanpa perlu tahu
 * tipe konkretnya.
 *
 * @author Muhammad Wisnu Jaya Wardana
 * @version 1.0
 */
public abstract class Ambulance extends Actor implements Movable
{
    /** Arah hadap Ambulance saat ini. Dipakai oleh canMove() dan move()
     *  untuk menentukan sumbu dan arah pergerakan. Selalu diubah lewat
     *  setDirection() agar rotasi gambar ikut terupdate. */
    private Direction direction;

    /**
     * Membuat Ambulance baru dengan arah awal tertentu dan gambar
     * ambulance yang diskalakan ke 60x40 pixel.
     *
     * @param direction arah hadap awal Ambulance
     */
    public Ambulance(Direction direction) {
        this.direction = direction;
        GreenfootImage img = new GreenfootImage("ambulance.png");
        img.scale(60, 40);
        setImage(img);
    }

    /**
     * Dipanggil otomatis tiap cycle. Jika mentok di tepi world
     * (canMove() == false), turnDirection() dipanggil dulu sebelum
     * move() — memastikan Ambulance selalu bergerak setiap cycle.
     */
    public void act() {
        if (!canMove()) {
            turnDirection();
        }
        move();
    }

    /**
     * Mengubah arah hadap Ambulance sekaligus memutar gambar agar
     * tampilannya sesuai arah gerak. Selalu gunakan method ini —
     * jangan set field direction langsung.
     *
     * @param direction arah hadap baru
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
        if (direction == Direction.SOUTH) setRotation(90);
        else if (direction == Direction.NORTH) setRotation(270);
        else if (direction == Direction.EAST) setRotation(0);
        else if (direction == Direction.WEST) setRotation(180);
    }

    /** @return arah hadap Ambulance saat ini */
    public Direction getDirection() { return direction; }

    /**
     * Memutar arah hadap 90 derajat berlawanan arah jarum jam.
     * SOUTH → EAST → NORTH → WEST → SOUTH.
     */
    public void turnLeft() {
        if (direction == Direction.SOUTH) setDirection(Direction.EAST);
        else if (direction == Direction.EAST) setDirection(Direction.NORTH);
        else if (direction == Direction.NORTH) setDirection(Direction.WEST);
        else if (direction == Direction.WEST) setDirection(Direction.SOUTH);
    }

    /**
     * Memutar arah hadap 90 derajat searah jarum jam.
     * SOUTH → WEST → NORTH → EAST → SOUTH.
     */
    public void turnRight() {
        if (direction == Direction.SOUTH) setDirection(Direction.WEST);
        else if (direction == Direction.WEST) setDirection(Direction.NORTH);
        else if (direction == Direction.NORTH) setDirection(Direction.EAST);
        else if (direction == Direction.EAST) setDirection(Direction.SOUTH);
    }

    /**
     * Mengecek apakah Ambulance masih bisa maju 10 pixel ke arah
     * hadapnya sekarang tanpa keluar dari batas world.
     *
     * @return true jika masih ada ruang untuk maju
     */
    public boolean canMove() {
        int x = getX();
        int y = getY();
        int yMax = getWorld().getHeight();
        int xMax = getWorld().getWidth();

        if (direction == Direction.SOUTH) return y + 10 < yMax;
        else if (direction == Direction.NORTH) return y - 10 >= 0;
        else if (direction == Direction.EAST) return x + 10 < xMax;
        else if (direction == Direction.WEST) return x - 10 >= 0;
        else return false;
    }

    /**
     * Menggerakkan Ambulance maju 10 pixel ke arah hadapnya sekarang.
     * Implementasi dari interface Movable; dipakai bersama oleh
     * AmbulanceZigZagHorizontal dan AmbulanceZigZagVertikal.
     * Di-override total oleh AmbulanceSmartPathfinding.
     */
    @Override
    public void move() {
        int x = getX();
        int y = getY();
        if (direction == Direction.SOUTH) setLocation(x, y + 10);
        else if (direction == Direction.NORTH) setLocation(x, y - 10);
        else if (direction == Direction.EAST) setLocation(x + 10, y);
        else if (direction == Direction.WEST) setLocation(x - 10, y);
    }

    /**
     * Menentukan arah baru saat Ambulance mentok di tepi world.
     * Wajib diimplementasikan tiap subclass karena pola zigzag
     * horizontal dan vertikal punya aturan belok yang berbeda.
     * AmbulanceSmartPathfinding mengimplementasikan ini sebagai
     * no-op karena tidak memakai sistem 4-arah sama sekali.
     */
    public abstract void turnDirection();
}