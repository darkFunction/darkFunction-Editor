package dfEditor.animation;

/**
 *
 * @author s4m20
 */
public interface AnimationDataListener
{
    public void cellAdded(Animation aAnimation, AnimationCell aCell);
    public void cellRemoved(Animation aAnimation, AnimationCell aCell);
    public void cellOrderChanged(Animation aAnimation);
}
