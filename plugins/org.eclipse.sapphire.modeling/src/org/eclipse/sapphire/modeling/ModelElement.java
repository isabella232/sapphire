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
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.util.MiscUtil;
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
import org.eclipse.sapphire.services.ValueNormalizationService;
import org.eclipse.sapphire.services.ValueSerializationMasterService;
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
    private final Map<ModelProperty,Object> properties;
    private Status validation;
    private final ListenerContext listeners = new ListenerContext();
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
        this.properties = new HashMap<ModelProperty,Object>( this.type.properties().size() );
        this.validation = null;
        this.propertyServiceContexts = new HashMap<ModelProperty,PropertyInstanceServiceContext>();
        
        if( parent != null )
        {
            final ModelElement p = (ModelElement) parent.nearest( IModelElement.class );
            this.listeners.coordinate( p.listeners );
        }
        
        resource.init( this );
        
        for( Listener listener : this.type.listeners() )
        {
            attach( listener );
        }
        
        for( ModelProperty property : this.type.properties() )
        {
            for( Listener listener : property.listeners() )
            {
                attach( listener, property );
            }
        }
        
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
    
    @SuppressWarnings( "unchecked" )
    
    public final <T extends IModelElement> T initialize()
    {
        for( ModelProperty property : properties() ) 
        {
            if( property instanceof ValueProperty ) 
            {
                final InitialValueService initialValueService = service( property, InitialValueService.class );
                
                if( initialValueService != null ) 
                {
                    write( property, initialValueService.value() );
                }
            }
            else if( property instanceof ImpliedElementProperty )
            {
                read( ( (ImpliedElementProperty) property ) ).element().initialize();
            }
        }
        
        return (T) this;
    }
    
    public List<ModelProperty> properties()
    {
        return this.type.properties();
    }

    public <T extends ModelProperty> T property( final String name )
    {
        return this.type.property( name );
    }

    public final Object read( final ModelProperty property )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return read( property.getName() );
    }
    
    public final Object read( final String property )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }

        Object data = this.properties.get( prop );
        
        if( data == null )
        {
            refresh( prop, true );
        }
        
        data = this.properties.get( prop );
        
        return data;
    }

    @SuppressWarnings( "unchecked" )
    
    public final <T> Value<T> read( final ValueProperty property )
    {
        assertNotDisposed();
        
        return (Value<T>) read( (ModelProperty) property );
    }
    
    @SuppressWarnings( "unchecked" )
    
    public final <T extends IModelElement> ModelElementHandle<T> read( final ElementProperty property )
    {
        assertNotDisposed();
        
        return (ModelElementHandle<T>) read( (ModelProperty) property );
    }

    @SuppressWarnings( "unchecked" )
    
    public final <T extends IModelElement> ModelElementList<T> read( final ListProperty property )
    {
        assertNotDisposed();
        
        return (ModelElementList<T>) read( (ModelProperty) property );
    }

    @SuppressWarnings( "unchecked" )
    
    public final <T> Transient<T> read( final TransientProperty property )
    {
        assertNotDisposed();
        
        return (Transient<T>) read( (ModelProperty) property );
    }
    
    public final SortedSet<String> read( final ModelPath path )
    {
        assertNotDisposed();

        final SortedSet<String> result = new TreeSet<String>();
        read( path, result );
        
        return result;
    }

    public final void read( final ModelPath path,
                            final Collection<String> result )
    {
        synchronized( root() )
        {
            assertNotDisposed();

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
    
    public final void write( final ModelProperty property,
                             final Object content )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        write( property.getName(), content );
    }
    
    public final void write( final String property,
                             final Object content )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( prop instanceof ValueProperty )
            {
                final ValueProperty p = (ValueProperty) prop;
                String value = null;
                
                if( content != null )
                {
                    if( content instanceof String )
                    {
                        value = (String) content;
                    }
                    else
                    {
                        if( p.getTypeClass().isInstance( content ) )
                        {
                            value = service( p, ValueSerializationMasterService.class ).encode( content );
                        }
                        else
                        {
                            throw new IllegalArgumentException();
                        }
                    }
                }
                
                value = p.decodeKeywords( value );
                value = service( p, ValueNormalizationService.class ).normalize( value );
                
                if( ! equal( read( p ).getText( false ), value ) )
                {
                    resource().binding( p ).write( value );
                    refresh( p );
                }
            }
            else if( prop instanceof TransientProperty )
            {
                final TransientProperty p = (TransientProperty) prop;
                final Transient<?> oldTransient = (Transient<?>) this.properties.get( p );
                
                Transient<?> t = new Transient<Object>( this, p, content );
                this.properties.put( p, t );
                
                t.init();
                
                if( ! disposed() )
                {
                    if( oldTransient == null )
                    {
                        post( new PropertyInitializationEvent( this, p ) );
                        
                        if( content != null )
                        {
                            post( new PropertyContentEvent( this, p ) );
                        }
                    }
                    else
                    {
                        if( t.equals( oldTransient ) )
                        {
                            t = oldTransient;
                            this.properties.put( p, t );
                        }
                        else
                        {
                            if( ! MiscUtil.equal( t.content(), oldTransient.content() ) )
                            {
                                post( new PropertyContentEvent( this, p ) );
                            }
                            
                            if( t.enabled() != oldTransient.enabled() )
                            {
                                post( new PropertyEnablementEvent( this, p, oldTransient.enabled(), t.enabled() ) );
                            }
                            
                            if( ! t.validation().equals( oldTransient.validation() ) )
                            {
                                post( new PropertyValidationEvent( this, p, oldTransient.validation(), t.validation() ) );
                            }
                        }
                    }
                    
                    broadcast();
                }
            }
            else
            {
                final String msg = NLS.bind( Resources.cannotWriteProperty, property );
                throw new IllegalArgumentException( msg );
            }
        }
    }
    
    public final void refresh()
    {
        assertNotDisposed();

        refresh( false, false );
    }
    
    public final void refresh( final boolean force )
    {
        assertNotDisposed();

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
        assertNotDisposed();

        refresh( property, false, false );
    }
    
    public final void refresh( final String property )
    {
        assertNotDisposed();

        refresh( property, false, false );
    }
    
    public final void refresh( final ModelProperty property,
                               final boolean force )
    {
        assertNotDisposed();

        refresh( property, force, false );
    }
    
    public final void refresh( final String property,
                               final boolean force )
    {
        assertNotDisposed();

        refresh( property, force, false );
    }

    public final void refresh( final ModelProperty property,
                               final boolean force,
                               final boolean deep )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        refresh( property.getName(), force, deep );
    }

    public final void refresh( final String property,
                               final boolean force,
                               final boolean deep )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( prop instanceof ValueProperty )
            {
                final ValueProperty p = (ValueProperty) prop;
                Value<?> value = (Value<?>) this.properties.get( p );
                
                if( value != null || force )
                {
                    final Value<?> oldValue = value;
                    String val;
                    
                    if( p.hasAnnotation( Derived.class ) )
                    {
                        val = service( p, DerivedValueService.class ).value();
                    }
                    else
                    {
                        val = resource().binding( p ).read();
                    }
                    
                    val = service( p, ValueNormalizationService.class ).normalize( p.encodeKeywords( val ) );
                    
                    if( p.hasAnnotation( Reference.class ) )
                    {
                        value = new ReferenceValue<Object,Object>( this, p, val );
                    }
                    else
                    {
                        value = new Value<Object>( this, p, val );
                    }
                    
                    this.properties.put( p, value );
                    
                    value.init();
                    
                    if( ! disposed() )
                    {
                        if( oldValue == null )
                        {
                            post( new PropertyInitializationEvent( this, p ) );
                        }
                        else
                        {
                            if( value.equals( oldValue ) )
                            {
                                value = oldValue;
                                this.properties.put( p, value );
                            }
                            else
                            {
                                if( ! equal( value.getText( false ), oldValue.getText( false ) ) || ! equal( value.getDefaultText(), oldValue.getDefaultText() ) )
                                {
                                    post( new PropertyContentEvent( this, p ) );
                                }
                                
                                if( value.enabled() != oldValue.enabled() )
                                {
                                    post( new PropertyEnablementEvent( this, p, oldValue.enabled(), value.enabled() ) );
                                }
                                
                                if( ! value.validation().equals( oldValue.validation() ) )
                                {
                                    post( new PropertyValidationEvent( this, p, oldValue.validation(), value.validation() ) );
                                }
                            }
                        }
                        
                        broadcast();
                    }
                }
            }
            else if( prop instanceof ElementProperty )
            {
                final ElementProperty p = (ElementProperty) prop;
                ModelElementHandle<?> handle = (ModelElementHandle<?>) this.properties.get( p );
                
                if( handle == null )
                {
                    if( force )
                    {
                        handle = new ModelElementHandle<IModelElement>( this, p );
                        this.properties.put( p, handle );
                        
                        handle.init();
                        
                        broadcast( new PropertyInitializationEvent( this, p ) );
                    }
                }
                else
                {
                    handle.refresh();
                }
            }
            else if( prop instanceof ListProperty )
            {
                final ListProperty p = (ListProperty) prop;
                ModelElementList<?> list = (ModelElementList<?>) this.properties.get( p );
                
                if( list == null )
                {
                    if( force )
                    {
                        list = new ModelElementList<IModelElement>( this, p );
                        this.properties.put( p, list );
                        
                        list.init( resource().binding( p ) );
                        
                        broadcast( new PropertyInitializationEvent( this, p ) );
                    }
                }
                else
                {
                    list.refresh();
                }
            }
            else if( prop instanceof TransientProperty )
            {
                Transient<?> t = (Transient<?>) this.properties.get( prop );
                
                if( t == null )
                {
                    if( force )
                    {
                        write( prop, null );
                    }
                }
                else
                {
                    write( prop, t.content() );
                }
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        
        if( deep )
        {
            if( prop instanceof ElementProperty )
            {
                final IModelElement child = read( (ElementProperty) prop ).element();
                
                if( child != null )
                {
                    child.refresh( force, true );
                }
            }
            else if( prop instanceof ListProperty )
            {
                for( IModelElement child : read( (ListProperty) prop ) )
                {
                    child.refresh( force, true );
                }
            }
        }
    }
    
    public final void copy( final IModelElement element )
    {
        assertNotDisposed();

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
                    read( prop ).element().copy( element.read( prop ).element() );
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
        assertNotDisposed();

        if( serviceType == null )
        {
            throw new IllegalArgumentException();
        }
        
        final List<S> services = services( serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    public final <S extends Service> List<S> services( final Class<S> serviceType )
    {
        assertNotDisposed();

        if( serviceType == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( this.elementServiceContext == null )
            {
                this.elementServiceContext = new ElementInstanceServiceContext( this );
                this.elementServiceContext.coordinate( this.listeners );
            }
            
            return this.elementServiceContext.services( serviceType );
        }
    }

    public final <S extends Service> S service( final ModelProperty property,
                                                final Class<S> serviceType )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( serviceType == null )
        {
            throw new IllegalArgumentException();
        }
        
        return service( property.getName(), serviceType );
    }
    
    public final <S extends Service> S service( final String property,
                                                final Class<S> serviceType )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( serviceType == null )
        {
            throw new IllegalArgumentException();
        }
        
        final List<S> services = services( property, serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }
    
    public final <S extends Service> List<S> services( final ModelProperty property,
                                                       final Class<S> serviceType )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( serviceType == null )
        {
            throw new IllegalArgumentException();
        }
        
        return services( property.getName(), serviceType );
    }

    public final <S extends Service> List<S> services( final String property,
                                                       final Class<S> serviceType )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( serviceType == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            PropertyInstanceServiceContext context = this.propertyServiceContexts.get( prop );
            
            if( context == null )
            {
                context = new PropertyInstanceServiceContext( this, prop );
                context.coordinate( this.listeners );
                this.propertyServiceContexts.put( prop, context );
            }
            
            return context.services( serviceType );
        }
    }

    public final boolean empty( final ModelProperty property )
    {
        assertNotDisposed();
    
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return empty( property.getName() );
    }

    public final boolean empty( final String property )
    {
        assertNotDisposed();
    
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }
    
        if( prop instanceof ValueProperty )
        {
            return ( read( (ValueProperty) prop ).getText( false ) == null );
        }
        else if( prop instanceof ImpliedElementProperty )
        {
            final IModelElement element = read( (ImpliedElementProperty) prop ).element();
            
            for( ModelProperty p : element.type().properties() )
            {
               if( ! element.empty( p ) )
               {
                   return false;
               }
            }
            
            return true;
        }
        else if( prop instanceof ElementProperty )
        {
            return ( read( (ElementProperty) prop ).element() == null );
        }
        else if( prop instanceof ListProperty )
        {
            return ( read( (ListProperty) prop ).isEmpty() );
        }
        else if( prop instanceof TransientProperty )
        {
            return ( read( (TransientProperty) prop ).content() == null );
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    public final boolean enabled( final ModelProperty property )
    {
        assertNotDisposed();
    
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return enabled( property.getName() );
    }

    public final boolean enabled( final String property )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }
        
        final boolean enablement;
        
        if( prop instanceof ValueProperty )
        {
            enablement = read( (ValueProperty) prop ).enabled();
        }
        else if( prop instanceof ListProperty )
        {
            enablement = read( (ListProperty) prop ).enabled();
        }
        else if( prop instanceof ElementProperty )
        {
            enablement = read( (ElementProperty) prop ).enabled();
        }
        else if( prop instanceof TransientProperty )
        {
            enablement = read( (TransientProperty) prop ).enabled();
        }
        else
        {
            throw new IllegalStateException();
        }
        
        return enablement;
    }

    public final Status validation( final ModelProperty property )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return validation( property.getName() );
    }
    
    public final Status validation( final String property )
    {
        assertNotDisposed();

        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        final ModelProperty prop = this.type.property( property );
        
        if( prop == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Status validation;
        
        if( prop instanceof ValueProperty )
        {
            validation = read( (ValueProperty) prop ).validation();
        }
        else if( prop instanceof ListProperty )
        {
            validation = read( (ListProperty) prop ).validation();
        }
        else if( prop instanceof ElementProperty )
        {
            validation = read( (ElementProperty) prop ).validation();
        }
        else if( prop instanceof TransientProperty )
        {
            validation = read( (TransientProperty) prop ).validation();
        }
        else
        {
            throw new IllegalStateException();
        }
        
        return validation;
    }
    
    public final Status validation()
    {
        assertNotDisposed();
    
        if( this.validation == null )
        {
            refreshValidationResult();
        }
        
        return this.validation;
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
    
    public ListenerContext listeners()
    {
        return this.listeners;
    }
    
    public final boolean attach( final Listener listener )
    {
        assertNotDisposed();

        return this.listeners.attach( listener );
    }
    
    public final void attach( final Listener listener,
                              final String path )
    {
        assertNotDisposed();

        attach( listener, new ModelPath( path ) );
    }
    
    public final void attach( final Listener listener,
                              final ModelPath path )
    {
        assertNotDisposed();

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
                    if( property instanceof ElementProperty )
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
                    read( (ImpliedElementProperty) property ).element().attach( listener, tail );
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
    
    public final void attach( final Listener listener,
                              final ModelProperty property )
    {
        assertNotDisposed();

        attach( listener, property.getName() );
    }
    
    public final boolean detach( final Listener listener )
    {
        if( disposed() )
        {
            return false;
        }
        
        return this.listeners.detach( listener );
    }
    
    public final void detach( final Listener listener,
                              final String path )
    {
        if( disposed() )
        {
            return;
        }
        
        detach( listener, new ModelPath( path ) );
    }
    
    public final void detach( final Listener listener,
                              final ModelPath path )
    {
        if( disposed() )
        {
            return;
        }
        
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
                    read( (ImpliedElementProperty) property ).element().detach( listener, path );
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
                    read( (ImpliedElementProperty) property ).element().detach( listener, tail );
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
    
    public final void detach( final Listener listener,
                              final ModelProperty property )
    {
        if( disposed() )
        {
            return;
        }
        
        detach( listener, property.getName() );
    }
    
    protected final void post( final Event event )
    {
        this.listeners.post( event );
    }

    protected final void broadcast()
    {
        this.listeners.broadcast();
    }
    
    protected final void broadcast( final Event event )
    {
        this.listeners.broadcast( event );
    }
    
    final void broadcastPropertyContentEvent( final ModelProperty property )
    {
        broadcast( new PropertyContentEvent( this, property ) );
    }

    final void broadcastPropertyEnablementEvent( final ModelProperty property,
                                                 final boolean before,
                                                 final boolean after )
    {
        broadcast( new PropertyEnablementEvent( this, property, before, after ) );
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
            if( ! this.disposed )
            {
                this.disposed = true;
                
                broadcast( new ElementDisposeEvent( this ) );
                
                if( this.elementServiceContext != null )
                {
                    this.elementServiceContext.dispose();
                }
                
                for( ServiceContext context : this.propertyServiceContexts.values() )
                {
                    context.dispose();
                }
                
                for( Object data : this.properties.values() )
                {
                    if( data instanceof ModelElementHandle )
                    {
                        final IModelElement element = ( (ModelElementHandle<?>) data ).element( false );
                        
                        if( element != null )
                        {
                            element.dispose();
                        }
                    }
                    else if( data instanceof ModelElementList )
                    {
                        for( IModelElement element : (ModelElementList<?>) data )
                        {
                            element.dispose();
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
            }
        }
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
        assertNotDisposed();

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
            
            for( EnablementService enablementService : element.services( property, EnablementService.class ) )
            {
                enablementService.attach( triggerRefreshListener );
            }
            
            element.service( property, ValidationAggregationService.class ).attach( triggerRefreshListener );
            
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
                    element.attach( clearOnDisableListener, property );
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
        public static String cannotWriteProperty;
        public static String elementAlreadyDisposed;
        
        static
        {
            initializeMessages( ModelElement.class.getName(), Resources.class );
        }
    }
    
}
