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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.Cursor;

/**
 *
 * @author s4m20
 */
public class SpritesheetPanel extends GraphicPanel
{
    private PixelBuffer _pix;
    private SpritesheetController _controller;
    private ImageModifiedListener _imgModifiedListener;

    public SpritesheetPanel()
    {
        super();
        _pix = null;
        _controller = null;
        
        setEnabled(false);
    }

    public void setController(SpritesheetController aController)
    {
        _controller = aController;
    }

    @Override
    protected void draw(Graphics g)
    {          
        super.draw(g);
        
        if (_pix != null)
        {
            Point actualSize = actualImageSize();
            Point origin = getOrigin();
            Rectangle r = new Rectangle(origin.x, origin.y, actualSize.x, actualSize.y);
          
            this.drawCheckerBoardBuffer(g, r);
           
            g.drawImage(_pix.getImage(), r.x, r.y, r.width, r.height, this);            
        }
    }

    public void enterColourPickerMode(ImageModifiedListener aListener)
    {
        _imgModifiedListener = aListener;
        _bAllowsEditing = false;
        this.setCursor(Cursor.CROSSHAIR_CURSOR);        
    }

    public BufferedImage getImage()
    {
        if (_pix == null)
            return null;
        
        return _pix.getImage();
    }

    public void setImage(BufferedImage aImage)
    {
        if (aImage == null)
        {
            _pix = null;
            this.setEnabled(false);
            return;
        }
        
        setGraphicsBounds(new Rectangle(0, 0, aImage.getWidth(), aImage.getHeight()));        

        _pix = new PixelBuffer(aImage);
    }

    @Override
    public void setZoom(float aZoom)
    {
        if (_pix == null)
        {
            super.setZoom(aZoom);
            return;
        }
        
        Point oldImgSize = actualImageSize();
        super.setZoom(aZoom);
        Point imgSize = actualImageSize();

        int xDiff = imgSize.x - oldImgSize.x;
        int yDiff = imgSize.y - oldImgSize.y;

        getOrigin().x -= xDiff >> 1;
        getOrigin().y -= yDiff >> 1;
    }

    public Rectangle suggestVisibleSpriteRect()
    {        
        final int edgeInsetPix = 0;
        Rectangle r = new Rectangle(
                (int)(-getOrigin().getX() / getZoom()) + edgeInsetPix,
                (int)(-getOrigin().getY() / getZoom()) + edgeInsetPix,
                (int)(80 / getZoom()),
                (int)(80 / getZoom()) );

        if (r.x < 0)
            r.x = 0;
        if (r.y < 0)
            r.y = 0;

        GraphicObject selectedGraphic = null;
        java.util.ArrayList<GraphicObject> selectedGraphics = this.selectedGraphics();
        if (selectedGraphics != null && !selectedGraphics.isEmpty())
        {
            selectedGraphic = selectedGraphics.get(selectedGraphics.size() - 1);
        }

        if (selectedGraphic == null)
        {
            while(r.width > _pix.getImage().getWidth())
            {
                r.width /= 2;
                r.width -= 1;
            }
            while(r.height > _pix.getImage().getHeight())
            {
                r.height /= 2;
                r.height -= 1;
            }
        }
        else
        {
            Rectangle lastRect = selectedGraphic.getRect();

            r.width = lastRect.width;
            r.height = lastRect.height;
            r.x = lastRect.x;
            r.y = lastRect.y;

            if (lastRect.x + lastRect.width*2 < _pix.getImage().getWidth())
            {
                r.x +=  lastRect.width;
                r.y = lastRect.y;
            }
            else
            {
                // row end, start new row, autoscroll the view

                // search backwards for first in this size group       
                GraphicObject found = null;
                if (selectedGraphics != null && !selectedGraphics.isEmpty())
                    found = this.selectedGraphics().get(0);
                
                if (found == null)
                    found = _lastAddedGraphic;
                if (found != null)
                {
                    for (int i=_drawStack.indexOf(found); i<_drawStack.size(); i++)
                    {
                        Rectangle rect = _drawStack.get(i).getRect();
                        if (r.width == rect.width && r.height == rect.height && r.y == rect.y)
                        {
                            found = _drawStack.get(i);
                        }
                        else
                            break;
                    }
                    Rectangle firstRect = found.getRect();
                    if (firstRect.y + firstRect.height < _pix.getImage().getHeight())
                    {
                        r.x = firstRect.x;
                        r.y = firstRect.y +  firstRect.height;
                    }


                    // TODO: not centring properly
                    this.setOrigin(new Point(
                            (int)(((this.getWidth()-_pix.getImage().getWidth()/2)/2)- (r.x) * this.getZoom()),
                            (int)(((this.getHeight()-_pix.getImage().getHeight()/2)/2)- (r.y) * this.getZoom())
                            ));
                }
            }
        }

        if (r.width <= 0)
            r.width = 1;
        if (r.height <= 0)
            r.height = 1;

        if (r.x + r.width > _pix.getImage().getWidth())
            r.x = _pix.getImage().getWidth() - r.width;
        if (r.y + r.height > _pix.getImage().getHeight())
            r.y = _pix.getImage().getHeight() - r.height;
        
        return r;
    }

    private Point actualImageSize()
    {       
        return MathUtil.multiplyPoint(
                new Point(_pix.getImage().getWidth(), _pix.getImage().getHeight()),
                getZoom() );         
    }   

    

    @Override
    public void mouseClicked(MouseEvent e)
    {
        super.mouseClicked(e);

        Point p = e.getPoint();
        int x = (int)((p.x  - _origin.x) / _zoom);
        int y = (int)((p.y  - _origin.y) / _zoom);

        if (!_bAllowsEditing)
        {
            if (_imgModifiedListener != null)
            {
                if (x >= 0 && y >= 0 && x < _pix.getImage().getWidth() && y < _pix.getImage().getHeight()
                        && e.getButton() == MouseEvent.BUTTON1)
                {
                    _pix.makeColourTransparent(_pix.pixelAt(x, y));
                    _imgModifiedListener.imageModified();
                }

                _imgModifiedListener = null;
                _bAllowsEditing = true;
                setCursor(Cursor.DEFAULT_CURSOR);
            }        
        }
        else if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (e.getClickCount() == 2)
            {
                if (_pix.hasTransparentPixels())
                {
                   Rectangle r = _pix.getRectAroundSpriteAtPoint(new Point(x, y));

                    if (r.width > 0 && r.height > 0)
                        _controller.addSpriteAt(r);
                }
            }
        }
        
        repaint();
    }

    public void graphicSelectionChanged(GraphicPanel aPanel, GraphicObject aGraphic)
    {
        bringGraphicToFront(aGraphic);
    }
    
    public boolean hasTransparentPixels()
    {
        return _pix.hasTransparentPixels();
    }
}
