/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [372359] Provide means to extend the behavior of adapt methods
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.AllSiblingsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.modeling.annotations.ClearOnDisable;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.AdapterService;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DependenciesAggregationService;
import org.eclipse.sapphire.services.DerivedValueService;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.EqualityService;
import org.eclipse.sapphire.services.InitialValueService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationAggregationService;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.services.internal.ElementInstanceServiceContext;
import org.eclipse.sapphire.services.internal.PropertyInstanceServiceContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class ModelElement extends ModelParticle implements IModelElement
{
    private final ModelElementType type;
    private final ModelProperty parentProperty;
    private Status validation;
    private final ListenerContext listeners = new ListenerContext();
    private final Map<ModelProperty,Boolean> enablementStatuses;
    private boolean enablementServicesInitialized;
    private ElementInstanceServiceContext elementServiceContext;
    private final Map<ModelProperty,PropertyInstanceServiceContext> propertyServiceContexts;
    private boolean disposed = false;
    
    public ModelElement( final ModelElementType type,
                         final IModelParticle parent,
                         final ModelProperty parentProperty,
                         final Resource resource )
    {
        super( parent, resource );
        
        this.type = type;
        this.parentProperty = parentProperty;
        this.validation = null;
        this.enablementStatuses = new HashMap<ModelProperty,Boolean>();
        this.propertyServiceContexts = new HashMap<ModelProperty,PropertyInstanceServiceContext>();
        
        if( parent != null )
        {
            final ModelElement p = (ModelElement) parent.nearest( IModelElement.class );
            this.listeners.coordinate( p.listeners );
        }
        
        resource.init( this );
        
        attach( new GlobalBridgeListener() );
        attach( new PropertyInitializationListener() );
    }
    
    public ModelElementType type()
    {
        return this.type;
    }
    
    public ModelProperty getParentProperty()
    {
        return this.parentProperty;
    }
    
    public final void initialize()
    {
        for( ModelProperty property : properties() ) 
        {
            if( property instanceof ValueProperty ) 
            {
                final InitialValueService initialValueService = service( property, InitialValueService.class );
                
                if( initialValueService != null ) 
                {
                    write( (ValueProperty) property, initialValueService.value() );
                }
            }
            else if( property instanceof ImpliedElementProperty )
            {
                read( ( (ImpliedElementProperty) property ) ).initialize();
            }
        }
    }
    
    public List<ModelProperty> properties()
    {
        return this.type.properties();
    }

    public <T extends ModelProperty> T property( final String name )
    {
        return this.type.property( name );
    }

    public Object read( final ModelProperty property )
    {
        final String msg = NLS.bind( Resources.cannotReadProperty, property.getName() );
        throw new IllegalArgumentException( msg );
    }
    
    @SuppressWarnings( "unchecked" )
    
    public final <T> Value<T> read( final ValueProperty property )
    {
        return (Value<T>) read( (ModelProperty) property );
    }
    
    @SuppressWarnings( "unchecked" )
    
    public final <T extends IModelElement> ModelElementHandle<T> read( final ElementProperty property )
    {
        return (ModelElementHandle<T>) read( (ModelProperty) property );
    }

    @SuppressWarnings( "unchecked" )
    
    public final <T extends IModelElement> T read( final ImpliedElementProperty property )
    {
        return (T) read( (ModelProperty) property );
    }

    @SuppressWarnings( "unchecked" )
    
    public final <T extends IModelElement> ModelElementList<T> read( final ListProperty property )
    {
        return (ModelElementList<T>) read( (ModelProperty) property );
    }

    @SuppressWarnings( "unchecked" )
    
    public final <T> Transient<T> read( final TransientProperty property )
    {
        return (Transient<T>) read( (ModelProperty) property );
    }
    
    public final SortedSet<String> read( final ModelPath path )
    {
        final SortedSet<String> result = new TreeSet<String>();
        read( path, result );
        return result;
    }

    public final void read( final ModelPath path,
                            final Collection<String> result )
    {
        synchronized( root() )
        {
            final ModelPath.Segment head = path.head();
            
            if( head instanceof ModelRootSegment )
            {
                ( (IModelElement) root() ).read( path.tail(), result );
            }
            else if( head instanceof ParentElementSegment )
            {
                IModelParticle parent = parent();
                
                if( parent == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                else
                {
                    if( parent instanceof ModelElementList<?> )
                    {
                        parent = parent.parent();
                    }
                }
                
                ( (IModelElement) parent ).read( path.tail(), result );
            }
            else if( head instanceof AllSiblingsSegment )
            {
                IModelParticle parent = parent();
                
                if( parent == null || ! ( parent instanceof ModelElementList<?> ) )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                
                parent = parent.parent();
                
                final ModelPath p = ( new ModelPath( getParentProperty().getName() ) ).append( path.tail() );
                ( (IModelElement) parent ).read( p, result );
            }
            else if( head instanceof AllDescendentsSegment )
            {
                for( ModelProperty property : properties() )
                {
                    final Object obj = read( property );
                    
                    if( obj instanceof Value<?> )
                    {
                        final String val = ( (Value<?>) obj ).getText();
                        
                        if( val != null )
                        {
                            result.add( val );
                        }
                    }
                    else if( obj instanceof IModelElement )
                    {
                        ( (IModelElement) obj ).read( path, result );
                    }
                    else if( obj instanceof ModelElementList<?> )
                    {
                        for( IModelElement entry : (ModelElementList<?>) obj )
                        {
                            entry.read( path, result );
                        }
                    }
                }
            }
            else if( head instanceof TypeFilterSegment )
            {
                final String t = type().getSimpleName();
                boolean match = false;
                
                for( String type : ( (TypeFilterSegment) head ).getTypes() )
                {
                    if( type.equalsIgnoreCase( t ) )
                    {
                        match = true;
                        break;
                    }
                }
                
                if( match )
                {
                    read( path.tail(), result );
                }
            }
            else
            {
                final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
                final ModelProperty property = property( propertyName );

                if( property == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                
                final Object obj = read( property );
                
                if( obj instanceof Value<?> )
                {
                    final String val = ( (Value<?>) obj ).getText();
                    
                    if( val != null )
                    {
                        result.add( val );
                    }
                    
                    if( path.length() != 1 )
                    {
                        logInvalidModelPathMessage( path );
                        return;
                    }
                }
                else if( obj instanceof IModelElement )
                {
                    ( (IModelElement) obj ).read( path.tail(), result );
                }
                else if( obj instanceof ModelElementList<?> )
                {
                    for( IModelElement entry : (ModelElementList<?>) obj )
                    {
                        entry.read( path.tail(), result );
                    }
                }
            }
        }
    }
    
    public void write( final ValueProperty property,
                       final Object value )
    {
        final String msg = NLS.bind( Resources.cannotWriteProperty, property.getName() );
        throw new IllegalArgumentException( msg );
    }
    
    public void write( final TransientProperty property,
                       final Object value )
    {
        final String msg = NLS.bind( Resources.cannotWriteProperty, property.getName() );
        throw new IllegalArgumentException( msg );
    }
    
    public final void refresh()
    {
        refresh( false, false );
    }
    
    public final void refresh( final boolean force )
    {
        refresh( force, false );
    }
    
    public final void refresh( final boolean force,
                               final boolean deep )
    {
        synchronized( root() )
        {
            assertNotDisposed();
            
            for( ModelProperty property : properties() )
            {
                // The second disposed check is to catch the case where refreshing one property
                // triggers a listener that causes this element to be disposed.
                
                if( disposed() )
                {
                    break;
                }
                
                refresh( property, force, deep );
            }
        }
    }
    
    public final void refresh( final ModelProperty property )
    {
        refresh( property, false, false );
    }
    
    public final void refresh( final ModelProperty property,
                               final boolean force )
    {
        refresh( property, force, false );
    }
    
    public final void refresh( final ModelProperty property,
                               final boolean force,
                               final boolean deep )
    {
        assertNotDisposed();
        
        refreshProperty( property, force );
        
        if( deep )
        {
            if( property instanceof ElementProperty )
            {
                final IModelElement child;
                
                if( property instanceof ImpliedElementProperty )
                {
                    child = read( (ImpliedElementProperty) property );
                }
                else
                {
                    child = read( (ElementProperty) property ).element();
                }
                
                if( child != null )
                {
                    child.refresh( force, true );
                }
            }
            else if( property instanceof ListProperty )
            {
                for( IModelElement child : read( (ListProperty) property ) )
                {
                    child.refresh( force, true );
                }
            }
        }
    }

    protected void refreshProperty( final ModelProperty property,
                                    final boolean force )
    {
        // The default implementation does not do anything.
    }
    
    public final void copy( final IModelElement element )
    {
        if( this.type != element.type() )
        {
            throw new IllegalArgumentException();
        }
        
        for( ModelProperty property : this.type.properties() )
        {
            if( ! property.isReadOnly() )
            {
                if( property instanceof ValueProperty )
                {
                    final ValueProperty prop = (ValueProperty) property;
                    write( prop, element.read( prop ).getText( false ) );
                }
                else if( property instanceof ImpliedElementProperty )
                {
                    final ImpliedElementProperty prop = (ImpliedElementProperty) property;
                    read( prop ).copy( element.read( prop ) );
                }
                else if( property instanceof ElementProperty )
                {
                    final ElementProperty prop = (ElementProperty) property;
                    final IModelElement elementChild = element.read( prop ).element();
                    final ModelElementHandle<?> handle = read( prop );
                    
                    if( elementChild == null )
                    {
                        handle.remove();
                    }
                    else
                    {
                        final IModelElement thisChild = handle.element( true, elementChild.type() );
                        thisChild.copy( elementChild );
                    }
                }
                else if( property instanceof ListProperty )
                {
                    final ListProperty prop = (ListProperty) property;
                    final ModelElementList<?> list = read( prop );
                    
                    list.clear();
                    
                    for( final IModelElement elementChild : element.read( prop ) )
                    {
                        final IModelElement thisChild = list.insert( elementChild.type() );
                        thisChild.copy( elementChild );
                    }
                }
                else if( property instanceof TransientProperty )
                {
                    final TransientProperty prop = (TransientProperty) property;
                    write( prop, element.read( prop ).content() );
                }
            }
        }
    }
    
    @Override
    public final boolean equals( final Object obj )
    {
        synchronized( root() )
        {
            boolean result = false;
            
            if( this == obj )
            {
                result = true;
            }
            else if( obj instanceof IModelElement && ! disposed() )
            {
                final EqualityService equalityService = service( EqualityService.class );
                
                if( equalityService != null )
                {
                    result = equalityService.doEquals( obj );
                }
            }
            
            return result;
        }
    }

    @Override
    public final int hashCode()
    {
        synchronized( root() )
        {
            int result;
         
            if( disposed() )
            {
                result = super.hashCode();
            }
            else
            {
                final EqualityService equalityService = service( EqualityService.class );
                
                if( equalityService != null )
                {
                    result = equalityService.doHashCode();
                }
                else
                {
                    result = super.hashCode();
                }
            }
            
            return result;
        }
    }

    public final <S extends Service> S service( final Class<S> serviceType )
    {
        final List<S> services = services( serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    public final <S extends Service> List<S> services( final Class<S> serviceType )
    {
        synchronized( root() )
        {
            if( this.elementServiceContext == null )
            {
                this.elementServiceContext = new ElementInstanceServiceContext( this );
            }
        }
        
        return this.elementServiceContext.services( serviceType );
    }

    public final <S extends Service> S service( final ModelProperty property,
                                                final Class<S> serviceType )
    {
        final List<S> services = services( property, serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }
    
    public final <S extends Service> List<S> services( final ModelProperty property,
                                                       final Class<S> serviceType )
    {
        PropertyInstanceServiceContext context;
        
        synchronized( root() )
        {
            context = this.propertyServiceContexts.get( property );
            
            if( context == null )
            {
                context = new PropertyInstanceServiceContext( this, property );
                this.propertyServiceContexts.put( property, context );
            }
        }
        
        return context.services( serviceType );
    }

    public final boolean enabled( final ModelProperty property )
    {
        synchronized( root() )
        {
            final ModelProperty finalProperty = property.refine( this );
            
            Boolean status = this.enablementStatuses.get( finalProperty );
            
            if( status == null )
            {
                refreshProperty( finalProperty, true );
                status = this.enablementStatuses.get( finalProperty );
            }
            
            return status;
        }
    }
    
    protected final EnablementRefreshResult refreshPropertyEnablement( final ModelProperty property )
    {
        return refreshPropertyEnablement( property, false );
    }
    
    private final EnablementRefreshResult refreshPropertyEnablement( final ModelProperty property,
                                                                     final boolean notifyListenersIfNecessary )
    {
        synchronized( root() )
        {
            if( ! this.enablementServicesInitialized )
            {
                for( final ModelProperty prop : this.type.properties() )
                {
                    final Listener enablementServiceListener = new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            if( ! disposed() )
                            {
                                refreshPropertyEnablement( prop, true );
                            }
                        }
                    };
                    
                    for( EnablementService service : services( prop, EnablementService.class ) )
                    {
                        service.attach( enablementServiceListener );
                    }
                }
                
                this.enablementServicesInitialized = true;
            }
            
            boolean newState = true;
            
            for( EnablementService service : services( property, EnablementService.class ) )
            {
                newState = ( newState && service.enablement() );
                
                if( newState == false )
                {
                    break;
                }
            }
            
            final Boolean oldState = this.enablementStatuses.get( property );
            
            if( oldState == null )
            {
                this.enablementStatuses.put( property, newState );
            }
            else if( ! oldState.equals( newState ) )
            {
                this.enablementStatuses.put( property, newState );
                
                if( notifyListenersIfNecessary )
                {
                    broadcast( new PropertyEnablementEvent( this, property, oldState, newState ) );
                }
            }
            
            return new EnablementRefreshResult( oldState, newState );
        }
    }

    public final Status validation()
    {
        if( this.validation == null )
        {
            refreshValidationResult();
        }
        
        return this.validation;
    }
    
    public final Status validation( final ModelProperty property )
    {
        final Status validation;
        
        if( property instanceof ValueProperty )
        {
            validation = read( (ValueProperty) property ).validation();
        }
        else if( property instanceof ListProperty )
        {
            validation = read( (ListProperty) property ).validation();
        }
        else if( property instanceof ImpliedElementProperty )
        {
            validation = read( (ImpliedElementProperty) property ).validation();
        }
        else if( property instanceof ElementProperty )
        {
            validation = read( (ElementProperty) property ).validation();
        }
        else if( property instanceof TransientProperty )
        {
            validation = read( (TransientProperty) property ).validation();
        }
        else
        {
            throw new IllegalStateException();
        }
        
        return validation;
    }
    
    private void refreshValidationResult()
    {
        final ValidationAggregationService service = service( ValidationAggregationService.class );
        final Status validation = service.validation();
        
        if( this.validation == null )
        {
            this.validation = validation;
            
            final Listener validationAggregationServiceListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refreshValidationResult();
                }
            };
            
            service.attach( validationAggregationServiceListener );
        }
        else if( ! this.validation.equals( validation ) )
        {
            final Status oldValidationState = this.validation;
            this.validation = validation;
            
            broadcast( new ElementValidationEvent( this, oldValidationState, this.validation ) );
        }
    }
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final void attach( final Listener listener,
                              final String path )
    {
        attach( listener, new ModelPath( path ) );
    }
    
    public final void attach( final Listener listener,
                              final ModelPath path )
    {
        final ModelPath.Segment head = path.head();
        
        if( head instanceof ModelRootSegment )
        {
            ( (IModelElement) root() ).attach( listener, path.tail() );
        }
        else if( head instanceof ParentElementSegment )
        {
            IModelParticle parent = parent();
            
            if( parent == null )
            {
                logInvalidModelPathMessage( path );
                return;
            }
            else
            {
                if( parent instanceof ModelElementList<?> )
                {
                    parent = parent.parent();
                }
            }
            
            ( (IModelElement) parent ).attach( listener, path.tail() );
        }
        else if( head instanceof AllSiblingsSegment )
        {
            IModelParticle parent = parent();
            
            if( parent == null || ! ( parent instanceof ModelElementList<?> ) )
            {
                logInvalidModelPathMessage( path );
                return;
            }
            
            parent = parent.parent();
            
            final ModelPath p = ( new ModelPath( this.parentProperty.getName() ) ).append( path.tail() );
            ( (IModelElement) parent ).attach( listener, p );
        }
        else if( head instanceof AllDescendentsSegment )
        {
            if( attach( new PropagationListener( path, listener ) ) )
            {
                for( ModelProperty property : this.type.properties() )
                {
                    if( property instanceof ImpliedElementProperty )
                    {
                        read( (ImpliedElementProperty) property ).attach( listener, path );
                    }
                    else if( property instanceof ElementProperty )
                    {
                        final IModelElement element = read( (ElementProperty) property ).element();
                        
                        if( element != null )
                        {
                            element.attach( listener, path );
                        }
                    }
                    else if( property instanceof ListProperty )
                    {
                        final ModelElementList<?> list = read( (ListProperty) property );
                        
                        for( IModelElement x : list )
                        {
                            x.attach( listener, path );
                        }
                    }
                    else
                    {
                        attach( PropertyEvent.filter( listener ) );
                    }
                }
            }
        }
        else if( head instanceof TypeFilterSegment )
        {
            final String t = this.type.getSimpleName();
            boolean match = false;
            
            for( String type : ( (TypeFilterSegment) head ).getTypes() )
            {
                if( type.equalsIgnoreCase( t ) )
                {
                    match = true;
                    break;
                }
            }
            
            if( match )
            {
                attach( listener, path.tail() );
            }
        }
        else
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            final ModelProperty property = this.type.property( propertyName );
            
            if( property == null )
            {
                logInvalidModelPathMessage( path );
                return;
            }

            if( path.length() == 1 )
            {
                attach( PropertyEvent.filter( listener, property ) );
            }
            else
            {
                final ModelPath tail = path.tail();
                
                if( property instanceof ImpliedElementProperty )
                {
                    read( (ImpliedElementProperty) property ).attach( listener, tail );
                }
                else if( property instanceof ElementProperty )
                {
                    if( attach( PropertyEvent.filter( new PropagationListener( tail, listener ), property ) ) )
                    {
                        final IModelElement element = read( (ElementProperty) property ).element();
                        
                        if( element != null )
                        {
                            element.attach( listener, tail );
                        }
                    }
                }
                else if( property instanceof ListProperty )
                {
                    if( attach( PropertyEvent.filter( new PropagationListener( tail, listener ), property ) ) )
                    {
                        final ModelElementList<?> list = read( (ListProperty) property );
                        
                        for( IModelElement x : list )
                        {
                            x.attach( listener, tail );
                        }
                    }
                }
                else
                {
                    logInvalidModelPathMessage( path );
                }
            }
        }
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    public final void detach( final Listener listener,
                              final String path )
    {
        detach( listener, new ModelPath( path ) );
    }
    
    public final void detach( final Listener listener,
                              final ModelPath path )
    {
        final ModelPath.Segment head = path.head();
        
        if( head instanceof ModelRootSegment )
        {
            ( (IModelElement) root() ).detach( listener, path.tail() );
        }
        else if( head instanceof ParentElementSegment )
        {
            IModelParticle parent = parent();
            
            if( parent == null )
            {
                logInvalidModelPathMessage( path );
                return;
            }
            else
            {
                if( parent instanceof ModelElementList<?> )
                {
                    parent = parent.parent();
                }
            }
            
            ( (IModelElement) parent ).detach( listener, path.tail() );
        }
        else if( head instanceof AllSiblingsSegment )
        {
            IModelParticle parent = parent();
            
            if( parent == null || ! ( parent instanceof ModelElementList<?> ) )
            {
                logInvalidModelPathMessage( path );
                return;
            }
            
            parent = parent.parent();
            
            final ModelPath p = ( new ModelPath( this.parentProperty.getName() ) ).append( path.tail() );
            ( (IModelElement) parent ).detach( listener, p );
        }
        else if( head instanceof AllDescendentsSegment )
        {
            detach( new PropagationListener( path, listener ) );
            
            for( ModelProperty property : this.type.properties() )
            {
                if( property instanceof ImpliedElementProperty )
                {
                    read( (ImpliedElementProperty) property ).detach( listener, path );
                }
                else if( property instanceof ElementProperty )
                {
                    final IModelElement element = read( (ElementProperty) property ).element();
                    
                    if( element != null )
                    {
                        element.detach( listener, path );
                    }
                }
                else if( property instanceof ListProperty )
                {
                    final ModelElementList<?> list = read( (ListProperty) property );
                    
                    for( IModelElement x : list )
                    {
                        x.detach( listener, path );
                    }
                }
                else
                {
                    detach( PropertyEvent.filter( listener ) );
                }
            }
        }
        else if( head instanceof TypeFilterSegment )
        {
            final String t = this.type.getSimpleName();
            boolean match = false;
            
            for( String type : ( (TypeFilterSegment) head ).getTypes() )
            {
                if( type.equalsIgnoreCase( t ) )
                {
                    match = true;
                    break;
                }
            }
            
            if( match )
            {
                detach( listener, path.tail() );
            }
        }
        else
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            final ModelProperty property = this.type.property( propertyName );
            
            if( property == null )
            {
                logInvalidModelPathMessage( path );
                return;
            }
            
            if( path.length() == 1 )
            {
                detach( PropertyEvent.filter( listener, property ) );
            }
            else
            {
                final ModelPath tail = path.tail();
                
                if( property instanceof ImpliedElementProperty )
                {
                    read( (ImpliedElementProperty) property ).detach( listener, tail );
                }
                else if( property instanceof ElementProperty )
                {
                    detach( PropertyEvent.filter( new PropagationListener( tail, listener ), property ) );

                    final IModelElement element = read( (ElementProperty) property ).element();
                        
                    if( element != null )
                    {
                        element.detach( listener, tail );
                    }
                }
                else if( property instanceof ListProperty )
                {
                    detach( PropertyEvent.filter( new PropagationListener( tail, listener ), property ) );
                    
                    final ModelElementList<?> list = read( (ListProperty) property );
                    
                    for( IModelElement x : list )
                    {
                        x.detach( listener, tail );
                    }
                }
                else
                {
                    logInvalidModelPathMessage( path );
                }
            }
        }
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }
    
    final void broadcastPropertyContentEvent( final ModelProperty property )
    {
        broadcast( new PropertyContentEvent( this, property ) );
    }

    final void broadcastPropertyValidationEvent( final ModelProperty property,
                                                 final Status before,
                                                 final Status after )
    {
        broadcast( new PropertyValidationEvent( this, property, before, after ) );
    }
    
    public final boolean disposed()
    {
        synchronized( root() )
        {
            return this.disposed;
        }
    }
    
    public final void dispose()
    {
        synchronized( root() )
        {
            this.disposed = true;
            
            if( this.elementServiceContext != null )
            {
                this.elementServiceContext.dispose();
            }
            
            for( ServiceContext context : this.propertyServiceContexts.values() )
            {
                context.dispose();
            }
            
            broadcast( new ElementDisposeEvent( this ) );
            
            disposeProperties();
            
            try
            {
                resource().dispose();
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
        }
    }
    
    protected void disposeProperties()
    {
        // The default implementation does not do anything.
    }
    
    protected final void assertNotDisposed()
    {
        if( disposed() )
        {
            final String msg = NLS.bind( Resources.elementAlreadyDisposed, this.type.getSimpleName() );
            throw new IllegalStateException( msg );
        }
    }
    
    private void logInvalidModelPathMessage( final ModelPath path )
    {
        final String message 
            = NLS.bind( Resources.invalidModelPath, this.type.getModelElementClass().getName(), path.toString() );
        
        LoggingService.log( Status.createErrorStatus( message ) );
    }
    
    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;

        for( AdapterService service : services( AdapterService.class ) )
        {
            result = service.adapt( adapterType );
            
            if( result != null )
            {
                break;
            }
        }

        if( result == null )
        {
            result = super.adapt( adapterType );
        }

        return result;
    }

    protected static final boolean equal( final String value1,
                                          final String value2 )
    {
        final String val1 = normalize( value1 );
        final String val2 = normalize( value2 );
        
        boolean valuesAreEqual = false;
        
        if( val1 == val2 )
        {
            valuesAreEqual = true;
        }
        else if( val1 != null && val2 != null )
        {
            valuesAreEqual = val1.equals( val2 );
        }
    
        return valuesAreEqual;
    }
    
    protected static final String normalize( final String value )
    {
        if( value != null && value.equals( "" ) )
        {
            return null;
        }
        
        return value;
    }
    
    protected final class EnablementRefreshResult
    {
        private final Boolean oldEnablementState;
        private final boolean newEnablementState;
        
        public EnablementRefreshResult( final Boolean before,
                                        final boolean after )
        {
            this.oldEnablementState = before;
            this.newEnablementState = after;
        }
        
        public Boolean before()
        {
            return this.oldEnablementState;
        }
        
        public boolean after()
        {
            return this.newEnablementState;
        }
        
        public boolean changed()
        {
            if( this.oldEnablementState == null )
            {
                return false;
            }
            else
            {
                return ( this.oldEnablementState != this.newEnablementState );
            }
        }
    }
    
    private final class PropagationListener extends Listener
    {
        private final ModelPath path;
        private final Listener listener;
        
        public PropagationListener( final ModelPath path,
                                    final Listener listener )
        {
            this.path = path;
            this.listener = listener;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof PropagationListener )
            {
                final PropagationListener pl = (PropagationListener) obj;
                return this.path.equals( pl.path ) && this.listener.equals( pl.listener );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.path.hashCode() ^ this.listener.hashCode();
        }
        
        @Override
        public void handle( final Event event )
        {
            if( event instanceof PropertyContentEvent )
            {
                final ModelProperty property = ( (PropertyContentEvent) event ).property();
                
                if( property instanceof ListProperty )
                {
                    final ModelElementList<?> list = read( (ListProperty) property );
                    
                    for( IModelElement x : list )
                    {
                        x.attach( this.listener, this.path );
                    }
                    
                    this.listener.handle( event );
                }
                else if( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) )
                {
                    final IModelElement element = read( (ElementProperty) property ).element();
                    
                    if( element != null )
                    {
                        element.attach( this.listener, this.path );
                    }
                    
                    this.listener.handle( event );
                }
            }
        }
    }
    
    private final class GlobalBridgeListener extends Listener
    {
        @Override
        public void handle( final Event event )
        {
            type().broadcast( event );

            if( event instanceof PropertyEvent )
            {
                final PropertyEvent evt = (PropertyEvent) event;
                evt.property().broadcast( evt );
            }
        }
    }
    
    private static final class PropertyInitializationListener extends FilteredListener<PropertyInitializationEvent>
    {
        @Override
        protected void handleTypedEvent( final PropertyInitializationEvent event )
        {
            final IModelElement element = event.element();
            final ModelProperty property = event.property();
            
            final Listener triggerRefreshListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( ! element.disposed() )
                    {
                        element.refresh( property );
                    }
                }
            };
            
            final Set<ModelPath> dependencies = element.service( property, DependenciesAggregationService.class ).dependencies();
            
            for( ModelPath dependency : dependencies )
            {
                element.attach( triggerRefreshListener, dependency );
            }
            
            for( ValidationService validationService : element.services( property, ValidationService.class ) )
            {
                validationService.attach( triggerRefreshListener );
            }
            
            final DefaultValueService defaultValueService = element.service( property, DefaultValueService.class );
            
            if( defaultValueService != null )
            {
                defaultValueService.attach( triggerRefreshListener );
            }
            
            final DerivedValueService derivedValueService = element.service( property, DerivedValueService.class );
            
            if( derivedValueService != null )
            {
                derivedValueService.attach( triggerRefreshListener );
            }

            if( property.hasAnnotation( ClearOnDisable.class ) )
            {
                Listener clearOnDisableListener = null;
                
                if( property instanceof ValueProperty )
                {
                    final ValueProperty prop = (ValueProperty) property;
                    
                    clearOnDisableListener = new FilteredListener<PropertyEnablementEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final PropertyEnablementEvent event )
                        {
                            if( event.before() == true && event.after() == false )
                            {
                                element.write( prop, null );
                            }
                        }
                    };
                }
                else if( property instanceof ListProperty )
                {
                    final ListProperty prop = (ListProperty) property;
                    
                    clearOnDisableListener = new FilteredListener<PropertyEnablementEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final PropertyEnablementEvent event )
                        {
                            if( event.before() == true && event.after() == false )
                            {
                                element.read( prop ).clear();
                            }
                        }
                    };
                }
                else if( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) )
                {
                    final ElementProperty prop = (ElementProperty) property;
                    
                    clearOnDisableListener = new FilteredListener<PropertyEnablementEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final PropertyEnablementEvent event )
                        {
                            if( event.before() == true && event.after() == false )
                            {
                                element.read( prop ).remove();
                            }
                        }
                    };
                }
                
                if( clearOnDisableListener != null )
                {
                    element.attach( clearOnDisableListener, property.getName() );
                }
            }
            
            // Note that only need to detach triggerRefreshListener from dependencies on element dispose. It isn't
            // necessary to detach from the services as these services have the same lifespan as the element.
            
            if( ! dependencies.isEmpty() )
            {
                final Listener disposeListener = new FilteredListener<ElementDisposeEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final ElementDisposeEvent event )
                    {
                        for( ModelPath dependency : dependencies )
                        {
                            element.detach( triggerRefreshListener, dependency );
                        }
                    }
                };
                
                element.attach( disposeListener );
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String invalidModelPath;
        public static String cannotReadProperty;
        public static String cannotWriteProperty;
        public static String elementAlreadyDisposed;
        
        static
        {
            initializeMessages( ModelElement.class.getName(), Resources.class );
        }
    }
    
}
