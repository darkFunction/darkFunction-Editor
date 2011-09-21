package dfEditor;

/**
 *
 * @author s4m20
 */
public class Compass
{
    public static final int NONE    = (0);
    public static final int EAST    = (1 << 1);
    public static final int WEST    = (1 << 2);
    public static final int NORTH   = (1 << 3);
    public static final int SOUTH   = (1 << 4);

    public static final int NORTH_EAST = NORTH | EAST;
    public static final int NORTH_WEST = NORTH | WEST;
    public static final int SOUTH_EAST = SOUTH | EAST;
    public static final int SOUTH_WEST = SOUTH | WEST; 
}
