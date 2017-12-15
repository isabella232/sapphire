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

package org.eclipse.sapphire.modeling.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.sapphire.modeling.ExtensionsLocator;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementService;
import org.eclipse.sapphire.modeling.ModelElementServiceFactory;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelPropertyServiceFactory;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.TypeCast;
import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireModelingExtensionSystem
{
    private static final String EL_VALUE_SERIALIZATION_SERVICE = "value-serialization-service";
    private static final String EL_MODEL_ELEMENT_SERVICE = "model-element-service";
    private static final String EL_MODEL_PROPERTY_SERVICE = "model-property-service";
    private static final String EL_FUNCTION = "function";
    private static final String EL_NAME = "name";
    private static final String EL_TYPE = "type";
    private static final String EL_FACTORY = "factory";
    private static final String EL_IMPL = "impl";
    private static final String EL_ID = "id";
    private static final String EL_OVERRIDES = "overrides";
    private static final String EL_SOURCE = "source";
    private static final String EL_TARGET = "target";
    private static final String EL_TYPE_CAST = "type-cast";

    private static boolean initialized = false;
    private static List<ModelElementServiceFactoryProxy> modelElementServiceFactories;
    private static List<ModelPropertyServiceFactoryProxy> modelPropertyServiceFactories;
    private static List<ValueSerializationServiceFactory> valueSerializerFactories;
    private static Map<String,FunctionFactory> functionFactories;
    private static List<TypeCast> typeCasts;

    public static List<ModelElementServiceFactoryProxy> getModelElementServices()
    {
        initialize();
        return modelElementServiceFactories;
    }

    public static List<ModelPropertyServiceFactoryProxy> getModelPropertyServices()
    {
        initialize();
        return modelPropertyServiceFactories;
    }

    public static ValueSerializationService createValueSerializer( final IModelElement element,
                                                                   final ValueProperty property,
                                                                   final Class<?> type )
    {
        initialize();

        for( ValueSerializationServiceFactory factory : valueSerializerFactories )
        {
            if( factory.applicable( type ) )
            {
                final ValueSerializationService instance = factory.create( element, property );
                
                if( instance != null )
                {
                    return instance;
                }
            }
        }

        return null;
    }

    public static Function createFunction( final String name,
                                           final Function... operands )
    {
        initialize();

        final FunctionFactory factory = functionFactories.get( name.toLowerCase() );

        if( factory != null )
        {
            return factory.create( operands );
        }

        return null;
    }
    
    public static List<TypeCast> getTypeCasts()
    {
        initialize();
        return typeCasts;
    }

    private static synchronized void initialize()
    {
        if( ! initialized )
        {
            initialized = true;
            modelElementServiceFactories = new ArrayList<ModelElementServiceFactoryProxy>();
            modelPropertyServiceFactories = new ArrayList<ModelPropertyServiceFactoryProxy>();
            valueSerializerFactories = new ArrayList<ValueSerializationServiceFactory>();
            functionFactories = new HashMap<String,FunctionFactory>();
            typeCasts = new ArrayList<TypeCast>();

            for( final ExtensionsLocator.Handle handle : ExtensionsLocator.instance().find() )
            {
                final Element root = parse( handle.extension() );

                if( root != null )
                {
                    final NodeList nodes = root.getChildNodes();

                    for( int i = 0, n = nodes.getLength(); i < n; i++ )
                    {
                        final Node node = nodes.item( i );

                        if( node instanceof Element )
                        {
                            final Element el = (Element) node;
                            final String elname = el.getLocalName();

                            try
                            {
                                if( elname.equals( EL_VALUE_SERIALIZATION_SERVICE ) )
                                {
                                    final Class<?> valueType = handle.findClass( text( child( el, EL_TYPE ) ) );
                                    final Class<? extends ValueSerializationService> serializerClass = handle.findClass( text( child( el, EL_IMPL ) ) );

                                    valueSerializerFactories.add( new ValueSerializationServiceFactory( valueType, serializerClass ) );
                                }
                                else if( elname.equals( EL_MODEL_ELEMENT_SERVICE ) )
                                {
                                    final String id = text( child( el, EL_ID ) );
                                    final Class<? extends ModelElementService> serviceType = handle.findClass( text( child( el, EL_TYPE ) ) );
                                    final Class<? extends ModelElementServiceFactory> serviceFactory = handle.findClass( text( child( el, EL_FACTORY ) ) );
                                    final Set<String> overrides = parseOverrides( el );

                                    modelElementServiceFactories.add( new ModelElementServiceFactoryProxy( id, serviceType, serviceFactory, overrides ) );
                                }
                                else if( elname.equals( EL_MODEL_PROPERTY_SERVICE ) )
                                {
                                    final String id = text( child( el, EL_ID ) );
                                    final Class<? extends ModelPropertyService> serviceType = handle.findClass( text( child( el, EL_TYPE ) ) );
                                    final Class<? extends ModelPropertyServiceFactory> serviceFactory = handle.findClass( text( child( el, EL_FACTORY ) ) );
                                    final Set<String> overrides = parseOverrides( el );

                                    modelPropertyServiceFactories.add( new ModelPropertyServiceFactoryProxy( id, serviceType, serviceFactory, overrides ) );
                                }
                                else if( elname.equals( EL_FUNCTION ) )
                                {
                                    final String name = text( child( el, EL_NAME ) );
                                    final Class<? extends Function> impl = handle.findClass( text( child( el, EL_IMPL ) ) );

                                    functionFactories.put( name.toLowerCase(), new FunctionFactory( impl ) );
                                }
                                else if( elname.equals( EL_TYPE_CAST ) )
                                {
                                    final Class<?> source = handle.findClass( text( child( el, EL_SOURCE ) ) );
                                    final Class<?> target = handle.findClass( text( child( el, EL_TARGET ) ) );
                                    final Class<? extends TypeCast> impl = handle.findClass( text( child( el, EL_IMPL ) ) );
                                    
                                    typeCasts.add( new TypeCastProxy( source, target, impl ) );
                                }
                            }
                            catch( InvalidExtensionException e ) {}
                        }
                    }
                }
            }
        }
    }
    
    private static Set<String> parseOverrides( final Element root )
    {
        Set<String> overrides = null;
        final NodeList nodes = root.getChildNodes();

        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );

            if( node instanceof Element && node.getLocalName().equals( EL_OVERRIDES ) )
            {
                final String text = text( (Element) node );
                
                if( text.length() > 0 )
                {
                    if( overrides == null )
                    {
                        overrides = new HashSet<String>();
                    }
                    
                    overrides.add( text );
                }
            }
        }
        
        if( overrides == null )
        {
            overrides = Collections.emptySet();
        }
        
        return overrides;
    }

    private static Element parse( final URL url )
    {
        try
        {
            final InputStream in = url.openStream();

            try
            {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

                factory.setValidating( false );
                factory.setNamespaceAware( true );
                factory.setIgnoringComments( false );

                final DocumentBuilder docbuilder = factory.newDocumentBuilder();

                docbuilder.setEntityResolver
                (
                    new EntityResolver()
                    {
                        public InputSource resolveEntity( final String publicID,
                                                          final String systemID )
                        {
                            return new InputSource( new StringReader( "" ) );
                        }
                    }
                );

                final Document document = docbuilder.parse( in );

                return document.getDocumentElement();
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch( IOException e ) {}
            }
        }
        catch( Exception e )
        {
            // TODO: Log the problem.
            return null;
        }
    }

    private static Element child( final Element element,
                                  final String name )
    {
        final NodeList nodes = element.getChildNodes();

        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );

            if( node instanceof Element )
            {
                final Element el = (Element) node;

                if( name.equals( el.getLocalName() ) )
                {
                    return el;
                }
            }
        }

        // TODO: Log the problem.
        throw new InvalidExtensionException();
    }

    private static String text( final Element element )
    {
        final StringBuilder buf = new StringBuilder();
        final NodeList nodes = element.getChildNodes();

        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );

            if( node instanceof Text )
            {
                buf.append( ( (Text) node ).getData() );
            }
        }

        return buf.toString().trim();
    }

    public static final class InvalidExtensionException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    public static final class ModelElementServiceFactoryProxy

        extends ModelElementServiceFactory

    {
        private final String id;
        private final Class<? extends ModelElementService> type;
        private final Class<? extends ModelElementServiceFactory> factoryClass;
        private final Set<String> overrides;
        private ModelElementServiceFactory factoryInstance;
        private boolean factoryInstantiationFailed;

        public ModelElementServiceFactoryProxy( final String id,
                                                final Class<? extends ModelElementService> type,
                                                final Class<? extends ModelElementServiceFactory> factoryClass,
                                                final Set<String> overrides )
        {
            this.id = id;
            this.type = type;
            this.factoryClass = factoryClass;
            this.overrides = overrides;
        }
        
        public String id()
        {
            return this.id;
        }

        @Override
        public boolean applicable( final IModelElement element,
                                   final Class<? extends ModelElementService> service )
        {
            boolean result = false;

            if( service.isAssignableFrom( this.type ) )
            {
                final ModelElementServiceFactory factory = factory();

                if( factory != null )
                {
                    result = factory.applicable( element, service );
                }
            }

            return result;
        }
        
        public Set<String> overrides()
        {
            return this.overrides;
        }

        @Override
        public ModelElementService create( final IModelElement element,
                                           final Class<? extends ModelElementService> service )
        {
            ModelElementService result = null;
            final ModelElementServiceFactory factory = factory();

            if( factory != null )
            {
                result = factory.create( element, service );
            }

            return result;
        }

        private synchronized ModelElementServiceFactory factory()
        {
            if( this.factoryInstance == null && ! this.factoryInstantiationFailed )
            {
                try
                {
                    this.factoryInstance = this.factoryClass.newInstance();
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                    this.factoryInstantiationFailed = true;
                }
            }

            return this.factoryInstance;
        }
    }

    public static final class ModelPropertyServiceFactoryProxy

        extends ModelPropertyServiceFactory

    {
        private final String id;
        private final Class<? extends ModelPropertyService> type;
        private final Class<? extends ModelPropertyServiceFactory> factoryClass;
        private final Set<String> overrides;
        private ModelPropertyServiceFactory factoryInstance;
        private boolean factoryInstantiationFailed;

        public ModelPropertyServiceFactoryProxy( final String id,
                                                 final Class<? extends ModelPropertyService> type,
                                                 final Class<? extends ModelPropertyServiceFactory> factoryClass,
                                                 final Set<String> overrides )
        {
            this.id = id;
            this.type = type;
            this.factoryClass = factoryClass;
            this.overrides = overrides;
        }
        
        public String id()
        {
            return this.id;
        }

        @Override
        public boolean applicable( final IModelElement element,
                                   final ModelProperty property,
                                   final Class<? extends ModelPropertyService> service )
        {
            boolean result = false;

            if( service.isAssignableFrom( this.type ) )
            {
                final ModelPropertyServiceFactory factory = factory();

                if( factory != null )
                {
                    result = factory.applicable( element, property, service );
                }
            }

            return result;
        }

        public Set<String> overrides()
        {
            return this.overrides;
        }

        @Override
        public ModelPropertyService create( final IModelElement element,
                                            final ModelProperty property,
                                            final Class<? extends ModelPropertyService> service )
        {
            ModelPropertyService result = null;
            final ModelPropertyServiceFactory factory = factory();

            if( factory != null )
            {
                result = factory.create( element, property, service );
            }

            return result;
        }

        private synchronized ModelPropertyServiceFactory factory()
        {
            if( this.factoryInstance == null && ! this.factoryInstantiationFailed )
            {
                try
                {
                    this.factoryInstance = this.factoryClass.newInstance();
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                    this.factoryInstantiationFailed = true;
                }
            }

            return this.factoryInstance;
        }
    }

    private static final class ValueSerializationServiceFactory
    {
        private final Class<?> type;
        private final Class<? extends ValueSerializationService> serializerClass;
        private boolean serializerInstantiationFailed;

        public ValueSerializationServiceFactory( final Class<?> type,
                                                 final Class<? extends ValueSerializationService> serializerClass )
        {
            this.type = type;
            this.serializerClass = serializerClass;
        }

        public boolean applicable( final Class<?> type )
        {
            boolean result = false;

            if( this.type.isAssignableFrom( type ) )
            {
                result = true;
            }

            return result;
        }

        public ValueSerializationService create( final IModelElement element,
                                                 final ValueProperty property )
        {
            ValueSerializationService serializer = null;

            if( ! this.serializerInstantiationFailed )
            {
                try
                {
                    serializer = this.serializerClass.newInstance();
                    serializer.init( element, property, new String[ 0 ] );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                    serializer = null;
                    this.serializerInstantiationFailed = true;
                }
            }

            return serializer;
        }
    }

    private static final class FunctionFactory
    {
        private final Class<? extends Function> functionClass;
        private boolean functionInstantiationFailed;

        public FunctionFactory( final Class<? extends Function> functionClass )
        {
            this.functionClass = functionClass;
        }

        public Function create( final Function... operands )
        {
            Function function = null;

            if( ! this.functionInstantiationFailed )
            {
                try
                {
                    function = this.functionClass.newInstance();
                    function.init( operands );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                    function = null;
                    this.functionInstantiationFailed = true;
                }
            }

            return function;
        }
    }
    
    private static final class TypeCastProxy extends TypeCast
    {
        private final Class<?> source;
        private final Class<?> target;
        private final Class<? extends TypeCast> implClass;
        private TypeCast implInstance;
        private boolean implInstantiationFailed;
        
        public TypeCastProxy( final Class<?> source,
                              final Class<?> target,
                              final Class<? extends TypeCast> implementation )
        {
            this.source = source;
            this.target = target;
            this.implClass = implementation;
        }
        
        @Override
        public boolean applicable( final FunctionContext context,
                                   final Function requestor,
                                   final Object value,
                                   final Class<?> target )
        {
            if( ! this.implInstantiationFailed )
            {
                if( target == this.target && ( value == null || value.getClass() == this.source ) )
                {
                    if( this.implInstance == null )
                    {
                        try
                        {
                            this.implInstance = this.implClass.newInstance();
                        }
                        catch( Exception e )
                        {
                            LoggingService.log( e );
                            this.implInstantiationFailed = true;
                        }
                    }
                    
                    if( ! this.implInstantiationFailed )
                    {
                        return this.implInstance.applicable( context, requestor, value, target );
                    }
                }
            }
            
            return false;
        }

        @Override
        public Object evaluate( final FunctionContext context,
                                final Function requestor,
                                final Object value,
                                final Class<?> target )
        {
            if( this.implInstance == null || this.implInstantiationFailed )
            {
                throw new IllegalStateException();
            }
            
            return this.implInstance.evaluate( context, requestor, value, target );
        }
    }

}
