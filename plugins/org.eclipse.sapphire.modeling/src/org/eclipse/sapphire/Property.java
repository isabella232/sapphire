/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.ClearOnDisable;
import org.eclipse.sapphire.services.DependenciesService;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.services.internal.PropertyInstanceServiceContext;

/**
 * Represents an instance of a property within an element.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class Property implements Observable
{
    private static final int INITIALIZED = 1;
    private static final int ENABLEMENT_INITIALIZED = 1 << 1;
    private static final int VALIDATION_INITIALIZED = 1 << 2;
    protected static final int CONTENT_INITIALIZED = 1 << 3;
    
    @Text( "{0} property is already disposed." )
    private static LocalizableText propertyAlreadyDisposed;
    
    @Text( "Path \"{2}\" is invalid for {0}#{1}." )
    private static LocalizableText illegalPathException;
    
    static
    {
        LocalizableText.init( Property.class );
    }

    private final Element element;
    private final PropertyDef definition;
    private PropertyInstanceServiceContext services;
    private ListenerContext listeners;
    private boolean enablement;
    private Status validation;
    protected byte initialization;
    private boolean disposed = false;
    
    public Property( final Element element, final PropertyDef property )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.element = element;
        
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.definition = property;
    }
    
    protected final void init()
    {
        assertNotDisposed();
        
        if( ( this.initialization & INITIALIZED ) == 0 )
        {
            this.initialization |= INITIALIZED;
            
            for( Listener listener : definition().listeners() )
            {
                attach( listener );
            }
            
            final Listener triggerRefreshListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( ! disposed() )
                    {
                        refresh();
                    }
                }
            };
            
            final Set<ModelPath> dependencies = new HashSet<ModelPath>();
            
            for( DependenciesService ds : services( DependenciesService.class ) )
            {
                dependencies.addAll( ds.dependencies() );
            }
            
            if( ! dependencies.isEmpty() )
            {
                for( ModelPath dependency : dependencies )
                {
                    element().attach( triggerRefreshListener, dependency );
                }
                
                final Listener disposeListener = new FilteredListener<ElementDisposeEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final ElementDisposeEvent event )
                    {
                        for( ModelPath dependency : dependencies )
                        {
                            element().detach( triggerRefreshListener, dependency );
                        }
                    }
                };
                
                element().attach( disposeListener );
            }
        }
    }
    
    protected final void refreshEnablement( final boolean onlyIfNotInitialized )
    {
        boolean initialized;
        
        synchronized( this )
        {
            initialized = ( ( this.initialization & ENABLEMENT_INITIALIZED ) != 0 );
        }
        
        if( ! initialized || ! onlyIfNotInitialized )
        {
            boolean after = true;
            
            if( ! initialized )
            {
                final Listener listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refreshEnablement( false );
                    }
                };
                
                for( EnablementService service : services( EnablementService.class ) )
                {
                    service.attach( listener );
                }
                
                if( definition().hasAnnotation( ClearOnDisable.class ) )
                {
                    final Listener clearOnDisableListener = new FilteredListener<PropertyEnablementEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final PropertyEnablementEvent event )
                        {
                            if( event.before() == true && event.after() == false )
                            {
                                clear();
                            }
                        }
                    };
                    
                    attach( clearOnDisableListener );
                }
            }
            
            for( EnablementService service : services( EnablementService.class ) )
            {
                after = ( after && service.enablement() );
                
                if( after == false )
                {
                    break;
                }
            }
            
            PropertyEnablementEvent event = null; 
            
            synchronized( this )
            {
                initialized = ( ( this.initialization & ENABLEMENT_INITIALIZED ) != 0 );
                
                if( initialized )
                {
                    final boolean before = this.enablement;
                    
                    if( before != after )
                    {
                        this.enablement = after;
                        event = new PropertyEnablementEvent( this, before, after );
                    }
                }
                else
                {
                    this.enablement = after;
                    this.initialization |= ENABLEMENT_INITIALIZED;
                }
            }
            
            broadcast( event );
        }
    }
    
    protected final void refreshValidation( final boolean onlyIfNotInitialized )
    {
        boolean initialized;
        
        synchronized( this )
        {
            initialized = ( ( this.initialization & VALIDATION_INITIALIZED ) != 0 );
        }
        
        if( ! initialized || ! onlyIfNotInitialized )
        {
            final Status.CompositeStatusFactory freshValidationResultFactory = Status.factoryForComposite();
            
            if( ! initialized )
            {
                final Listener listener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        refreshValidation( false );
                    }
                };
                
                for( final ValidationService service : services( ValidationService.class ) )
                {
                    service.attach( listener );
                }
            }
            
            for( final ValidationService service : services( ValidationService.class ) )
            {
                freshValidationResultFactory.merge( service.validation() );
            }
            
            final Status freshValidationResult = freshValidationResultFactory.create();
            
            PropertyValidationEvent event = null; 
            
            synchronized( this )
            {
                initialized = ( ( this.initialization & VALIDATION_INITIALIZED ) != 0 );
                
                if( initialized )
                {
                    final Status staleValidationResult = this.validation;
                    
                    if( ! staleValidationResult.equals( freshValidationResult ) )
                    {
                        this.validation = freshValidationResult;
                        event = new PropertyValidationEvent( this, staleValidationResult, freshValidationResult );
                    }
                }
                else
                {
                    this.validation = freshValidationResult;
                    this.initialization |= VALIDATION_INITIALIZED;
                }
            }
            
            broadcast( event );
        }
    }
    
    /**
     * Returns the root of the model.
     * 
     * @return the root of the model
     */
    
    public final Element root()
    {
        return this.element.root();
    }
    
    /**
     * Return the element instance.
     * 
     * @return the element instance
     */
    
    public final Element element()
    {
        return this.element;
    }
    
    /**
     * Returns the property definition.
     * 
     * @return the property definition
     */
    
    public PropertyDef definition()
    {
        return this.definition;
    }
    
    /**
     * Returns the property name.
     * 
     * @return the property name
     */
    
    public final String name()
    {
        return this.definition.name();
    }
    
    public final <T> T nearest( final Class<T> type )
    {
        if( type.isAssignableFrom( getClass() ) )
        {
            return type.cast( this );
        }
        else
        {
            return element().nearest( type );
        }
    }
    
    protected PropertyBinding binding()
    {
        return element().resource().binding( this );
    }
    
    /**
     * Clears this property. 
     */
    
    public abstract void clear();
    
    /**
     * Copies property content from the provided source element. The source element does not have to
     * be of the same type as target. The copy will happen if the source element has a property with
     * the same name and type as this property. Otherwise, no change will be performed.
     * 
     * @param source the element to copy from
     * @throws IllegalArgumentException if source is null
     * @throws UnsupportedOperationException if this property is read-only
     * @throws IllegalStateException if this property or the source element is already disposed
     */
    
    public abstract void copy( final Element source );
    
    /**
     * Determines if this property is empty. The empty state is defined as follows:
     * 
     * <ul>
     *   <li><b>Value Property</b> - has null value or has default value</li>
     *   <li><b>Element Property</b> - element does not exist</li>
     *   <li><b>Implied Element Property</b> - none of the child element's properties are non-empty</li>
     *   <li><b>List Property</b> - list size is zero</li>
     *   <li><b>Transient Property</b> - has null content</li>
     * </ul>
     * 
     * @return true if this property is empty, false otherwise
     * @throws IllegalStateException if this property is already disposed
     */
    
    public abstract boolean empty();

    /**
     * Determines whether this property is enabled
     * 
     * @return true if this property is enabled and false otherwise
     * @throws IllegalStateException if this property is already disposed
     */
    
    public final boolean enabled()
    {
        init();
        refreshEnablement( true );
        
        synchronized( this )
        {
            return this.enablement;
        }
    }
    
    /**
     * Returns the validation result for this property.
     * 
     * @return the validation result for this property
     * @throws IllegalStateException if this property is already disposed
     */
    
    public final Status validation()
    {
        init();
        refreshValidation( true );
        
        synchronized( this )
        {
            return this.validation;
        }
    }
    
    public abstract void refresh();
    
    /**
     * Returns the service of the specified type from the property instance service context.
     * 
     * <p>Service Context: <b>Sapphire.Property.Instance</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    public final <S extends Service> S service( final Class<S> type )
    {
        assertNotDisposed();

        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        final List<S> services = services( type );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    /**
     * Returns the service of the specified type from the property instance service context.
     * 
     * <p>Service Context: <b>Sapphire.Property.Instance</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    public final <S extends Service> List<S> services( final Class<S> type )
    {
        assertNotDisposed();

        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( this.services == null )
            {
                this.services = new PropertyInstanceServiceContext( this, ( (ElementImpl) element() ).listeners() );
            }
            
            return this.services.services( type );
        }
    }
    
    /**
     * Attaches a listener to this property.
     * 
     * @param listener the listener
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalStateException if this property is disposed
     */
    
    public final void attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
            
            if( this.listeners == null )
            {
                this.listeners = new ListenerContext();
                this.listeners.coordinate( ( (ElementImpl) this.element ).listeners() );
            }
            
            this.listeners.attach( listener );
        }
    }
    
    /**
     * Attaches a listener to this property.
     * 
     * @param listener the listener
     * @param path 
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalArgumentException if the path is null or invalid
     * @throws IllegalStateException if this property is disposed
     */
    
    public final void attach( final Listener listener, final String path )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
    
            attach( listener, new ModelPath( path ) );
        }
    }
    
    /**
     * Attaches a listener to this property.
     * 
     * @param listener the listener
     * @param path 
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalArgumentException if the path is null or invalid
     * @throws IllegalStateException if this property is disposed
     */
    
    public void attach( final Listener listener, final ModelPath path )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
    
            if( path.length() == 0 )
            {
                attach( listener );
            }
            else
            {
                final ModelPath.Segment head = path.head();
                
                if( head instanceof AllDescendentsSegment )
                {
                    attach( listener );
                }
                else if( head instanceof ModelRootSegment )
                {
                    root().attach( listener, path.tail() );
                }
                else if( head instanceof ParentElementSegment )
                {
                    final Property parent = element().parent();
                    
                    if( parent == null )
                    {
                        throw createIllegalPathException( path );
                    }
                    
                    parent.element().attach( listener, path.tail() );
                }
                else
                {
                    throw createIllegalPathException( path );
                }
            }
        }
    }
    
    /**
     * Detaches a listener from this property.
     * 
     * @param listener the listener
     * @throws IllegalArgumentException if the listener is null
     */
    
    public final void detach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( this.listeners != null )
            {
                this.listeners.detach( listener );
            }
        }
    }
    
    /**
     * Detaches a listener from this property.
     * 
     * @param listener the listener
     * @param path 
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalArgumentException if the path is null or invalid
     */
    
    public final void detach( final Listener listener, final String path )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            detach( listener, new ModelPath( path ) );
        }
    }
    
    /**
     * Detaches a listener from this property.
     * 
     * @param listener the listener
     * @param path 
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalArgumentException if the path is null or invalid
     */
    
    public void detach( final Listener listener, final ModelPath path )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( path.length() == 0 )
            {
                detach( listener );
            }
            else
            {
                final ModelPath.Segment head = path.head();
                
                if( head instanceof AllDescendentsSegment )
                {
                    detach( listener );
                }
                else if( head instanceof ModelRootSegment )
                {
                    root().detach( listener, path.tail() );
                }
                else if( head instanceof ParentElementSegment )
                {
                    final Property parent = element().parent();
                    
                    if( parent == null )
                    {
                        throw createIllegalPathException( path );
                    }
                    
                    parent.element().detach( listener, path.tail() );
                }
                else
                {
                    throw createIllegalPathException( path );
                }
            }
        }
    }
    
    protected final void broadcast( final Event event )
    {
        if( event != null )
        {
            synchronized( root() )
            {
                if( this.listeners != null )
                {
                    this.listeners.broadcast( event );
                }
            }
        }
    }
    
    public final boolean disposed()
    {
        synchronized( root() )
        {
            return this.disposed;
        }
    }
    
    /**
     * Only to be called by the framework.
     */
    
    // TODO: Hide
    
    public final void dispose()
    {
        synchronized( root() )
        {
            if( ! this.disposed )
            {
                this.disposed = true;
                
                if( this.services != null )
                {
                    this.services.dispose();
                    this.services = null;
                }
                
                disposeOther();
                
                this.listeners = null;
                this.validation = null;
            }
        }
    }
    
    protected void disposeOther()
    {
        // To be overridden.
    }
    
    protected final void assertNotDisposed()
    {
        if( disposed() )
        {
            final String msg = propertyAlreadyDisposed.format( this.definition.name() );
            throw new IllegalStateException( msg );
        }
    }
    
    protected final IllegalArgumentException createIllegalPathException( final ModelPath path )
    {
        final String message = illegalPathException.format
        (
            element().type().getModelElementClass().getName(),
            name(),
            path.toString()
        );
        
        return new IllegalArgumentException( message );
    }
    
}
