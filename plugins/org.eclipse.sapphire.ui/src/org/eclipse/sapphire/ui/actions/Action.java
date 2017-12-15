/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class Action
{
    public enum Type
    {
        PUSH,
        TOGGLE
    }
    
    public static abstract class Listener
    {
        public void handleActionChanged( final Action action )
        {
        }
        
        public void handleActionExecuted( final Action action,
                                          final Object result )
        {
        }
    }
    
    private String id = null;
    private String commandId = null;
    private Type type = Type.PUSH;
    private String label = null;
    private ImageDescriptor imageDescriptor = null;
    private Image image = null;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean checked = false;
    private ISapphirePart part = null;
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<Listener>();
    
    private final List<ActionGroup> childActionGroups = new ArrayList<ActionGroup>();
    
    private final List<ActionGroup> childActionGroupsReadOnly 
        = Collections.unmodifiableList( this.childActionGroups );
    
    public String getId()
    {
        return this.id;
    }
    
    public void setId( final String id )
    {
        this.id = id;
    }
    
    public String getCommandId()
    {
        return this.commandId;
    }
    
    public void setCommandId( final String commandId )
    {
        this.commandId = commandId;
    }
    
    public Type getType()
    {
        return this.type;
    }
    
    public void setType( final Type type )
    {
        this.type = type;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    
    public void setLabel( final String label )
    {
        this.label = label;
    }
    
    public Image getImage()
    {
        return getImage( false );
    }
    
    public Image getImage( final boolean useDefaultIfNecessary )
    {
        if( this.image == null )
        {
            ImageDescriptor imageDescriptor = this.imageDescriptor;
            
            if( imageDescriptor == null && useDefaultIfNecessary )
            {
                imageDescriptor = SapphireImageCache.ACTION_DEFAULT;
            }
            
            if( imageDescriptor != null )
            {
                this.image = getPart().getImageCache().getImage( imageDescriptor );
            }
        }
        
        return this.image;
    }
    
    public ImageDescriptor getImageDescriptor()
    {
        return this.imageDescriptor;
    }
    
    public void setImageDescriptor( final ImageDescriptor imageDescriptor )
    {
        this.imageDescriptor = imageDescriptor;
    }
    
    public boolean isVisible()
    {
        return this.visible;
    }
    
    public void setVisible( final boolean visible )
    {
        this.visible = visible;
    }
    
    public boolean isEnabled()
    {
        return this.enabled;
    }
    
    public void setEnabled( final boolean enabled )
    {
        this.enabled = enabled;
    }
    
    public boolean isChecked()
    {
        return this.checked;
    }
    
    public void setChecked( final boolean checked )
    {
        this.checked = checked;
    }
    
    public ISapphirePart getPart()
    {
        return this.part;
    }
    
    public void setPart( final ISapphirePart part )
    {
        this.part = part;
        
        for( ActionGroup childActionGroup : this.childActionGroups )
        {
            childActionGroup.setPart( part );
        }
    }

    public void addListener( final Listener listener )
    {
        if( listener != null )
        {
            this.listeners.add( listener );
        }
    }
    
    public void removeListener( final Listener listener )
    {
        this.listeners.remove( listener );
    }
    
    public void notifyChangeListeners()
    {
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handleActionChanged( this );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }
    
    public List<ActionGroup> getChildActionGroups()
    {
        return this.childActionGroupsReadOnly;
    }
    
    public void addChildActionGroup( final ActionGroup group )
    {
        this.childActionGroups.add( group );
    }
    
    public final void execute( final Shell shell )
    {
        Object result = null;
        
        try
        {
            result = run( shell );
        }
        catch( Exception e )
        {
            // The EditFailedException happen here only as the result of the user explicitly deciding
            // not not go forward with an action. They serve the purpose of an abort signal so we
            // don't log them. Everything else gets logged.
            
            final EditFailedException editFailedException = EditFailedException.findAsCause( e );
            
            if( editFailedException == null )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
        
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handleActionExecuted( this, result );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }

    protected abstract Object run( Shell shell );
    
    public void dispose()
    {
        if( this.image != null && this.imageDescriptor != null )
        {
            this.image.dispose();
            this.image = null;
        }
        
        for( ActionGroup group : this.childActionGroups )
        {
            group.dispose();
        }
    }
}
