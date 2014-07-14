/* 
 *  Copyright 2012 Samuel Taylor
 * 
 *  This file is part of darkFunction Editor
 *
 *  darkFunction Editor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  darkFunction Editor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with darkFunction Editor.  If not, see <http://www.gnu.org/licenses/>.
 */

package dfEditor;

import java.util.ArrayList;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import dfEditor.command.*;
import dfEditor.commands.*;
import java.awt.geom.AffineTransform;
import java.awt.KeyboardFocusManager;
import java.awt.Polygon;

/**
 *
 * @author s4m20
 */
public class GraphicPanel extends javax.swing.JDesktopPane implements MouseMotionListener, MouseWheelListener, MouseListener, KeyListener
{
    private static final int SELECT_BUTTON = MouseEvent.BUTTON1;
    private static final int DRAG_BUTTON = MouseEvent.BUTTON3;
    private static final int DRAG_BUTTON_2 = MouseEvent.BUTTON2;

    private static final Color[] checkerBoardCols = {new Color(210,210,210), new Color(255,255,255)};//{new Color(255,200,200), new Color(255,210,210)};
    
    protected ArrayList<GraphicObject> _drawStack;
    protected float _zoom;
    protected Point _origin;
    protected BufferedImage _checkerBoard;
    private Point _lastOrigin;
    private Point _lastClickPoint;
    protected Rectangle _graphicBounds;
    private GraphicObject _resizingGraphic;
    private int _resizeDirection;
    private int _mouseButtonPressed;
    protected CommandManager _cmdManager;
    private ArrayList<GraphicPanelChangeListener> _changeListeners;
    protected boolean _bAllowsEditing;
    protected GraphicObject _lastAddedGraphic;
    protected Rectangle _multiSelectRect;    
    private static final Color _multiSelectFill = new Color(0,0,0,20);
    private GraphicObject _movingGraphic = null;
    private int _keyDeltaX = 0;
    private int _keyDeltaY = 0;    
    
    protected GraphicPanel()
    {
        _drawStack = new ArrayList<GraphicObject>();        
        _graphicBounds = null;
        _resizingGraphic = null;
        _resizeDirection = Compass.NONE;
        _zoom = 1.0f;                
        _origin = new Point(0,0);
        _mouseButtonPressed = MouseEvent.NOBUTTON;
        _cmdManager = null;
        _changeListeners = new ArrayList<GraphicPanelChangeListener>();
        _bAllowsEditing = true;
        _multiSelectRect = null;
        _lastAddedGraphic = null;
      
        listenForEvents(true);
    }

    public void addGraphicChangeListener(GraphicPanelChangeListener aListener)
    {
        _changeListeners.add(aListener);
    }

    public void removeGraphicChangeListener(GraphicPanelChangeListener aListener)
    {
        _changeListeners.remove(aListener);
    }

