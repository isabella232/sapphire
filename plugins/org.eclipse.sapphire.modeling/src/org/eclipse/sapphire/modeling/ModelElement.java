/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.AllSiblingsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.modeling.annotations.ClearOnDisable;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DependenciesAggregationService;
import org.eclipse.sapphire.services.DerivedValueService;
import org.eclipse.sapphire.services.EnablementService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;
import org.eclipse.sapphire.services.internal.ElementInstanceServiceContext;
import org.eclipse.sapphire.services.internal.PropertyInstanceServiceContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelElement

    extends ModelParticle
    implements IModelElement
    
{
    private final ModelElementType type;
    private final ModelProperty parentProperty;
    private Status valres;
    private Set<ModelElementListener> listeners;
    private Map<ModelProperty,Set<ModelPropertyListener>> propertyListeners;
    private final Map<ModelProperty,Boolean> enablementStatuses;
    private boolean enablementServicesInitialized;
    private ElementInstanceServiceContext elementServiceContext;
    private final Map<ModelProperty,PropertyInstanceServiceContext> propertyServiceContexts;
    
    public ModelElement( final ModelElementType type,
                         final IModelParticle parent,
                         final ModelProperty parentProperty,
                         final Resource resource )
    {
        super( parent, resource );
        
        this.type = type;
        this.parentProperty = parentProperty;
        this.valres = null;
        this.listeners = null;
        this.propertyListeners = null;
        this.enablementStatuses = new HashMap<ModelProperty,Boolean>();
        this.propertyServiceContexts = new HashMap<ModelProperty,PropertyInstanceServiceContext>();
        
        resource.init( this );
        
        final Map<ModelProperty,ModelPropertyListener> listenerByProperty = new HashMap<ModelProperty,ModelPropertyListener>();
        final Map<ModelProperty,Set<ModelPath>> dependenciesByProperty = new HashMap<ModelProperty,Set<ModelPath>>();
        
        for( final ModelProperty property : type.getProperties() )
        {
            final Set<ModelPath> dependencies = service( property, DependenciesAggregationService.class ).dependencies();
            
            if( ! dependencies.isEmpty() )
            {
                final ModelPropertyListener listener = new ModelPropertyListener()
                {
                    @Override
                    public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                    {
                        refresh( property );
                    }
                };
                
                listenerByProperty.put( property, listener );
                dependenciesByProperty.put( property, dependencies );
    
                for( ModelPath dependency : dependencies )
                {
                    addListener( listener, dependency );
                }
            }

            final Listener triggerRefreshOnServiceEventListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refresh( property );
                }
            };
            
            for( ValidationService validationService : services( property, ValidationService.class ) )
            {
                validationService.attach( triggerRefreshOnServiceEventListener );
            }
            
            final DefaultValueService defaultValueService = service( property, DefaultValueService.class );
            
            if( defaultValueService != null )
            {
                defaultValueService.attach( triggerRefreshOnServiceEventListener );
            }
            
            final DerivedValueService derivedValueService = service( property, DerivedValueService.class );
            
            if( derivedValueService != null )
            {
                derivedValueService.attach( triggerRefreshOnServiceEventListener );
            }

            if( property.hasAnnotation( ClearOnDisable.class ) )
            {
                ModelPropertyListener listener = null;
                
                if( property instanceof ValueProperty )
                {
                    final ValueProperty prop = (ValueProperty) property;
                    
                    listener = new ModelPropertyListener()
                    {
                        @Override
                        public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                        {
                            if( event.getOldEnablementState() == true && event.getNewEnablementState() == false )
                            {
                                write( prop, null );
                            }
                        }
                    };
                }
                else if( property instanceof ListProperty )
                {
                    final ListProperty prop = (ListProperty) property;
                    
                    listener = new ModelPropertyListener()
                    {
                        @Override
                        public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                        {
                            if( event.getOldEnablementState() == true && event.getNewEnablementState() == false )
                            {
                                read( prop ).clear();
                            }
                        }
                    };
                }
                else if( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) )
                {
                    final ElementProperty prop = (ElementProperty) property;
                    
                    listener = new ModelPropertyListener()
                    {
                        @Override
                        public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                        {
                            if( event.getOldEnablementState() == true && event.getNewEnablementState() == false )
                            {
                                read( prop ).remove();
                            }
                        }
                    };
                }
                
                if( listener != null )
                {
                    addListener( listener, property.getName() );
                }
            }
        }
        
        if( ! listenerByProperty.isEmpty() )
        {
            final ModelElementListener disposeListener = new ModelElementListener()
            {
                @Override
                public void handleElementDisposedEvent( final ModelElementDisposedEvent event )
                {
                    for( Map.Entry<ModelProperty,ModelPropertyListener> entry : listenerByProperty.entrySet() )
                    {
                        final ModelProperty property = entry.getKey();
                        final ModelPropertyListener listener = entry.getValue();
                        
                        for( ModelPath dependency : dependenciesByProperty.get( property ) )
                        {
                            removeListener( listener, dependency );
                        }
                    }
                }
            };
            
            addListener( disposeListener );
        }
    }

    public ModelElementType getModelElementType()
    {
        return this.type;
    }
    
    public ModelProperty getParentProperty()
    {
        return this.parentProperty;
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
                for( ModelProperty property : getModelElementType().getProperties() )
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
                final String t = getModelElementType().getSimpleName();
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
                final ModelProperty property = getModelElementType().getProperty( propertyName );

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
            for( ModelProperty property : getModelElementType().getProperties() )
            {
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
        if( this.type != element.getModelElementType() )
        {
            throw new IllegalArgumentException();
        }
        
        for( ModelProperty property : this.type.getProperties() )
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
                        final IModelElement thisChild = handle.element( true, elementChild.getModelElementType() );
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
                        final IModelElement thisChild = list.addNewElement( elementChild.getModelElementType() );
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

    public final boolean isPropertyEnabled( final ModelProperty property )
    {
        synchronized( root() )
        {
            Boolean status = this.enablementStatuses.get( property );
            
            if( status == null )
            {
                refreshPropertyEnablement( property, true );
                status = this.enablementStatuses.get( property );
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
                for( final ModelProperty prop : this.type.getProperties() )
                {
                    final Listener enablementServiceListener = new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            refreshPropertyEnablement( prop, true );
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
                    notifyPropertyChangeListeners( new ModelPropertyChangeEvent( this, property, oldState, newState ) );
                }
            }
            
            return new EnablementRefreshResult( oldState, newState );
        }
    }

    public Status validate()
    {
        if( this.valres == null )
        {
            refreshValidationResult();
        }
        
        return this.valres;
    }
    
    private void refreshValidationResult()
    {
        final Status.CompositeStatusFactory factory = Status.factoryForComposite();
        
        for( ModelProperty property : this.type.getProperties() )
        {
            if( isPropertyEnabled( property ) )
            {
                final Status x;
                
                if( property instanceof ValueProperty )
                {
                    x = read( (ValueProperty) property ).validate();
                }
                else if( property instanceof ListProperty )
                {
                    x = read( (ListProperty) property ).validate();
                }
                else if( property instanceof ImpliedElementProperty )
                {
                    x = read( (ImpliedElementProperty) property ).validate();
                }
                else if( property instanceof ElementProperty )
                {
                    x = read( (ElementProperty) property ).validate();
                }
                else if( property instanceof TransientProperty )
                {
                    x = read( (TransientProperty) property ).validate();
                }
                else
                {
                    throw new IllegalStateException();
                }
                
                factory.add( x );
            }
        }
        
        final Status st = factory.create();
        
        if( this.valres == null )
        {
            this.valres = st;
        }
        else if( ! this.valres.equals( st ) )
        {
            final Status oldValidationState = this.valres;
            this.valres = st;
            
            notifyValidationStateChangeListeners( oldValidationState, this.valres );
            
            final IModelParticle parent = parent();
            
            if( parent != null && parent instanceof IModelElement )
            {
                ( (IModelElement) parent ).notifyPropertyChangeListeners( this.parentProperty );
            }
        }
    }

    public final void addListener( final ModelElementListener listener )
    {
        synchronized( root() )
        {
            if( this.listeners == null )
            {
                this.listeners = new CopyOnWriteArraySet<ModelElementListener>();
            }
            
            this.listeners.add( listener );
        }
    }
    
    public final void addListener( final ModelPropertyListener listener,
                                   final String path )
    {
        addListener( listener, new ModelPath( path ) );
    }
    
    public final void addListener( final ModelPropertyListener listener,
                                   final ModelPath path )
    {
        synchronized( root() )
        {
            final ModelPath.Segment head = path.head();
            
            if( head instanceof ModelRootSegment )
            {
                ( (IModelElement) root() ).addListener( listener, path.tail() );
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
                
                ( (IModelElement) parent ).addListener( listener, path.tail() );
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
                ( (IModelElement) parent ).addListener( listener, p );
            }
            else if( head instanceof AllDescendentsSegment )
            {
                for( ModelProperty property : this.type.getProperties() )
                {
                    final Set<ModelPropertyListener> listeners = getListenersForEdit( property );
                    
                    listeners.add( listener );
                    
                    if( property instanceof ListProperty || property instanceof ElementProperty )
                    {
                        final PropagationListener pListener 
                            = new PropagationListener( property, path, listener );
                        
                        if( listeners.add( pListener ) )
                        {
                            pListener.handlePropertyChangedEvent( null );
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
                    addListener( listener, path.tail() );
                }
            }
            else
            {
                final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
                final ModelProperty property = this.type.getProperty( propertyName );
                
                if( property == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }

                final Set<ModelPropertyListener> listeners = getListenersForEdit( property );
                
                if( path.length() == 1 )
                {
                    listeners.add( listener );
                }
                else
                {
                    if( property instanceof ValueProperty )
                    {
                        logInvalidModelPathMessage( path );
                        return;
                    }
                    else if( property instanceof ListProperty || property instanceof ElementProperty )
                    {
                        final PropagationListener pListener 
                            = new PropagationListener( property, path.tail(), listener );
                        
                        if( listeners.add( pListener ) )
                        {
                            pListener.handlePropertyChangedEvent( null );
                        }
                    }
                }
            }
        }
    }
    
    public final void removeListener( final ModelElementListener listener )
    {
        synchronized( root() )
        {
            if( this.listeners != null )
            {
                this.listeners.remove( listener );
            }
        }
    }
    
    public final void removeListener( final ModelPropertyListener listener,
                                      final String path )
    {
        removeListener( listener, new ModelPath( path ) );
    }
    
    public final void removeListener( final ModelPropertyListener listener,
                                      final ModelPath path )
    {
        synchronized( root() )
        {
            final ModelPath.Segment head = path.head();
            
            if( head instanceof ModelRootSegment )
            {
                ( (IModelElement) root() ).removeListener( listener, path.tail() );
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
                
                ( (IModelElement) parent ).removeListener( listener, path.tail() );
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
                ( (IModelElement) parent ).removeListener( listener, p );
            }
            else if( head instanceof AllDescendentsSegment )
            {
                for( ModelProperty property : this.type.getProperties() )
                {
                    final Set<ModelPropertyListener> listeners = getListenersForEdit( property );
                    
                    listeners.remove( listener );
                    
                    if( ! ( property instanceof ValueProperty ) )
                    {
                        final PropagationListener pListener 
                            = new PropagationListener( property, path, listener );
                        
                        listeners.remove( pListener );
                        
                        if( property instanceof ElementProperty )
                        {
                            IModelElement element = null;
                            if( property instanceof ImpliedElementProperty )
                            {
                                element = read( (ImpliedElementProperty) property );
                            }
                            else
                            {
                                element = read( (ElementProperty) property ).element();
                            }
                                                                                
                            if( element != null )
                            {
                                element.removeListener( listener, path );
                            }
                        }
                        else if( property instanceof ListProperty )
                        {
                            final ModelElementList<?> list = read( (ListProperty) property );
                            
                            for( IModelElement x : list )
                            {
                                x.removeListener( listener, path );
                            }
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
                    removeListener( listener, path.tail() );
                }
            }
            else
            {
                final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
                final ModelProperty property = this.type.getProperty( propertyName );
                
                if( property == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }

                final Set<ModelPropertyListener> listeners = getListenersForEdit( property );
                
                if( path.length() == 1 )
                {
                    listeners.remove( listener );
                }
                else
                {
                    final PropagationListener pListener 
                        = new PropagationListener( property, path.tail(), listener );
                    
                    listeners.remove( pListener );
                    
                    final ModelPath tail = path.tail();
                    
                    if( property instanceof ElementProperty )
                    {
                        IModelElement element = null;
                        if( property instanceof ImpliedElementProperty )
                        {
                            element = read( (ImpliedElementProperty) property );
                        }
                        else
                        {
                            element = read( (ElementProperty) property ).element();
                        }
                                                
                        if( element != null )
                        {
                            element.removeListener( listener, tail );
                        }
                    }
                    else if( property instanceof ListProperty )
                    {
                        final ModelElementList<?> list = read( (ListProperty) property );
                        
                        for( IModelElement x : list )
                        {
                            x.removeListener( listener, tail );
                        }
                    }
                }
            }
        }
    }
    
    public final void notifyPropertyChangeListeners( final ModelProperty property )
    {
        final boolean enabled = isPropertyEnabled( property );
        notifyPropertyChangeListeners( new ModelPropertyChangeEvent( this, property, enabled, enabled ) );
    }
    
    protected final void notifyPropertyChangeListeners( final ModelProperty property,
                                                        final EnablementRefreshResult enablementRefreshResult )
    {
        notifyPropertyChangeListeners( new ModelPropertyChangeEvent( this, property, enablementRefreshResult.before(), enablementRefreshResult.after() ) );
    }

    private void notifyPropertyChangeListeners( final ModelPropertyChangeEvent event )
    {
        synchronized( root() )
        {
            if( this.valres != null )
            {
                refreshValidationResult();
            }
            
            if( this.listeners != null )
            {
                for( ModelElementListener listener : this.listeners )
                {
                    try
                    {
                        listener.propertyChanged( event );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                }
            }
            
            if( this.propertyListeners != null )
            {
                final Set<ModelPropertyListener> listeners = this.propertyListeners.get( event.getProperty() );
                
                if( listeners != null )
                {
                    for( ModelPropertyListener listener : listeners )
                    {
                        try
                        {
                            listener.handlePropertyChangedEvent( event );
                        }
                        catch( Exception e )
                        {
                            LoggingService.log( e );
                        }
                    }
                }
            }
            
            for( ModelPropertyListener listener : event.getProperty().getListeners() )
            {
                try
                {
                    listener.handlePropertyChangedEvent( event );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
        }
    }

    private void notifyValidationStateChangeListeners( final Status oldValidationState,
                                                       final Status newValidationState )
    {
        final ValidationStateChangeEvent event = new ValidationStateChangeEvent( this, oldValidationState, newValidationState );
        
        synchronized( root() )
        {
            if( this.listeners != null )
            {
                for( ModelElementListener listener : this.listeners )
                {
                    try
                    {
                        listener.validationStateChanged( event );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                }
            }
        }
    }
    
    private Set<ModelPropertyListener> getListenersForEdit( final ModelProperty property )
    {
        if( this.propertyListeners == null )
        {
            this.propertyListeners = new HashMap<ModelProperty,Set<ModelPropertyListener>>();
        }
        
        Set<ModelPropertyListener> set = this.propertyListeners.get( property );
        
        if( set == null )
        {
            set = new HashSet<ModelPropertyListener>();
        }
        else
        {
            set = new HashSet<ModelPropertyListener>( set );
        }
        
        this.propertyListeners.put( property, set );
        
        return set;
    }
    
    public final void dispose()
    {
        synchronized( root() )
        {
            if( this.listeners != null )
            {
                final ModelElementDisposedEvent event = new ModelElementDisposedEvent( this );
                
                for( ModelElementListener listener : this.listeners )
                {
                    try
                    {
                        listener.handleElementDisposedEvent( event );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                }
            }
            
            for( ModelProperty property : this.type.getProperties() )
            {
                if( property instanceof ListProperty )
                {
                    for( IModelElement child : read( (ListProperty) property ) )
                    {
                        child.dispose();
                    }
                }
                else if( property instanceof ImpliedElementProperty )
                {
                    read( (ImpliedElementProperty) property ).dispose();
                }
                else if( property instanceof ElementProperty )
                {
                    final IModelElement child = read( (ElementProperty) property ).element( false );
                    
                    if( child != null )
                    {
                        child.dispose();
                    }
                }
            }
            
            try
            {
                resource().dispose();
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
            
            if( this.elementServiceContext != null )
            {
                this.elementServiceContext.dispose();
            }
            
            for( ServiceContext context : this.propertyServiceContexts.values() )
            {
                context.dispose();
            }
        }
    }
    
    private void logInvalidModelPathMessage( final ModelPath path )
    {
        final String message 
            = NLS.bind( Resources.invalidModelPath, this.type.getModelElementClass().getName(), path.toString() );
        
        LoggingService.log( Status.createErrorStatus( message ) );
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
    
    private final class PropagationListener
    
        extends ModelPropertyListener
        
    {
        private final ModelProperty property;
        private final ModelPath path;
        private final ModelPropertyListener listener;
        
        public PropagationListener( final ModelProperty property,
                                    final ModelPath path,
                                    final ModelPropertyListener listener )
        {
            if( property instanceof ValueProperty )
            {
                throw new IllegalArgumentException();
            }
            
            this.property = property;
            this.path = path;
            this.listener = listener;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof PropagationListener )
            {
                final PropagationListener pl = (PropagationListener) obj;
                
                return this.property == pl.property &&
                       this.path.equals( pl.path ) && 
                       this.listener == pl.listener;
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.property.hashCode() ^ this.path.hashCode();
        }
        
        @Override
        public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
        {
            if( this.property instanceof ElementProperty )
            {
                final IModelElement element;
                
                if( this.property instanceof ImpliedElementProperty )
                {
                    element = read( (ImpliedElementProperty) this.property );
                }
                else
                {
                    element = read( (ElementProperty) this.property ).element();
                }
                
                if( element != null )
                {
                    element.addListener( this.listener, this.path );
                }
            }
            else
            {
                final ModelElementList<?> list = read( (ListProperty) this.property );
                
                for( IModelElement x : list )
                {
                    x.addListener( this.listener, this.path );
                }
            }
            
            this.listener.handlePropertyChangedEvent( event );
        }
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String invalidModelPath;
        public static String cannotReadProperty;
        public static String cannotWriteProperty;
        
        static
        {
            initializeMessages( ModelElement.class.getName(), Resources.class );
        }
    }
    
}
