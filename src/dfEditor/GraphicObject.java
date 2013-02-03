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

import java.awt.*;

/**
 *
 * @author s4m20
 */
public abstract class GraphicObject extends java.util.Observable
{
    public enum Anchor { TOP_LEFT, CENTRE }
    
    protected Rectangle _rect;
    protected boolean _bResizable;
    protected boolean _bSelected;
    protected Rectangle _bounds;
    protected Rectangle _savedRect;
    protected String _description = null;
    protected float _angle;
    protected float _savedAngle;
    protected Anchor _anchor;

    public GraphicObject(Rectangle aRect)
    {
        _angle = 0;
        _bResizable = false;
        _bSelected = false;
        setRect(aRect);
        saveRect();
        saveAngle();
        _anchor = Anchor.TOP_LEFT;
        
        _description = super.toString();
    }
    
    public void setAnchor(Anchor aAnchor)
    {
        _anchor = aAnchor;
    }
    
    public Anchor getAnchor()
    {
        return _anchor;
    }
    
    public void draw(Graphics g, Point aOffset, float aScale, float aAlpha, boolean bDrawSelectedMode)
    {
        Graphics2D g2d = (Graphics2D)g;
        
        Composite backupComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, aAlpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);            
        
        this.draw(g, aOffset, aScale, bDrawSelectedMode);
        
        g2d.setComposite(backupComposite);
    }

    public abstract void draw(Graphics g, Point aOffset, float aScale, boolean bDrawSelectedMode);
    
    public abstract GraphicObject copy();
    
    private void changed()
    {
        setChanged();
        notifyObservers();
    }

    public boolean hasMoved()
    {
        return ! (_savedRect.equals(_rect));
    }

    public void setAngle(float aAngle)
    {
        _angle = aAngle;
    }
    
    public float getAngle()
    {
        return _angle;
    }
    
    public void setRect(Rectangle aRect)
    {        
        _rect = new Rectangle(aRect.x, aRect.y, aRect.width, aRect.height);

        constrainToBounds(_bounds);

        changed();
    }
    
    public void setBounds(Rectangle aBounds)
    {
        _bounds = aBounds;
    }

    public void saveRect()
    {
        Rectangle r = getRect();
        _savedRect = new Rectangle(r.x, r.y, r.width, r.height);
    }
    
    public void saveAngle()
    {
        _savedAngle = getAngle();
    }

    public Rectangle getRect()
    {
        return _rect;
    }

    public Rectangle getSavedRect()
    {
        return _savedRect;
    }

    public float getSavedAngle()            
    {
        return _savedAngle;
    }
    
    public void moveFromSavedPoint(Point aDist)
    {
        Rectangle r = getRect();

        r.x = _savedRect.x + aDist.x;
        r.y = _savedRect.y + aDist.y;
        
        constrainToBounds(_bounds);

        changed();
    }

    public void resizeFromSavedPoint(Point aVec, int aDir)
    {
        if ((aDir & Compass.NORTH) != 0)
        {
            int startY = _rect.y;
            _rect.y = _savedRect.y + aVec.y;           
                
            if (_rect.y > _savedRect.y + _savedRect.height - 1)
                _rect.y = _savedRect.y + _savedRect.height - 1;
            else if (_rect.y < _bounds.y)
                _rect.y = _bounds.y;

            _rect.height += (startY - _rect.y);
        }
        else if ((aDir & Compass.SOUTH) != 0)
        {
            int y = _savedRect.height + aVec.y;
            if (y >  _bounds.height - _rect.y)
                y = _bounds.height - _rect.y;
           
            _rect.height = y;
        }

        if ((aDir & Compass.WEST) != 0)
        {
            int startX = _rect.x;
            _rect.x = _savedRect.x + aVec.x;

            if (_rect.x > _savedRect.x + _savedRect.width - 1)
                _rect.x = _savedRect.x + _savedRect.width - 1;
            else if (_rect.x < _bounds.x)
                _rect.x = _bounds.x;

            _rect.width += (startX - _rect.x);
        }
        else if ((aDir & Compass.EAST) != 0)
        {
            int x = _savedRect.width + aVec.x;
            if (x >  _bounds.width - _rect.x)
                x = _bounds.width - _rect.x;
            _rect.width = x;
        }

        constrainToBounds(_bounds);

        changed();
    }

    public boolean isResizable()
    {
        return _bResizable;
    }

    public void setResizable(boolean aResizable)
    {
        _bResizable = aResizable;
    }

    public boolean isSelected()
    {
        return _bSelected;
    }

    public void setSelected(boolean aSelected)
    {
        _bSelected = aSelected;

        changed();
    }

    public void constrainToBounds(Rectangle aBounds)
    {
        if (aBounds == null)
            return;
        
        Rectangle r = this.getRect();

        if (r.width < 1)
            r.width = 1;
        else if (r.width > aBounds.width)
            r.width = aBounds.width;

        if (r.height < 1)
            r.height = 1;
        else if (r.height > aBounds.height)
            r.height = aBounds.height;

        if (r.x < 0)
            r.x = 0;
        else if (r.x + r.width > aBounds.width)
            r.x = aBounds.width - r.width;

        if (r.y < 0)
            r.y = 0;
        else if (r.y + r.height > aBounds.height)
            r.y = aBounds.height - r.height;
    }

    public void setDescription(final String aDesc)
    {
        _description = aDesc;
    }

    @Override
    public String toString()
    {
        return _description;
    }
}
