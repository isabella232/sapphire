/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [419551] ClassCastException in SapphireHotSpotsActionPresentation
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class SapphireHotSpotsActionPresentation extends SapphireActionPresentation
{
    private final Map<SapphireAction,HotSpot> actionToHotSpot;
    private Menu popupMenu;
    
    public SapphireHotSpotsActionPresentation( final SapphireActionPresentationManager manager )
    {
        super( manager );

        this.actionToHotSpot = new HashMap<SapphireAction,HotSpot>();
    }
    
    /**
     * Allows subclasses to register where on the screen a particular action is rendered. This 
     * information is used to display the handlers menu in the correct location. 
     * 
     * @param action the action
     * @param hotspot the hotspot
     */
    
    protected final void registerHotSpot( final SapphireAction action,
                                          final HotSpot hotspot )
    {
        this.actionToHotSpot.put( action, hotspot );
    }
    
    public final boolean displayActionHandlerChoice( final SapphireAction action )
    {
        final HotSpot hotspot = this.actionToHotSpot.get( action );
        
        if( hotspot == null )
        {
            return false;
        }
        
        if( this.popupMenu != null )
        {
            this.popupMenu.dispose();
            this.popupMenu = null;
        }
        
        this.popupMenu = new Menu( ( (SwtPresentation) getManager().context() ).shell(), SWT.POP_UP );
        
        for( final SapphireActionHandler handler : action.getEnabledHandlers() )
        {
            renderMenuItem( this.popupMenu, handler );
        }
        
        final Rectangle bounds = hotspot.getBounds();
        final Point pt = new Point( bounds.x, bounds.y + bounds.height );
        
        this.popupMenu.setLocation( pt );
        this.popupMenu.setVisible( true );
        
        return true;
    }
    
    public static abstract class HotSpot
    {
        public abstract Rectangle getBounds();

        protected Rectangle toDisplay( final Control parent,
                                       final Rectangle bounds )
        {
            final Point location = parent.toDisplay( bounds.x, bounds.y );
            return new Rectangle( location.x, location.y, bounds.width, bounds.height );
        }
    }
    
    public static class ControlHotSpot
    
        extends HotSpot
        
    {
        private final Control control;
        
        public ControlHotSpot( final Control control )
        {
            this.control = control;
        }
    
        @Override
        public Rectangle getBounds()
        {
            return toDisplay( this.control.getParent(), this.control.getBounds() );
        }
    }
    
}
