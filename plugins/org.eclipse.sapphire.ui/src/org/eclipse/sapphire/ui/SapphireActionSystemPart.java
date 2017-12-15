/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Greg Amerson - [342777] ImageProvider needs to support a listener mechanism
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil.toImageDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.ui.def.ISapphireActionImage;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHint;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHintAfter;
import org.eclipse.sapphire.ui.def.ISapphireActionLocationHintBefore;
import org.eclipse.sapphire.ui.def.ISapphireActionSystemPartDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireActionSystemPart
{
    private FunctionContext functionContext;
    private String id;
    private FunctionResult labelFunctionResult;
    private final List<ImageDescriptor> images = new CopyOnWriteArrayList<ImageDescriptor>();
    private final List<SapphireActionLocationHint> locationHints = new CopyOnWriteArrayList<SapphireActionLocationHint>();
    private final List<SapphireActionLocationHint> locationHintsReadOnly = Collections.unmodifiableList( this.locationHints );
    private boolean enabled;
    private boolean checked;
    private final ListenerContext listeners = new ListenerContext();
    
    protected final void init( final ISapphireActionSystemPartDef def )
    {
        this.functionContext = initFunctionContext();
        
        if( def != null )
        {
            this.id = def.getId().getContent();
            
            final Function labelFunction = FailSafeFunction.create( def.getLabel().getContent(), Literal.create( String.class ) );
            
            this.labelFunctionResult = labelFunction.evaluate( this.functionContext );
            
            this.labelFunctionResult.attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        broadcast( new LabelChangedEvent() );
                    }
                }
            );
            
            for( ISapphireActionImage image : def.getImages() )
            {
                final Function imageFunction = FailSafeFunction.create( image.getImage().getContent(), Literal.create( ImageData.class ) );
                final FunctionResult imageFunctionResult = imageFunction.evaluate( this.functionContext ); 
                final ImageData data = (ImageData) imageFunctionResult.value();
                
                if( data != null )
                {
                    final ImageDescriptor img = toImageDescriptor( data );
                    
                    if( img != null )
                    {
                        this.images.add( img ); 
                    }
                }
                
                imageFunctionResult.dispose();
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
    
    protected abstract FunctionContext initFunctionContext();
    
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
        
        broadcast( new IdChangedEvent() );
    }

    public final String getLabel()
    {
        synchronized( this )
        {
            return ( this.labelFunctionResult == null ? null : (String) this.labelFunctionResult.value() );
        }
    }
    
    public final void setLabel( final String label )
    {
        synchronized( this )
        {
            this.labelFunctionResult = Literal.create( label ).evaluate( this.functionContext );
        }
        
        broadcast( new LabelChangedEvent() );
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
        broadcast( new ImagesChangedEvent() );
    }
    
    public final void removeImage( final ImageDescriptor image )
    {
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.images.remove( image );
        broadcast( new ImagesChangedEvent() );
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
        broadcast( new LocationHintsChangedEvent() );
    }

    public final void removeLocationHint( final String locationHint )
    {
        if( locationHint == null )
        {
            throw new IllegalArgumentException();
        }

        this.locationHints.remove( locationHint );
        broadcast( new LocationHintsChangedEvent() );
    }
    
    public boolean isEnabled()
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
            broadcast( new EnablementChangedEvent() );
        }
    }
    
    public boolean isChecked()
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
            broadcast( new CheckedStateChangedEvent() );
        }
    }
    
    public final void attach( final Listener listener )
    {
        this.listeners.attach( listener );
    }
    
    public final void detach( final Listener listener )
    {
        this.listeners.detach( listener );
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }
    
    public final void dispose()
    {
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        broadcast( new DisposeEvent() );
    }
    
    public static final class IdChangedEvent extends Event {}
    public static final class LabelChangedEvent extends Event {}
    public static final class ImagesChangedEvent extends Event {}
    public static final class LocationHintsChangedEvent extends Event {}
    public static final class EnablementChangedEvent extends Event {}
    public static final class CheckedStateChangedEvent extends Event {}
    
}