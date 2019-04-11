package control;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * General mouse drag handling for all mouse modes implemented in Gravel
 * this class is abstract, so this can't be an mouse mode itself.
 * Handles the general mouse movement actions such as shift-drag on background
 * @author Ronny Bergmann
 *
 */
public interface DragMouseHandler extends MouseListener, MouseMotionListener 
{

	Point getMouseOffSet();
	/**
	 * Indicated whetther someoe is just dragging or not
	 * @return
	 */
  boolean dragged();
	/** set whether the nodes are set to a gridpoint after dragging or not. Handler must not implement this
	 * 
	 */
  void setGridOrientated(boolean b);
	/** update Gridinfo
	 * 
	 */
  void setGrid(int x, int y);
	/**
	 * For Displaying the Selection Rectangle
	 * 
	 * @return the rectangle if it exists, else null
	 */
  Rectangle getSelectionRectangle();
	/**
	 * RemoveObservable Stuff
	 */
  void removeGraphObservers();
}
