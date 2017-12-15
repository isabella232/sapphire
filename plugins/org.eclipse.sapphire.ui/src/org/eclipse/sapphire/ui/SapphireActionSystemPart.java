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

package org.eclipse.sapphire.ui;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.ui.def.ISapphireActionImage;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHint;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHintAfter;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHintBefore;
import org.eclipse.sapphire.ui.def.ISapphireActionSystemPartDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireActionSystemPart
{
    public static final String EVENT_ID_CHANGED = "id";
    public static final String EVENT_LABEL_CHANGED = "label";
    public static final String EVENT_IMAGES_CHANGED = "image";
    public static final String EVENT_LOCATION_HINTS_CHANGED = "location-hints";
    public static final String EVENT_ENABLEMENT_STATE_CHANGED = "enablement";
    public static final String EVENT_CHECKED_STATE_CHANGED = "checked";
    
    private String id;
    private String label;
    private final List<ImageDescriptor> images = new CopyOnWriteArrayList<ImageDescriptor>();
    private final List<SapphireActionLocationHint> locationHints = new CopyOnWriteArrayList<SapphireActionLocationHint>();
    private final List<SapphireActionLocationHint> locationHintsReadOnly = Collections.unmodifiableList( this.locationHints );
    private boolean enabled;
    private boolean checked;
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    
    protected final void init( final ISapphireActionSystemPartDef def )
    {
        if( def != null )
        {
            this.id = def.getId().getContent();
            this.label = def.getLabel().getLocalizedText();
            
            for( ISapphireActionImage image : def.getImages() )
            {
                final ImageDescriptor img = image.getImage().resolve();
                
                if( img != null )
                {
                    this.images.add( img );
                }
            }
            
            for( ISapphireActionLocationHint locationHintDef : def.getLocationHints() )
            {
                final String locationHintText = locationHintDef.getReferenceEntityId().getContent();
                
                if( locationHintText != null )
                {
                    final SapphireActionLocationHint locationHint;
                    
                    if( locationHintDef instanceof ISapphireActionLocationHintBefore )
                    {
                        locationHint = new SapphireActionLocationHintBefore( locationHintText );
                    }
                    else if( locationHintDef instanceof ISapphireActionLocationHintAfter )
                    {
                        locationHint = new SapphireActionLocationHintAfter( locationHintText );
                    }
                    else
                    {
                        throw new IllegalStateException();
                    }
                    
                    this.locationHints.add( locationHint );
                }
            }
        }
        
        this.enabled = true;
    }
    
    public final String getId()
    {
        synchronized( this )
        {
            return this.id;
        }
    }
    
    public final void setId( final String id )
    {
        synchronized( this )
        {
            this.id = id;
        }
        
        notifyListeners( new Event( EVENT_ID_CHANGED ) );
    }

    public final String getLabel()
    {
        synchronized( this )
        {
            return this.label;
        }
    }
    
    public final void setLabel( final String label )
    {
        synchronized( this )
        {
            this.label = label;
        }
        
        notifyListeners( new Event( EVENT_LABEL_CHANGED ) );
    }
    
    public final ImageDescriptor getImage( final int size )
    {
        for( ImageDescriptor image : this.images )
        {
            if( image.getImageData().height == size )
            {
                return image;
            }
        }
        
        return null;
    }
    
    public final void addImage( final ImageDescriptor image )
    {
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.images.add( image );
        notifyListeners( new Event( EVENT_IMAGES_CHANGED ) );
    }
    
    public final void removeImage( final ImageDescriptor image )
    {
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.images.remove( image );
        notifyListeners( new Event( EVENT_IMAGES_CHANGED ) );
    }
    
    public final List<SapphireActionLocationHint> getLocationHints()
    {
        return this.locationHintsReadOnly;
    }
    
    public final void addLocationHint( final SapphireActionLocationHint locationHint )
    {
        if( locationHint == null )
        {
            throw new IllegalArgumentException();
        }

        this.locationHints.add( locationHint );
        notifyListeners( new Event( EVENT_LOCATION_HINTS_CHANGED ) );
    }

    public final void removeLocationHint( final String locationHint )
    {
        if( locationHint == null )
        {
            throw new IllegalArgumentException();
        }

        this.locationHints.remove( locationHint );
        notifyListeners( new Event( EVENT_LOCATION_HINTS_CHANGED ) );
    }
    
    public final boolean isEnabled()
    {
        synchronized( this )
        {
            return this.enabled;
        }
    }
    
    public final void setEnabled( final boolean enabled )
    {
        boolean changed = false;
        
        synchronized( this )
        {
            if( this.enabled != enabled )
            {
                this.enabled = enabled;
                changed = true;
            }
        }
        
        if( changed )
        {
            notifyListeners( new Event( EVENT_ENABLEMENT_STATE_CHANGED ) );
        }
    }
    
    public final boolean isChecked()
    {
        synchronized( this )
        {
            return this.checked;
        }
    }
    
    public final void setChecked( final boolean checked )
    {
        boolean changed = false;
        
        synchronized( this )
        {
            if( this.checked != checked )
            {
                this.checked = checked;
                changed = true;
            }
        }
        
        if( changed )
        {
            notifyListeners( new Event( EVENT_CHECKED_STATE_CHANGED ) );
        }
    }
    
    public final void addListener( final Listener listener )
    {
        this.listeners.add( listener );
    }
    
    public final void removeListener( final Listener listener )
    {
        this.listeners.remove( listener );
    }
    
    protected final void notifyListeners( final Event event )
    {
        for( Listener listener : this.listeners )
        {
            try
            {
                listener.handleEvent( event );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }
    
    public static abstract class Listener
    {
        public abstract void handleEvent( final Event event );
    }
    
    public static class Event
    {
        private final String type;
        
        public Event( final String type )
        {
            this.type = type;
        }
        
        public String getType()
        {
            return this.type;
        }
    }
    
}