    private void listenForEvents(boolean bListen)
    {
        if (bListen)
        {
            setFocusable(true);
            addMouseWheelListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
            addKeyListener(this);
        }
        else
        {
            setFocusable(false);
            removeMouseWheelListener(this);
            removeMouseListener(this);
            removeMouseMotionListener(this);
            removeKeyListener(this);
        }
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_DELETE)
        {
            this.removeSelectedGraphics();
        }
        else if (   e.getKeyCode() == KeyEvent.VK_UP 
                 || e.getKeyCode() == KeyEvent.VK_DOWN
                 || e.getKeyCode() == KeyEvent.VK_LEFT
                 || e.getKeyCode() == KeyEvent.VK_RIGHT ) 
        {            
            switch(e.getKeyCode())
            {
                case KeyEvent.VK_LEFT:    
                    _keyDeltaX = -1;
                    break;
                case KeyEvent.VK_RIGHT:
                    _keyDeltaX = 1;
                    break;
                case KeyEvent.VK_UP:
                    _keyDeltaY = -1;
                    break;
                case KeyEvent.VK_DOWN:
                    _keyDeltaY = 1;
                    break;                    
            }
            for (int i=0; i<_drawStack.size(); ++i)
            {
                GraphicObject graphic = _drawStack.get(i);
                if (graphic.isSelected())
                {
                    Rectangle r = graphic.getRect(); 
                    r.x += _keyDeltaX;
                    r.y += _keyDeltaY;
                }
            }     
            if (_keyDeltaX != 0 || _keyDeltaY != 0)
                repaint();
        }
    }
    
    public void keyTyped(KeyEvent e)
    {

    }
    
    public void keyReleased(KeyEvent e)
    {
        if (   e.getKeyCode() == KeyEvent.VK_UP 
                 || e.getKeyCode() == KeyEvent.VK_DOWN
                 || e.getKeyCode() == KeyEvent.VK_LEFT
                 || e.getKeyCode() == KeyEvent.VK_RIGHT ) 
        {
            switch(e.getKeyCode())
            {              
                case KeyEvent.VK_LEFT: 
                case KeyEvent.VK_RIGHT:
                    _keyDeltaX = 0;
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    _keyDeltaY = 0;
                    break;  
            }
            
            dropAllGraphicsUndoable();
        }
    }
    
    @Override public boolean isFocusable()
    {
        return true;
    }
    /*
    @Override
    public void setEnabled(boolean enabled)
    {
        if (enabled == this.isEnabled())
            return;

        listenForMouseEvents(enabled);

        super.setEnabled(enabled);
    }
     * */

    public void setEditable(boolean aEditable)
    {
        _bAllowsEditing = aEditable;
    }


    // TODO: not happy about not being able to pass this in constructor because
    // of Bean requirements... consider Builder pattern?
    public void setCommandManager(CommandManager aCM)
    {
        _cmdManager = aCM;
    }
 
    public void setZoom(float zoom)
    {
        if (zoom < 0.1f)
            zoom = 0.1f;

        this._zoom = zoom;
        
        _lastOrigin = new Point(_origin);

        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        draw(g);
        drawStack(g);
        
        if (_multiSelectRect != null)
        {
            g.setColor(_multiSelectFill);
            g.fillRect(_multiSelectRect.x, _multiSelectRect.y, _multiSelectRect.width, _multiSelectRect.height);
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(_multiSelectRect.x, _multiSelectRect.y, _multiSelectRect.width, _multiSelectRect.height);
            
        }
    }

    protected void draw(Graphics g)
    {
        
    }

    public float getZoom()
    {
        return _zoom;
    }

    // Do not allow graphical objects to go beyond this area.
    // Also stops the panel being dragged into a position where
    // the user cannot see at least some of this area.
    protected void setGraphicsBounds(Rectangle r)
    {
        _graphicBounds = r;
        for (int i=0; i<_drawStack.size(); ++i)
        {
            GraphicObject graphic = _drawStack.get(i);
            graphic.setBounds(r);
            graphic.constrainToBounds(r);
        }        
    }

    protected Rectangle getGraphicsBounds()
    {
        return _graphicBounds;
    }

    public void addGraphic(GraphicObject aGraphic)
    {
        if (aGraphic.getAnchor() == GraphicObject.Anchor.CENTRE)
        {
            Rectangle r = aGraphic.getRect();
            r.x -= r.width/2;
            r.y -= r.height/2;
            aGraphic.saveRect();
        }
        
        _drawStack.add(0, aGraphic); // add to top
        aGraphic.setBounds(_graphicBounds);
        repaint();

        _lastAddedGraphic = aGraphic;

        for (int i=0; i<_changeListeners.size(); ++i)
        {
            _changeListeners.get(i).graphicAdded(this, aGraphic);
        }
    }

    public void removeGraphic(GraphicObject aGraphic)
    {
        if (aGraphic == _lastAddedGraphic)
        {
            _lastAddedGraphic = null;
        }

        _drawStack.remove(aGraphic);
        repaint();

        for (int i=0; i<_changeListeners.size(); ++i)
        {
            _changeListeners.get(i).graphicErased(this, aGraphic);
        }
    }

    protected void drawStack(Graphics g)
    {
        this.drawStack(g, getOrigin(), getZoom(), 1.0f);
    }
    
    protected void drawStack(Graphics g, Point aOrigin, float aZoom, float aAlpha)
    {
        for (int i=_drawStack.size(); --i>=0;)
        {
            GraphicObject graphic = _drawStack.get(i);
            
            drawGraphicRotated(graphic, g, aOrigin, aZoom, aAlpha);
        }
    }
    
    protected void drawGraphicRotated(GraphicObject graphic, Graphics g, Point aOrigin, float aZoom, float aAlpha)            
    {
        Graphics2D g2d = (Graphics2D)g;            
            
        Rectangle graphicRect = graphic.getRect();

        Rectangle r = convertRectToViewRect(graphicRect);          

        AffineTransform transform = new AffineTransform(g2d.getTransform());
        AffineTransform oldTransform = g2d.getTransform();

        transform.rotate(Math.toRadians(graphic.getAngle()), r.x+r.width/2, r.y+r.height/2);

        g2d.setTransform(transform);

        graphic.draw(g2d, aOrigin, aZoom, aAlpha, _bAllowsEditing);

        g2d.setTransform(oldTransform);
    }
    
    protected void setOrigin(Point p)
    {
        _origin = p;
    }

    protected Point getOrigin()
    {
        return _origin;
    }
    
    protected Rectangle convertRectToViewRect(Rectangle aRect)
    {
        return new Rectangle (
                    (int)(aRect.x * _zoom) + _origin.x,
                    (int)(aRect.y * _zoom) + _origin.y,
                    (int)(aRect.width * _zoom),
                    (int)(aRect.height * _zoom) );
    }

    protected Rectangle convertViewRectToRect(Rectangle aRect)
    {
        return new Rectangle (
                    (int)((aRect.x  - _origin.x) / _zoom),
                    (int)((aRect.y  - _origin.y) / _zoom),
                    (int)(aRect.width / _zoom),
                    (int)(aRect.height / _zoom) );
    }

    protected GraphicObject topGraphicAtPosition(Point aPoint)
    {
        for (int i=0; i<_drawStack.size(); ++i)
        {
            GraphicObject graphic = _drawStack.get(i);
            Rectangle r = convertRectToViewRect(graphic.getRect());

            if (MathUtil.pointRectCollide(aPoint, r))
            {
                return graphic;
            }
        }

        return null;
    }

    protected void moveContent(Point aDist, Point aFromPoint)
    {
        _origin.x = aFromPoint.x + aDist.x;
        _origin.y = aFromPoint.y + aDist.y;

        if (_graphicBounds != null)
        {
            Point actualSize = MathUtil.multiplyPoint(
                    new Point(_graphicBounds.width, _graphicBounds.height),
                    _zoom );

            final int BORDER = 20;

            if(_origin.x + actualSize.x < BORDER)
                _origin.x = BORDER - actualSize.x;
            else if(_origin.x > this.getWidth() - BORDER)
                _origin.x = this.getWidth() - BORDER;

            if(_origin.y + actualSize.y < BORDER)
                _origin.y = BORDER - actualSize.y;
            else if(_origin.y > this.getHeight() - BORDER)
                _origin.y = this.getHeight() - BORDER;
        }
    }

    public void unselectAllGraphics()
    {
        for (int i=0; i<_drawStack.size(); ++i)
        {
            GraphicObject graphic = _drawStack.get(i);
            this.selectGraphic(graphic, false);
        }
    }

    private void setCursorToResizeCursor(int aDir)
    {        
        int cursorType = Cursor.DEFAULT_CURSOR;
        
        switch(aDir)
        {
            case Compass.NORTH:
                cursorType = Cursor.N_RESIZE_CURSOR;
                break;
            case Compass.SOUTH:
                cursorType = Cursor.S_RESIZE_CURSOR;
                break;
            case Compass.EAST:
                cursorType = Cursor.E_RESIZE_CURSOR;
                break;
            case Compass.WEST:
                cursorType = Cursor.W_RESIZE_CURSOR;
                break;
            case Compass.NORTH_EAST:
                cursorType = Cursor.NE_RESIZE_CURSOR;
                break;
            case Compass.NORTH_WEST:
                cursorType = Cursor.NW_RESIZE_CURSOR;
                break;
            case Compass.SOUTH_EAST:
                cursorType = Cursor.SE_RESIZE_CURSOR;
                break;
            case Compass.SOUTH_WEST:
                cursorType = Cursor.SW_RESIZE_CURSOR;
                break;
        }

        setCursor(cursorType);

    }

    public void bringGraphicToFront(GraphicObject aGraphic)
    {
        int oldIndex = _drawStack.indexOf(aGraphic);
        _drawStack.add(0, aGraphic);
        _drawStack.remove(oldIndex+1);
    }
    
    public void sendGraphicToBack(GraphicObject aGraphic)
    {
        _drawStack.remove(aGraphic);
        _drawStack.add(aGraphic);
        
    }

    /*
    public void updateDrawStackUsingList(ArrayList<GraphicObject> aNewList)
    {
        // remove anything from the draw stack that is not in the new list
        // (objects that have been deleted from the tree)
        for(int i=0; i<_drawStack.size(); ++i)
        {
            GraphicObject old = _drawStack.get(i);
            boolean bFound = false;

            for(int j=0; j<aNewList.size(); ++j)
            {
                if(old == aNewList.get(j))
                {
                    bFound = true;
                    break;
                }
            }

            if(!bFound)
            {
                _drawStack.remove(old);
                i--;
            }
        }

        // now add anything new into the draw stack
        for(int i=0; i<aNewList.size(); ++i)
        {
            GraphicObject newGraphic = aNewList.get(i);
            boolean bIsNew = true;

            for(int j=0; j<_drawStack.size(); ++j)
            {
                if(newGraphic == _drawStack.get(j))
                {
                    bIsNew = false;
                    break;
                }
            }

            if(bIsNew)
            {
                addGraphic(newGraphic);
            }
        }

        repaint();
    }
     */

    /*
     * Used for when we have manually shifted the rects and want to make it undoable (ie, packing) ???? hmm revisit
     */
    public void dropAllGraphics()
    {
        for (int i=0; i<_drawStack.size(); ++i)
        {
            GraphicObject graphic = _drawStack.get(i);
            this.dropGraphic(graphic, true);
             if (graphic.hasMoved())
                graphic.saveRect();
        }
    }

    protected void dropAllGraphicsUndoable()
    {
        ArrayList<UndoableCommand> commands = new ArrayList<UndoableCommand>();
        
        for (int i=0; i<_drawStack.size(); ++i)
        {
            GraphicObject graphic = _drawStack.get(i);
           
            if (graphic.hasMoved())
            {
                commands.add(new MoveGraphicCommand(this, graphic, graphic.getRect()));                
            }
        }  
        
        if (commands.size() > 0)
        {
            Command groupCommand = new GroupedUndoableCommand(commands);
            if (_cmdManager != null)
                _cmdManager.execute(groupCommand);
            else
                groupCommand.execute();        
        }
    }
    
    protected void dropGraphic(GraphicObject aGraphic, boolean aUndoable)
    {
        Command moveCommand = new MoveGraphicCommand(this, aGraphic, aGraphic.getRect());

        if (aGraphic.hasMoved())
        {           
            if (_cmdManager != null && aUndoable)
                _cmdManager.execute(moveCommand);
            else
                moveCommand.execute();          
        }       
    }
    
    protected void setDrawStack(ArrayList<GraphicObject> aDrawStack)
    {
        _drawStack.clear();

        for (int i=0; i<aDrawStack.size(); ++i)
        {
            _drawStack.add(aDrawStack.get(i));
        }

        repaint();
    }

    public void clear()
    {
        _drawStack.clear();
        repaint();
    }

    protected void setCursor(int aCursorType)
    {
        Container c = this.getRootPane().getContentPane();
        c.setCursor(Cursor.getPredefinedCursor(aCursorType));
    }
    
    public int getResizeDirectionFromPointOnRect(Point aPoint, Rectangle aRect)
    {
        final int BORDER_TOLERANCE = 5;

        int resizeDirection = Compass.NONE;

        Rectangle rectWithBorder = new Rectangle (
                aRect.x - BORDER_TOLERANCE,
                aRect.y - BORDER_TOLERANCE,
                aRect.width + (BORDER_TOLERANCE << 1),
                aRect.height + (BORDER_TOLERANCE << 1) );

        if (MathUtil.pointRectCollide(aPoint, rectWithBorder))
        {
            Rectangle topRect       = new Rectangle( rectWithBorder.x, rectWithBorder.y, rectWithBorder.width, (BORDER_TOLERANCE << 1) );
            Rectangle bottomRect    = new Rectangle( rectWithBorder.x, rectWithBorder.y + aRect.height, rectWithBorder.width, (BORDER_TOLERANCE << 1) );
            Rectangle leftRect      = new Rectangle( rectWithBorder.x, rectWithBorder.y, (BORDER_TOLERANCE << 1), rectWithBorder.height );
            Rectangle rightRect     = new Rectangle( rectWithBorder.x + aRect.width, rectWithBorder.y, (BORDER_TOLERANCE << 1), rectWithBorder.height );

            if (MathUtil.pointRectCollide(aPoint, topRect))
                resizeDirection |= Compass.NORTH;
            else if (MathUtil.pointRectCollide(aPoint, bottomRect))
                resizeDirection |= Compass.SOUTH;

            if (MathUtil.pointRectCollide(aPoint, leftRect))
                resizeDirection |= Compass.WEST;
            else if (MathUtil.pointRectCollide(aPoint, rightRect))
                resizeDirection |= Compass.EAST;

        }

        return resizeDirection;
    }

    public void selectGraphic(GraphicObject aGraphic)
    {
        this.selectGraphic(aGraphic, true);
    }

    public void unselectGraphic(GraphicObject aGraphic)
    {
        this.selectGraphic(aGraphic, false);
    }

    public void selectGraphic(GraphicObject aGraphic, boolean aSelect)
    {
        if (aGraphic != null)
        {
            if (aGraphic.isSelected() != aSelect)
            {
                aGraphic.setSelected(aSelect);
                for (int i=0; i<_changeListeners.size(); ++i)
                {
                    _changeListeners.get(i).graphicSelectionChanged(this, aGraphic);
                }
            }
            if (aSelect)
            {                
                if (aGraphic.isResizable())
                    _resizingGraphic = aGraphic;
            }
        }
    }
    
    public void removeSelectedGraphics()
    {
        ArrayList<GraphicObject> selectedGraphics = selectedGraphics();

        // need to notify change listeners first.. this stuff needs to be tided up
        for (int i=0; i<_changeListeners.size(); ++i)
        {
            _changeListeners.get(i).graphicsErased(this, selectedGraphics);
        }
                
        for (int i=0; i<selectedGraphics.size(); ++i)
        {
            removeGraphic(selectedGraphics.get(i));
        }
    }

    public ArrayList<GraphicObject> selectedGraphics()
    {
        ArrayList<GraphicObject> selectedGraphics = new ArrayList<GraphicObject>();
        for (int i=0; i<_drawStack.size(); ++i)
        {
            if (_drawStack.get(i).isSelected())
            {
                selectedGraphics.add(_drawStack.get(i));
            }
        }

        return selectedGraphics;
    }
    
    protected void drawCheckerBoardBuffer(final Graphics g, final Rectangle aRect)
    {
        if (_checkerBoard == null)
            _checkerBoard = this.makeCheckerBoardBuffer(new Rectangle(0, 0, 256, 256));
        
        g.clipRect(aRect.x, aRect.y, aRect.width, aRect.height);
        for (int x=aRect.x; x<aRect.x + aRect.width; ++x)
        {            
            for (int y=aRect.y; y<aRect.y + aRect.height; ++y)
            {                
                g.drawImage(_checkerBoard, x, y, null);
                y += _checkerBoard.getHeight()-1;
            }
            x += _checkerBoard.getWidth()-1;
        }
        g.setClip(0, 0, this.getWidth(), this.getHeight());
    }

    protected BufferedImage makeCheckerBoardBuffer(Rectangle r)
    {
        BufferedImage img = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);

        if (img != null)
        {
            Graphics gImg = img.getGraphics();
            this.drawTransparencyCheckerBoard(gImg, r);
        }

        return img;
    }

    private void drawTransparencyCheckerBoard(Graphics g, Rectangle r)
    {
        final int SQUARE_SIZE = r.width / 8;

        g.setClip(r.x,r.y,r.width,r.height);
        
        g.setColor(checkerBoardCols[0]);
        g.fillRect(r.x,r.y,r.width,r.height);

        Point squarePoint = new Point(0,0);
        for (int y=0; y<(r.height/SQUARE_SIZE)+1; ++y)
        {
            for (int x=0; x<(r.width/SQUARE_SIZE)+1; ++x)
            {
                int index = (x + (y % checkerBoardCols.length)) % checkerBoardCols.length;
                if (index != 0) //already painted as large bg rect
                {
                    Color curCol = checkerBoardCols[index];
                    g.setColor(curCol);
                    g.fillRect(r.x + squarePoint.x, r.y + squarePoint.y, SQUARE_SIZE, SQUARE_SIZE);
                }
                squarePoint.x += SQUARE_SIZE;
            }
            squarePoint.x = 0;
            squarePoint.y += SQUARE_SIZE;
        }

        g.setClip(null);
    }
    
    public void notifyGraphicMoved(GraphicObject aGraphic)
    {
        for (int i=0; i<_changeListeners.size(); ++i)
        {
            _changeListeners.get(i).graphicMoved(this, aGraphic);
        }
        
        repaint();
    }
            

    /***************************************************
     * Input
     * *************************************************/

    public void mouseMoved(MouseEvent evt)
    {
        if (!_bAllowsEditing)
            return;
        
        Point p = evt.getPoint();

        if (_resizingGraphic != null)
        {
            _resizeDirection = getResizeDirectionFromPointOnRect(p, convertRectToViewRect(_resizingGraphic.getRect()));
            setCursorToResizeCursor(_resizeDirection);            
        }
        else
        {
            _resizeDirection = Compass.NONE;
            setCursor(Cursor.DEFAULT_CURSOR);
        }

    }

    public void mouseDragged(MouseEvent evt)
    {
        if (_lastClickPoint == null)
            return;
        
        Point p = evt.getPoint();
        Point dist =  new Point(p.x - _lastClickPoint.x, p.y - _lastClickPoint.y);        

        switch(_mouseButtonPressed)
        {
            case DRAG_BUTTON:
            case DRAG_BUTTON_2:
            {
                moveContent(dist, _lastOrigin);
                
            } break;

            case SELECT_BUTTON:
            {
                if (System.getProperty("os.name").startsWith("Mac OS X") &&
                        (evt.getModifiers() & ActionEvent.META_MASK) != 0)
                {
                    moveContent(dist, _lastOrigin);
                    break;
                }
                if (!_bAllowsEditing)
                    break;
                
                if (_resizingGraphic != null && _resizeDirection != Compass.NONE)
                {
                    Point resizeDist = MathUtil.dividePoint(dist, _zoom);
                    _resizingGraphic.resizeFromSavedPoint(resizeDist, _resizeDirection);
                }
                else if (_multiSelectRect != null)
                {
                    int topX = Math.min(p.x, _lastClickPoint.x);
                    int topY = Math.min(p.y, _lastClickPoint.y);
                    int bottomX = Math.max(p.x, _lastClickPoint.x);
                    int bottomY = Math.max(p.y, _lastClickPoint.y);
                    
                    _multiSelectRect = new Rectangle(topX, topY, bottomX - topX, bottomY - topY);

                }
                else
                {                    
                    for (int i=0; i<_drawStack.size(); ++i)
                    {
                        GraphicObject graphic = _drawStack.get(i);

                        if (graphic.isSelected())
                        {
                            graphic.moveFromSavedPoint(MathUtil.dividePoint(dist, _zoom));
                        }
                    }
                }
               
            } break;
        }

        repaint();
    }

    public void mousePressed(MouseEvent evt)
    {          
        requestFocusInWindow();
                
        _lastClickPoint = evt.getPoint();
        _lastOrigin = new Point(_origin);
        _mouseButtonPressed = evt.getButton();

        switch(evt.getButton())
        {
            case SELECT_BUTTON:
            {
                if (!_bAllowsEditing)
                    break;

                if (_resizingGraphic != null && _resizeDirection != Compass.NONE)
                    break;

                _resizingGraphic = null;

                _movingGraphic = topGraphicAtPosition(evt.getPoint());

                if ((evt.getModifiers() & ActionEvent.CTRL_MASK) == 0)
                {
                    if (_movingGraphic == null || !_movingGraphic.isSelected())
                        unselectAllGraphics();
                }
                
                if (System.getProperty("os.name").startsWith("Mac OS X") &&
                        (evt.getModifiers() & ActionEvent.META_MASK) != 0)
                    setCursor(Cursor.HAND_CURSOR);
                
                if (_movingGraphic != null)
                {
                    if (!_movingGraphic.isSelected())                                            
                        selectGraphic(_movingGraphic);
                    else if ((evt.getModifiers() & ActionEvent.CTRL_MASK) != 0)
                        unselectGraphic(_movingGraphic);
                }
                else
                {
                    Point  p = evt.getPoint();
                    _multiSelectRect = new Rectangle(p.x, p.y, 0, 0);
                }                
            } break;

            case DRAG_BUTTON:
            case DRAG_BUTTON_2:
            {
                setCursor(Cursor.HAND_CURSOR);
            } break;
        }

        repaint();
    }

    public void mouseWheelMoved(MouseWheelEvent evt)
    {
        setZoom(getZoom() - (evt.getWheelRotation() * 0.2f));
    }

    public void mouseReleased(MouseEvent evt)
    {
        setCursor(Cursor.DEFAULT_CURSOR);
        _mouseButtonPressed = MouseEvent.NOBUTTON;
        
        if (!_bAllowsEditing)
            return;

        switch(evt.getButton())
        {
            case SELECT_BUTTON:
            {
                if (!_bAllowsEditing)
                    break;

                if (_resizingGraphic != null && _resizeDirection != Compass.NONE)
                    break;
                
                if ((evt.getModifiers() & ActionEvent.CTRL_MASK) == 0)
                {
                    if (_movingGraphic == null || !_movingGraphic.hasMoved())
                    {    
                        for (int i=0; i<_drawStack.size(); ++i)
                        {
                            GraphicObject graphic = _drawStack.get(i);
                            if (graphic != _movingGraphic)
                                this.selectGraphic(graphic, false);
                        }    
                    }
                    else
                    {
                        GraphicObject topGraphic = topGraphicAtPosition(evt.getPoint());
                        
                        if (topGraphic != null && topGraphic.isSelected() && !topGraphic.hasMoved())
                            unselectGraphic(topGraphic);
                    }
                }              
                
                if (_multiSelectRect != null && _multiSelectRect.width > 0 && _multiSelectRect.height > 0)
                {
                    // select all graphics in rect
                    Rectangle convertedRect = this.convertViewRectToRect(_multiSelectRect);
                    
                    for (int i=0; i<_drawStack.size(); ++i)
                    {  
                        GraphicObject go = _drawStack.get(i);
                        Rectangle r = go.getRect();                        
                        float angle = go.getAngle();
                        
                        Point[] p = new Point[4];
                        p[0] = MathUtil.rotatePoint2D(new Point(-r.width/2, -r.height/2), angle);
                        p[1] = MathUtil.rotatePoint2D(new Point(-r.width/2, r.height/2), angle);
                        p[2] = MathUtil.rotatePoint2D(new Point(r.width/2, r.height/2), angle);
                        p[3] = MathUtil.rotatePoint2D(new Point (r.width/2, -r.height/2), angle);
                        
                        for (int j=0; j<p.length; j++)
                        {
                            p[j].x += r.x + r.width/2;
                            p[j].y += r.y + r.height/2;
                        }
                        
                        int[] x = new int[p.length];
                        int[] y = new int[p.length];
                        for (int j=0; j<p.length; ++j)
                        {
                            x[j] = p[j].x;
                            y[j] = p[j].y;
                        }
                        Polygon poly = new Polygon(x, y, 4);
                        if (poly.intersects(convertedRect))
                        {
                            selectGraphic(go);
                        }                      
                    }                       
                }
                
            } break;
        }

        dropAllGraphicsUndoable();        
        _multiSelectRect = null;                 
        repaint();
    }

    public void mouseExited(MouseEvent evt)
    {

    }

    public void mouseEntered(MouseEvent evt)
    {

    }

    public void mouseClicked(MouseEvent evt)
    {

    }
}