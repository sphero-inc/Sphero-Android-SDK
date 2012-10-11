package orbotix.robot.widgets;

/**
 * A type that contains WidgetParts
 *
 * Created by Orbotix Inc.
 * Date: 11/22/11
 * @author Adam Williams
 */
public interface WidgetGraphic {

    /**
     * Adds a WidgetGraphicPart to this WidgetGraphic
     * @param part
     */
    public void addWidgetPart(WidgetGraphicPart part);

    /**
     * Sets all WidgetParts in this WidgetGraphic to show
     */
    public void showAllWidgetParts();

    /**
     * Sets all WidgetParts in this WidgetGraphic to hide
     */
    public void hideAllWidgetsParts();
}
