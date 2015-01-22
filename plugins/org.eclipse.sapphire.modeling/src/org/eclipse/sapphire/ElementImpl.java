/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [372359] Provide means to extend the behavior of adapt methods
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.sapphire.internal.NonSuspendableListener;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ElementEvent;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.AllSiblingsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.ModelPath.PropertySegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.SourceLanguageLocalizationService;
import org.eclipse.sapphire.services.EqualityService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.internal.ElementInstanceServiceContext;
import org.eclipse.sapphire.util.MapFactory;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.sapphire.util.SetFactory;
import org.eclipse.sapphire.util.SortedSetFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class ElementImpl implements Element
{
    @Text( "{0} element is already disposed." )
    private static LocalizableText elementAlreadyDisposed;
    
    @Text( "Path \"{1}\" is invalid for {0}." )
    private static LocalizableText illegalPathException;
    
    static
    {
        LocalizableText.init( ElementImpl.class );
    }

    private static final Comparator<Property> PROPERTY_INSTANCE_COMPARATOR = new Comparator<Property>()
    {
        public int compare( final Property x, final Property y )
        {
            return x.name().compareToIgnoreCase( y.name() );
        }
    };

    private final ElementType type;
    private final Property parent;
    private final Resource resource;
    private final SortedSet<Property> properties;
    private final Map<String,Property> propertiesByName;
    private ListenerContext listeners;
    private ElementInstanceServiceContext elementServiceContext;
    private boolean disposed = false;
    
    public ElementImpl( final ElementType type,
                        final Property parent,
                        final Resource resource )
    {
        this.type = type;
        this.parent = parent;
        this.resource = resource;
        
        final SortedSetFactory<Property> propertiesSetFactory = SortedSetFactory.start( PROPERTY_INSTANCE_COMPARATOR );
        final MapFactory<String,Property> propertiesByNameMapFactory = MapFactory.start();
        
        for( PropertyDef property : this.type.properties() )
        {
            final Property instance;
            
            if( property instanceof ValueProperty )
            {
                final ValueProperty p = (ValueProperty) property;
                
                if( property.hasAnnotation( Reference.class ) )
                {
                    instance = new ReferenceValue<Object,Object>( this, p );
                }
                else
                {
                    instance = new Value<Object>( this, p );
                }
            }
            else if( property instanceof TransientProperty )
            {
                instance = new Transient<Object>( this, (TransientProperty) property );
            }
            else if( property instanceof ElementProperty )
            {
                instance = new ElementHandle<Element>( this, (ElementProperty) property );
            }
            else if( property instanceof ListProperty )
            {
                instance = new ElementList<Element>( this, (ListProperty) property );
            }
            else
            {
                throw new IllegalStateException();
            }
            
            propertiesSetFactory.add( instance );
            propertiesByNameMapFactory.add( property.name().toLowerCase(), instance );
        }
        
        this.properties = propertiesSetFactory.result();
        this.propertiesByName = propertiesByNameMapFactory.result();
        
        resource.init( this );
        
        for( Listener listener : this.type.listeners() )
        {
            attach( listener );
        }
    }
    
    public final Resource resource()
    {
        return this.resource;
    }
    
    public final Element root()
    {
        if( this.parent == null )
        {
            return this;
        }
        
        return this.parent.root();
    }
    
    public final Property parent()
    {
        return this.parent;
    }
    
    @Override
    
    public final boolean holds( final Element element )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( Property p = element.parent(); p != null; p = p.element().parent() )
        {
            if( this == p.element() )
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    
    public final boolean holds( final Property property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( Property p = property; p != null; p = p.element().parent() )
        {
            if( this == p.element() )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public final <T> T nearest( final Class<T> type )
    {
        if( type.isAssignableFrom( getClass() ) )
        {
            return type.cast( this );
        }
        else
        {
            if( this.parent != null )
            {
                return this.parent.element().nearest( type );
            }
            else
            {
                return null;
            }
        }
    }
    
    public ElementType type()
    {
        return this.type;
    }
    
    @SuppressWarnings( "unchecked" )
    
    public final <T extends Element> T initialize()
    {
        for( Property instance : properties() ) 
        {
            final PropertyDef property = instance.definition();
            
            if( property instanceof ValueProperty ) 
            {
                final InitialValueService initialValueService = instance.service( InitialValueService.class );
                
                if( initialValueService != null ) 
                {
                    ( (Value<?>) instance ).write( initialValueService.value() );
                }
            }
            else if( property instanceof ImpliedElementProperty )
            {
                property( ( (ImpliedElementProperty) property ) ).content().initialize();
            }
        }
        
        return (T) this;
    }
    
    public final SortedSet<Property> properties()
    {
        synchronized( root() )
        {
            assertNotDisposed();

            return this.properties;
        }
    }
    
    public final Set<Property> properties( final String path )
    {
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            return properties( new ModelPath( path ) );
        }
    }
    
    public final Set<Property> properties( final ModelPath path )
    {
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
            
            final SetFactory<Property> properties = SetFactory.start();
            
            visit
            (
                path,
                new PropertyVisitor()
                {
                    @Override
                    public boolean visit( final Property property )
                    {
                        properties.add( property );
                        return true;
                    }
                }
            );
            
            return properties.result();
        }
    }

    public final Property property( final String path )
    {
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
            
            Property property = this.propertiesByName.get( path.toLowerCase() );
            
            if( property == null )
            {
                property = property( new ModelPath( path ) );
            }
            
            return property;
        }
    }
    
    public final Property property( final ModelPath path )
    {
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();
            
            final MutableReference<Property> result = new MutableReference<Property>();

            visit
            (
                path,
                new PropertyVisitor()
                {
                    @Override
                    public boolean visit( final Property property )
                    {
                        result.set( property );
                        return false;
                    }
                }
            );
            
            return result.get();
        }
    }
    
    public final Property property( final PropertyDef property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            final Property instance = property( property.name() );
            
            if( instance == null )
            {
                throw new IllegalArgumentException();
            }
            
            return instance;
        }
    }
    
    @SuppressWarnings( "unchecked" )
    public final <T> Value<T> property( final ValueProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            return (Value<T>) property( (PropertyDef) property );
        }
    }

    @SuppressWarnings( "unchecked" )
    public final <T> Transient<T> property( final TransientProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            return (Transient<T>) property( (PropertyDef) property );
        }
    }

    @SuppressWarnings( "unchecked" )
    public final <T extends Element> ElementHandle<T> property( final ElementProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            return (ElementHandle<T>) property( (PropertyDef) property );
        }
    }

    @SuppressWarnings( "unchecked" )
    public final <T extends Element> ElementList<T> property( final ListProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            return (ElementList<T>) property( (PropertyDef) property );
        }
    }
    
    public final SortedSet<Property> content()
    {
        synchronized( root() )
        {
            assertNotDisposed();
            
            final SortedSetFactory<Property> contentSetFactory = SortedSetFactory.start( PROPERTY_INSTANCE_COMPARATOR );
            
            for( final Property property : this.properties )
            {
                if( ! property.empty() )
                {
                    contentSetFactory.add( property );
                }
            }

            return contentSetFactory.result();
        }
    }
    
    public final boolean visit( final String path, final PropertyVisitor visitor )
    {
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( visitor == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            return visit( new ModelPath( path ), visitor );
        }
    }
    
    public final boolean visit( final ModelPath path, final PropertyVisitor visitor )
    {
        if( path == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( visitor == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            assertNotDisposed();

            final ModelPath.Segment head = path.head();
            
            if( path.length() == 1 )
            {
                if( head instanceof PropertySegment )
                {
                    final String name = ( (PropertySegment) head ).getPropertyName();
                    final Property property = this.propertiesByName.get( name.toLowerCase() );
                    
                    if( property != null )
                    {
                        return visitor.visit( property );
                    }
                    
                    return true;
                }
                else if( head instanceof AllDescendentsSegment )
                {
                    for( Property property : properties() )
                    {
                        if( ! visitor.visit( property ) )
                        {
                            return false;
                        }
                        
                        if( property instanceof ElementHandle )
                        {
                            final Element element = ( (ElementHandle<?>) property ).content();
                            
                            if( element != null )
                            {
                                if( ! element.visit( path, visitor ) )
                                {
                                    return false;
                                }
                            }
                        }
                        else if( property instanceof ElementList )
                        {
                            for( Element element : (ElementList<?>) property )
                            {
                                if( ! element.visit( path, visitor ) )
                                {
                                    return false;
                                }
                            }
                        }
                    }
                    
                    return true;
                }
            }
            else
            {
                if( head instanceof ModelRootSegment )
                {
                    return root().visit( path.tail(), visitor );
                }
                else if( head instanceof ParentElementSegment )
                {
                    final Property parent = parent();
                    
                    if( parent != null )
                    {
                        return parent.element().visit( path.tail(), visitor );
                    }
                    
                    return true;
                }
                else if( head instanceof PropertySegment )
                {
                    final String name = ( (PropertySegment) head ).getPropertyName();
                    final Property property = this.propertiesByName.get( name.toLowerCase() );
                    final ModelPath tail = path.tail();
                    
                    if( property instanceof ElementHandle )
                    {
                        final Element element = ( (ElementHandle<?>) property ).content();
                        
                        if( element != null )
                        {
                            return element.visit( tail, visitor );
                        }
                    }
                    else if( property instanceof ElementList )
                    {
                        for( Element element : (ElementList<?>) property )
                        {
                            if( ! element.visit( tail, visitor ) )
                            {
                                return false;
                            }
                        }
                    }
                    
                    return true;
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
                        return visit( path.tail(), visitor );
                    }
                    
                    return true;
                }
                else if( head instanceof AllSiblingsSegment )
                {
                    final Property parent = parent();
                    
                    if( parent instanceof ElementList )
                    {
                        final ModelPath p = ( new ModelPath( parent.name() ) ).append( path.tail() );
                        return parent.element().visit( p, visitor );
                    }
                }
            }

            throw new IllegalArgumentException( path.toString() );
        }
    }
    
    public final void refresh()
    {
        synchronized( root() )
        {
            assertNotDisposed();
            
            for( Property property : properties() )
            {
                // The second disposed check is to catch the case where refreshing one property
                // triggers a listener that causes this element to be disposed.
                
                if( disposed() )
                {
                    break;
                }
                
                property.refresh();
            }
        }
    }
    
    public final boolean empty()
    {
        synchronized( root() )
        {
            assertNotDisposed();
            
            for( final Property property : this.properties )
            {
                if( ! property.empty() )
                {
                    return false;
                }
            }

            return true;
        }
    }
    
    public final void clear()
    {
        assertNotDisposed();
        
        for( Property property : properties() )
        {
            property.clear();
        }
    }

    public final void copy( final Element source )
    {
        assertNotDisposed();

        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( final Property property : properties() )
        {
            if( ! property.definition().isReadOnly() )
            {
                property.copy( source );
            }
        }
    }

    public final void copy( final ElementData source )
    {
        assertNotDisposed();

        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        for( final Property property : properties() )
        {
            if( ! property.definition().isReadOnly() )
            {
                property.copy( source );
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
            else if( obj instanceof Element && ! disposed() )
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

    public final <S extends Service> List<S> services( final Class<S> type )
    {
        assertNotDisposed();

        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        synchronized( root() )
        {
            if( this.elementServiceContext == null )
            {
                this.elementServiceContext = new ElementInstanceServiceContext( this );
            }
            
            return this.elementServiceContext.services( type );
        }
    }
    
    public final Status validation()
    {
        synchronized( root() )
        {
            assertNotDisposed();
            
            final Status.CompositeStatusFactory factory = Status.factoryForComposite();
            
            for( Property property : properties() )
            {
                if( property.enabled() )
                {
                    factory.merge( property.validation() );
                    
                    if( property instanceof ElementHandle )
                    {
                        final Element child = ( (ElementHandle<?>) property ).content();
                        
                        if( child != null )
                        {
                            factory.merge( child.validation() );
                        }
                    }
                    else if( property instanceof ElementList )
                    {
                        for( Element child : (ElementList<?>) property )
                        {
                            factory.merge( child.validation() );
                        }
                    }
                }
            }
            
            return factory.create();
        }
    }
    
    public JobQueue<EventDeliveryJob> queue()
    {
        return ( (ElementImpl) root() ).listeners( true ).queue();
    }
    
    private ListenerContext listeners( final boolean createIfNecessary )
    {
        final Element root = root();
        
        synchronized( root )
        {
            if( this.listeners == null && createIfNecessary )
            {
                assertNotDisposed();
                
                this.listeners = new ListenerContext( this == root ? null : ( (ElementImpl) root ).listeners( true ).queue() );
            }
            
            return this.listeners;
        }
    }
    
    /**
     * Attaches a listener to this element.
     * 
     * @param listener the listener
     * @throws IllegalArgumentException if the listener is null
     * @throws IllegalStateException if this element is disposed
     */
    
    public final void attach( final Listener listener )
    {
        if( listener == null )
        {
            throw new IllegalArgumentException();
        }
        
        listeners( true ).attach( listener );
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
            root().attach( listener, path.tail() );
        }
        else if( head instanceof ParentElementSegment )
        {
            final Property parent = parent();
            
            if( parent == null )
            {
                throw createIllegalPathException( path );
            }
            
            parent.element().attach( listener, path.tail() );
        }
        else if( head instanceof AllSiblingsSegment )
        {
            final Property parent = parent();
            
            if( parent == null || ! ( parent.definition() instanceof ListProperty ) )
            {
                throw createIllegalPathException( path );
            }
            
            final ModelPath p = ( new ModelPath( parent.name() ) ).append( path.tail() );
            parent.element().attach( listener, p );
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
        else if( head instanceof AllDescendentsSegment )
        {
            for( Property property : properties() )
            {
                property.attach( listener, path );
            }
        }
        else if( head instanceof PropertySegment )
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            final Property property = property( propertyName );
            
            if( property == null )
            {
                throw createIllegalPathException( path );
            }
            
            property.attach( listener, path.tail() );
        }
    }
    
    /**
     * Detaches a listener from this element.
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
        
        final ListenerContext listeners = listeners( false );
        
        if( listeners != null )
        {
            listeners.detach( listener );
        }
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
            root().detach( listener, path.tail() );
        }
        else if( head instanceof ParentElementSegment )
        {
            final Property parent = parent();
            
            if( parent == null )
            {
                throw createIllegalPathException( path );
            }
            
            parent.element().detach( listener, path.tail() );
        }
        else if( head instanceof AllSiblingsSegment )
        {
            final Property parent = parent();
            
            if( parent == null || ! ( parent.definition() instanceof ListProperty ) )
            {
                throw createIllegalPathException( path );
            }
            
            final ModelPath p = ( new ModelPath( parent.name() ) ).append( path.tail() );
            parent.element().detach( listener, p );
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
        else if( head instanceof AllDescendentsSegment )
        {
            for( Property property : properties() )
            {
                property.detach( listener, path );
            }
        }
        else if( head instanceof PropertySegment )
        {
            final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
            final Property property = property( propertyName );
            
            if( property == null )
            {
                throw createIllegalPathException( path );
            }
            
            property.detach( listener, path.tail() );
        }
    }
        
    protected final void post( final Event event )
    {
        if( event != null )
        {
            final ListenerContext listeners = listeners( false );
            
            if( listeners != null )
            {
                listeners.post( event );
            }
        }
    }

    protected final void broadcast()
    {
        final ListenerContext listeners = listeners( false );
        
        if( listeners != null )
        {
            listeners.broadcast();
        }
    }
    
    protected final void broadcast( final Event event )
    {
        if( event != null )
        {
            final ListenerContext listeners = listeners( false );
            
            if( listeners != null )
            {
                listeners.broadcast( event );
            }
        }
    }
    
    @Override
    public final Disposable suspend()
    {
        final JobQueue<EventDeliveryJob> queue = listeners( true ).queue();
        final Disposable suspension = queue.suspend( new SuspendFilter() );
        
        return new Disposable()
        {
            @Override
            public void dispose()
            {
                suspension.dispose();
                queue.process();
            }
        };
    }
    
    public final boolean disposed()
    {
        synchronized( root() )
        {
            return this.disposed;
        }
    }
    
    @Override
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
                
                for( Property property : this.properties )
                {
                    property.dispose();
                }
                
                try
                {
                    resource().dispose();
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
                
                this.listeners = null;
            }
        }
    }
    
    protected final void assertNotDisposed()
    {
        if( disposed() )
        {
            final String msg = elementAlreadyDisposed.format( this.type.getSimpleName() );
            throw new IllegalStateException( msg );
        }
    }
    
    private final IllegalArgumentException createIllegalPathException( final ModelPath path )
    {
        final String message = illegalPathException.format( this.type.getModelElementClass().getName(), path.toString() );
        return new IllegalArgumentException( message );
    }
    
    public <A> A adapt( final Class<A> adapterType )
    {
        assertNotDisposed();

        A result = service( MasterConversionService.class ).convert( this, adapterType );

        if( result == null )
        {
            if( this.resource != null )
            {
                result = this.resource.adapt( adapterType );
            }

            if( result == null && this.parent != null )
            {
                result = this.parent.element().adapt( adapterType );
            }
            
            if( result == null && adapterType == LocalizationService.class )
            {
                result = adapterType.cast( SourceLanguageLocalizationService.INSTANCE );
            }
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
    
    private final class SuspendFilter implements Filter<EventDeliveryJob>
    {
        @Override
        public boolean allows( final EventDeliveryJob job )
        {
            if( ! ( job.listener() instanceof NonSuspendableListener ) )
            {
                final Event event = job.event();
                
                if( event instanceof PropertyEvent )
                {
                    return ! ( ElementImpl.this.holds( ( (PropertyEvent) event ).property() ) );
                }
                else if( event instanceof ElementEvent )
                {
                    return ! ( ElementImpl.this.holds( ( (ElementEvent) event ).element() ) );
                }
            }
            
            return true;
        }
    }
    
}
