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

package org.eclipse.sapphire.modeling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.AllSiblingsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.modeling.internal.SapphireModelingExtensionSystem;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelElement

    extends ModelParticle
    implements IModelElement
    
{
    private final ModelElementType type;
    private final ModelProperty parentProperty;
    private IStatus valres;
    private Set<ModelElementListener> listeners;
    private Map<ModelProperty,Set<ModelPropertyListener>> propertyListeners;
    private final Map<ModelProperty,Boolean> enablementStatuses;
    private final Map<Class<? extends ModelElementService>,ModelElementService> elementServices;
    private final Map<PropertyServiceKey,ModelPropertyService> propertyServices;
    
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
        this.elementServices = new HashMap<Class<? extends ModelElementService>,ModelElementService>();
        this.propertyServices = new HashMap<PropertyServiceKey,ModelPropertyService>();
        
        resource.init( this );
        
        final Map<ModelProperty,ModelPropertyListener> refreshListeners
            = new HashMap<ModelProperty,ModelPropertyListener>();
        
        for( final ModelProperty property : type.getProperties() )
        {
            final Set<ModelPath> dependencies = property.getDependencies();
            
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
                
                refreshListeners.put( property, listener );
    
                for( ModelPath depPath : dependencies )
                {
                    addListener( listener, depPath );
                }
            }
            
            if( property instanceof ValueProperty )
            {
                final PossibleValuesService possibleValuesProvider = service( property, PossibleValuesService.class );
                
                if( possibleValuesProvider != null )
                {
                    possibleValuesProvider.addListener
                    (
                        new PossibleValuesService.Listener()
                        {
                            @Override
                            public void handlePossibleValuesChangedEvent( final PossibleValuesService.PossibleValuesChangedEvent event )
                            {
                                refresh( property );
                            }
                        }
                    );
                }
            }
        }
        
        if( ! refreshListeners.isEmpty() )
        {
            final ModelElementListener disposeListener = new ModelElementListener()
            {
                @Override
                public void handleElementDisposedEvent( final ModelElementDisposedEvent event )
                {
                    for( Map.Entry<ModelProperty,ModelPropertyListener> entry : refreshListeners.entrySet() )
                    {
                        final ModelProperty property = entry.getKey();
                        final ModelPropertyListener listener = entry.getValue();
                        
                        for( ModelPath depPath : property.getDependencies() )
                        {
                            removeListener( listener, depPath );
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
    
    @SuppressWarnings( "unchecked" )
    
    public final <S extends ModelElementService> S service( final Class<S> serviceType )
    {
        synchronized( root() )
        {
            ModelElementService service = this.elementServices.get( serviceType );
            
            if( service == null )
            {
                service = SapphireModelingExtensionSystem.createModelElementService( this, serviceType );
                
                if( service != null )
                {
                    service.init( this );
                    this.elementServices.put( serviceType, service );
                }
            }
            
            return (S) service;
        }
    }
    
    @SuppressWarnings( "unchecked" )
    
    public final <S extends ModelPropertyService> S service( final ModelProperty property,
                                                             final Class<S> serviceType )
    {
        synchronized( root() )
        {
            final PropertyServiceKey key = new PropertyServiceKey( property, serviceType );
            ModelPropertyService service = this.propertyServices.get( key );
            
            if( service == null )
            {
                service = SapphireModelingExtensionSystem.createModelPropertyService( this, property, serviceType );
                
                if( service != null )
                {
                    this.propertyServices.put( key, service );
                }
            }
            
            return (S) service;
        }
    }
    
    public boolean isPropertyEnabled( final ModelProperty property )
    {
        synchronized( root() )
        {
            Boolean status = this.enablementStatuses.get( property );
            
            if( status == null )
            {
                refreshPropertyEnabledStatus( property );
                status = this.enablementStatuses.get( property );
            }
            
            return status;
        }
    }
    
    protected final boolean refreshPropertyEnabledStatus( final ModelProperty property )
    {
        synchronized( root() )
        {
            final Boolean oldStatus = this.enablementStatuses.get( property );
            final boolean newStatus = service( property, EnablementService.class ).isEnabled();
            
            this.enablementStatuses.put( property, newStatus );
            
            return ( oldStatus != null && ! oldStatus.equals( newStatus ) );
        }
    }

    public IStatus validate()
    {
        if( this.valres == null )
        {
            refreshValidationResult();
        }
        
        return this.valres;
    }
    
    private void refreshValidationResult()
    {
        final SapphireMultiStatus st = new SapphireMultiStatus();
        
        for( ModelProperty property : this.type.getProperties() )
        {
            if( service( property, EnablementService.class ).isEnabled() )
            {
                final IStatus x;
                
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
                
                st.add( x );
            }
        }
        
        if( this.valres == null )
        {
            this.valres = st;
        }
        else if( ! this.valres.equals( st ) )
        {
            final IStatus oldValidationState = this.valres;
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
                            final IModelElement element = read( (ElementProperty) property ).element();
                            
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
                        final IModelElement element = read( (ElementProperty) property ).element();
                        
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
        final ModelPropertyChangeEvent event = new ModelPropertyChangeEvent( this, property );
        
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
                        SapphireModelingFrameworkPlugin.log( e );
                    }
                }
            }
            
            if( this.propertyListeners != null )
            {
                final Set<ModelPropertyListener> listeners = this.propertyListeners.get( property );
                
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
                            SapphireModelingFrameworkPlugin.log( e );
                        }
                    }
                }
            }
            
            for( ModelPropertyListener listener : property.getListeners() )
            {
                try
                {
                    listener.handlePropertyChangedEvent( event );
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
        }
    }

    private void notifyValidationStateChangeListeners( final IStatus oldValidationState,
                                                       final IStatus newValidationState )
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
                        SapphireModelingFrameworkPlugin.log( e );
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
                        SapphireModelingFrameworkPlugin.log( e );
                    }
                }
            }
        }
    }
    
    private void logInvalidModelPathMessage( final ModelPath path )
    {
        final String message 
            = NLS.bind( Resources.invalidModelPath, this.type.getModelElementClass().getName(), path.toString() );
        
        SapphireModelingFrameworkPlugin.logError( message, null );
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
    
    private static final class PropertyServiceKey
    {
        private final ModelProperty property;
        private final Class<? extends ModelPropertyService> serviceType;
        
        public PropertyServiceKey( final ModelProperty property,
                                   final Class<? extends ModelPropertyService> serviceType )
        {
            this.property = property;
            this.serviceType = serviceType;
        }

        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof PropertyServiceKey )
            {
                final PropertyServiceKey key = (PropertyServiceKey) obj;
                return ( this.property.equals( key.property ) && this.serviceType.equals( key.serviceType ) );
            }
            
            return false;
        }

        @Override
        public int hashCode()
        {
            return this.property.hashCode() ^ this.serviceType.hashCode();
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
