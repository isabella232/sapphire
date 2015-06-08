/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [342777] ImageProvider needs to support a listener mechanism
 *    Gregory Amerson - [374622] Add ability to specify action tooltips
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.sizeOfImage;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.modeling.el.FailSafeFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.ui.def.ActionLocationHint;
import org.eclipse.sapphire.ui.def.ActionLocationHintAfter;
import org.eclipse.sapphire.ui.def.ActionLocationHintBefore;
import org.eclipse.sapphire.ui.def.ActionSystemPartDef;
import org.eclipse.sapphire.ui.def.ImageReference;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class SapphireActionSystemPart implements Disposable
{
    private FunctionContext functionContext;
    private String id;
    private FunctionResult labelFunctionResult;
    private FunctionResult toolTipFunctionResult;
    private String description;
    private final List<ImageData> images = new CopyOnWriteArrayList<ImageData>();
    private final List<SapphireActionLocationHint> locationHints = new CopyOnWriteArrayList<SapphireActionLocationHint>();
    private final List<SapphireActionLocationHint> locationHintsReadOnly = Collections.unmodifiableList( this.locationHints );
    private boolean visible;
    private boolean enabled;
    private boolean checked;
    private final ListenerContext listeners = new ListenerContext();
    
    protected final void init( final ActionSystemPartDef def )
    {
        this.functionContext = initFunctionContext();
        
        if( def != null )
        {
            this.id = def.getId().content();
            
            final Function labelFunction = FailSafeFunction.create( def.getLabel().content(), Literal.create( String.class ) );
            
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
            
            final Function toolTipFunction = FailSafeFunction.create( def.getToolTip().content(), Literal.create( String.class ) );

            this.toolTipFunctionResult = toolTipFunction.evaluate( this.functionContext );

            this.toolTipFunctionResult.attach
            ( 
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        broadcast( new ToolTipChangedEvent() );
                    }
                }
            );

            this.description = def.getDescription().content();
            
            for( ImageReference image : def.getImages() )
            {
                final Function imageFunction = FailSafeFunction.create( image.getImage().content(), Literal.create( ImageData.class ) );
                final FunctionResult imageFunctionResult = imageFunction.evaluate( this.functionContext ); 
                final ImageData data = (ImageData) imageFunctionResult.value();
                
                if( data != null )
                {
                    this.images.add( data );
                }
                
                imageFunctionResult.dispose();
            }
            
            for( ActionLocationHint locationHintDef : def.getLocationHints() )
            {
                final String locationHintText = locationHintDef.getReferenceEntityId().content();
                
                if( locationHintText != null )
                {
                    final SapphireActionLocationHint locationHint;
                    
                    if( locationHintDef instanceof ActionLocationHintBefore )
                    {
                        locationHint = new SapphireActionLocationHintBefore( locationHintText );
                    }
                    else if( locationHintDef instanceof ActionLocationHintAfter )
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
        this.visible = true;
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
    
    public final String getDescription()
    {
    	return this.description;
    }
    

    public final String getToolTip()
    {
        synchronized( this )
        {
            return( this.toolTipFunctionResult == null ? null : (String) this.toolTipFunctionResult.value() );
        }
    }

    public final void setToolTip( final String tooltip )
    {
        synchronized( this )
        {
            this.toolTipFunctionResult = Literal.create( tooltip ).evaluate( this.functionContext );
        }

        broadcast( new ToolTipChangedEvent() );
    }

    public final ImageData getImage( final int size )
    {
        for( ImageData image : this.images )
        {
            if( sizeOfImage( image ) == size )
            {
                return image;
            }
        }
        
        return null;
    }
    
    public final void addImage( final ImageData image )
    {
        if( image == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.images.add( image );
        broadcast( new ImagesChangedEvent() );
    }
    
    public final void removeImage( final ImageData image )
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

    public final boolean isVisible()
    {
        synchronized( this )
        {
            return this.visible;
        }
    }
    
    public final void setVisible( final boolean visible )
    {
        boolean changed = false;
        
        synchronized( this )
        {
            if( this.visible != visible )
            {
                this.visible = visible;
                changed = true;
            }
        }
        
        if( changed )
        {
            broadcast( new VisibilityEvent() );
        }
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
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }
    
    @Override
    public final void dispose()
    {
        if( this.labelFunctionResult != null )
        {
            this.labelFunctionResult.dispose();
        }
        
        if( this.toolTipFunctionResult != null )
        {
            this.toolTipFunctionResult.dispose();
        }
        
        broadcast( new DisposeEvent() );
    }
    
    public static final class IdChangedEvent extends Event {}
    public static final class LabelChangedEvent extends Event {}
    public static final class ToolTipChangedEvent extends Event {}
    public static final class ImagesChangedEvent extends Event {}
    public static final class LocationHintsChangedEvent extends Event {}
    public static final class VisibilityEvent extends Event {}
    public static final class EnablementChangedEvent extends Event {}
    public static final class CheckedStateChangedEvent extends Event {}
    
}