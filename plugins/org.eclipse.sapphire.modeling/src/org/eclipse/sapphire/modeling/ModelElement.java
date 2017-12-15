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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.AllSiblingsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesChangedEvent;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderListener;
import org.eclipse.sapphire.modeling.extensibility.ServicesExtensionPoint;
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
    private final Map<Class<? extends ModelElementService>,ModelElementService> services;
    
    public ModelElement( final ModelElementType type,
                         final IModelParticle parent,
                         final ModelProperty parentProperty )
    {
        super( parent );
        
        this.type = type;
        this.parentProperty = parentProperty;
        this.valres = null;
        this.listeners = null;
        this.propertyListeners = null;
        this.enablementStatuses = new HashMap<ModelProperty,Boolean>();
        this.services = new HashMap<Class<? extends ModelElementService>,ModelElementService>();
        
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
                final PossibleValuesProviderImpl possibleValuesProvider = service().getPossibleValuesProvider( (ValueProperty) property );
                
                if( possibleValuesProvider != null )
                {
                    possibleValuesProvider.addListener
                    (
                        new PossibleValuesProviderListener()
                        {
                            @Override
                            public void handlePossibleValuesChangedEvent( final PossibleValuesChangedEvent event )
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
        synchronized( this.model )
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
                final IModelElement child = service().read( (ElementProperty) property );
                
                if( child != null )
                {
                    child.refresh( force, true );
                }
            }
            else if( property instanceof ListProperty )
            {
                for( IModelElement child : service().read( (ListProperty) property ) )
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
        synchronized( this.model )
        {
            ModelElementService service = this.services.get( serviceType );
            
            if( service == null )
            {
                service = ServicesExtensionPoint.getService( serviceType );
                
                if( service != null )
                {
                    service.init( this );
                    this.services.put( serviceType, service );
                }
            }
            
            return (S) service;
        }
    }
    
    public final StandardModelElementService service()
    {
        return service( StandardModelElementService.class );
    }
    
    public final boolean isPropertyEnabled( final ModelProperty property )
    {
        synchronized( this.model )
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
        synchronized( this.model )
        {
            final Boolean oldStatus = this.enablementStatuses.get( property );
            final boolean newStatus = service().isEnabled( property );
            
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
            if( service().isEnabled( property ) )
            {
                final Object val = property.invokeGetterMethod( this );
                IStatus x = null;
                
                if( property instanceof ValueProperty )
                {
                    x = ( (Value<?>) val ).validate();
                }
                else if( property instanceof ListProperty )
                {
                	if ( val != null )
                	{
                		x = ( (ModelElementList<?>) val ).validate();
                	}
                }
                else if( property instanceof ElementProperty )
                {
                    if( val != null )
                    {
                        x = ( (IModelElement) val ).validate();
                    }
                }
                else
                {
                    throw new IllegalStateException();
                }
                
                if( x != null )
                {
                    st.add( x );
                }
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
            
            final IModelParticle parent = getParent();
            
            if( parent != null && parent instanceof IModelElement )
            {
                ( (IModelElement) parent ).notifyPropertyChangeListeners( this.parentProperty );
            }
        }
    }

    protected void validateEdit()
    {
        this.model.validateEdit();
    }
    
    public final void addListener( final ModelElementListener listener )
    {
        synchronized( this.model )
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
        synchronized( this.model )
        {
            final ModelPath.Segment head = path.head();
            
            if( head instanceof ModelRootSegment )
            {
                getModel().addListener( listener, path.tail() );
            }
            else if( head instanceof ParentElementSegment )
            {
                IModelParticle parent = getParent();
                
                if( parent == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                else
                {
                    if( parent instanceof ModelElementList<?> )
                    {
                        parent = parent.getParent();
                    }
                }
                
                ( (IModelElement) parent ).addListener( listener, path.tail() );
            }
            else if( head instanceof AllSiblingsSegment )
            {
                IModelParticle parent = getParent();
                
                if( parent == null || ! ( parent instanceof ModelElementList<?> ) )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                
                parent = parent.getParent();
                
                final ModelPath p = ( new ModelPath( this.parentProperty.getName() ) ).append( path.tail() );
                ( (IModelElement) parent ).addListener( listener, p );
            }
            else if( head instanceof AllDescendentsSegment )
            {
                for( ModelProperty property : this.type.getProperties() )
                {
                    final Set<ModelPropertyListener> listeners = getListenersForEdit( property );
                    
                    listeners.add( listener );
                    
                    if( ! ( property instanceof ValueProperty ) )
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
                    else
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
        synchronized( this.model )
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
        synchronized( this.model )
        {
            final ModelPath.Segment head = path.head();
            
            if( head instanceof ModelRootSegment )
            {
                getModel().removeListener( listener, path.tail() );
            }
            else if( head instanceof ParentElementSegment )
            {
                IModelParticle parent = getParent();
                
                if( parent == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                else
                {
                    if( parent instanceof ModelElementList<?> )
                    {
                        parent = parent.getParent();
                    }
                }
                
                ( (IModelElement) parent ).removeListener( listener, path.tail() );
            }
            else if( head instanceof AllSiblingsSegment )
            {
                IModelParticle parent = getParent();
                
                if( parent == null || ! ( parent instanceof ModelElementList<?> ) )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                
                parent = parent.getParent();
                
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
                        
                        final Object val = property.invokeGetterMethod( ModelElement.this );
                        
                        if( property instanceof ElementProperty )
                        {
                            if( val != null )
                            {
                                ( (IModelElement) val ).removeListener( listener, path );
                            }
                        }
                        else if( property instanceof ListProperty )
                        {
                            final ModelElementList<?> list = (ModelElementList<?>) val;
                            
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
                    
                    final Object val = property.invokeGetterMethod( ModelElement.this );
                    final ModelPath tail = path.tail();
                    
                    if( property instanceof ElementProperty )
                    {
                        if( val != null )
                        {
                            ( (IModelElement) val ).removeListener( listener, tail );
                        }
                    }
                    else if( property instanceof ListProperty )
                    {
                        final ModelElementList<?> list = (ModelElementList<?>) val;
                        
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
        
        synchronized( this.model )
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
        
        synchronized( this.model )
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
        synchronized( this.model )
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
            final Object val = this.property.invokeGetterMethod( ModelElement.this );
            
            if( this.property instanceof ElementProperty )
            {
                if( val != null )
                {
                    ( (IModelElement) val ).addListener( this.listener, this.path );
                }
            }
            else
            {
                final ModelElementList<?> list = (ModelElementList<?>) val;
                
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
        
        static
        {
            initializeMessages( ModelElement.class.getName(), Resources.class );
        }
    }
    
}
