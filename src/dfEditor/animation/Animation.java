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

package dfEditor.animation;

import java.util.ArrayList;
import dfEditor.CustomComponents.NamedElement;

/**
 *
 * @author s4m20
 */
public class Animation implements NamedElement
{
    private String name = null;
    private ArrayList<AnimationCell> cellList;
    private int currentIndex;
    private ArrayList<AnimationDataListener> animationListeners;
    private int loops = 0;

    public Animation(String aName)
    {
        animationListeners = new ArrayList<AnimationDataListener>();
        setName(aName);
        cellList = new ArrayList<AnimationCell>();
    }
    
    public Animation copy()
    {
        Animation copy = new Animation(this.getName());
        
        for (int i=0; i<cellList.size(); ++i)
        {
            copy.addCell(this.getCellAtIndex(i).copy());
        }
        
        return copy;
    }

    public void setName(String aName)
    {
        name = aName;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return getName();
    }

    public void addCell(AnimationCell aCell)
    {       
        cellList.add(aCell);
        for (int i=0; i<animationListeners.size(); ++i)
            animationListeners.get(i).cellAdded(this, aCell);
         currentIndex = cellList.size() - 1;
    }

    public void moveCell(int aFromIndex, int aToIndex)
    {
        if (aFromIndex < aToIndex)
            aToIndex --;
        
        if (    aFromIndex >= 0 && aFromIndex < cellList.size()
             && aToIndex >= 0 && aToIndex < cellList.size() )
        {
            // swap
            AnimationCell cell = cellList.get(aFromIndex);
            cellList.remove(aFromIndex);
            if (aToIndex < cellList.size())
                cellList.add(aToIndex, cell);
            else
                cellList.add(cell);
            
            for (int i=0; i<animationListeners.size(); ++i)
                animationListeners.get(i).cellOrderChanged(this);
        }
    }

    public void removeCell(AnimationCell aCell)
    {
        int index = cellList.indexOf(aCell);

        cellList.remove(aCell);        
        currentIndex = index-1;

        for (int i=0; i<animationListeners.size(); ++i)
            animationListeners.get(i).cellRemoved(this, aCell);
       
    }

    public int getCurrentCellIndex()
    {
        return currentIndex;
    }

    public void setCurrentCellIndex(int index)
    {
        if (index >= 0 && index < cellList.size())
        {
            currentIndex = index;          
        }
    }

    public void setLoops(final int aLoops)
    {
        this.loops = aLoops;

        if (this.loops < 0)
            this.loops = 0;
    }

    public int getLoops()
    {
        return loops;
    }

    public int numCells()
    {
        if (cellList == null)
            return 0;
        
        return cellList.size();
    }

    public AnimationCell getCurrentCell()
    {
        if (currentIndex >= 0 && cellList.size() > 0)
            return cellList.get(currentIndex);

        return null;
    }

    public AnimationCell getNextCell()
    {
        if (++currentIndex < cellList.size())
          return cellList.get(currentIndex);

        currentIndex--;
        return null;
    }
    
    public AnimationCell getCellAtIndex(int aIndex)
    {
        if (aIndex < 0 || aIndex >= cellList.size())
            return null;
        return cellList.get(aIndex);
    }
    
    public int indexOfCell(AnimationCell aCell)
    {
        if (cellList.contains(aCell))
        {
            return cellList.indexOf(aCell);
        }
        return -1;
    }

    public void addAnimationListener(AnimationDataListener aListener)
    {
        animationListeners.add(aListener);
    }

    public void removeAnimationListener(AnimationDataListener aListener)
    {
        animationListeners.remove(aListener);
    }
}
