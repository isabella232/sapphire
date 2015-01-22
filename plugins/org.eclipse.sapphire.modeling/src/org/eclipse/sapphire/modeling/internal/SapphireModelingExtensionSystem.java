/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.internal;

import static org.eclipse.sapphire.Result.failure;
import static org.eclipse.sapphire.Result.success;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Result;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.ExtensionsLocator;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.SetFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireModelingExtensionSystem
{
    private static final String EL_CONDITION = "condition";
    private static final String EL_CONTEXT = "context";
    private static final String EL_FUNCTION = "function";
    private static final String EL_ID = "id";
    private static final String EL_IMPL = "impl";
    private static final String EL_IMPLEMENTATION = "implementation";
    private static final String EL_NAME = "name";
    private static final String EL_OVERRIDES = "overrides";
    private static final String EL_PARAMETER = "parameter";
    private static final String EL_SERVICE = "service";
    private static final String EL_SIGNATURE = "signature";
    
    private static boolean initialized = false;
    private static List<ServiceExtension> serviceExtensions;
    private static Map<String,List<FunctionFactory>> functionFactories;

    public static List<ServiceExtension> services()
    {
        initialize();
        return serviceExtensions;
    }
    
    public static List<Function> functions( final String name, final int arity )
    {
        initialize();

        final List<FunctionFactory> factories = functionFactories.get( name.toLowerCase() );
        final ListFactory<Function> functions = ListFactory.start();

        if( factories != null )
        {
            for( FunctionFactory factory : factories )
            {
                if( factory.signature() == null || factory.signature().size() == arity )
                {
                    functions.add( factory.create() );
                }
            }
        }
        
        return functions.result();
    }
    
    private static synchronized void initialize()
    {
        if( ! initialized )
        {
            initialized = true;
            functionFactories = new HashMap<String,List<FunctionFactory>>();
            
            final ListFactory<ServiceExtension> serviceExtensionsFactory = ListFactory.start();

            for( final ExtensionsLocator.Handle handle : ExtensionsLocator.instance().find() )
            {
                final Element root = parse( handle.extension() );

                if( root != null )
                {
                    final NodeList nodes = root.getChildNodes();
                    final Context context = handle.context();

                    for( int i = 0, n = nodes.getLength(); i < n; i++ )
                    {
                        final Node node = nodes.item( i );

                        if( node instanceof Element )
                        {
                            final Element el = (Element) node;
                            final String elname = el.getLocalName();

                            try
                            {
                                if( elname.equals( EL_SERVICE ) )
                                {
                                    final String id = value( el, EL_ID ).required();
                                    final Set<String> contexts = SetFactory.unmodifiable( values( el, EL_CONTEXT ) );
                                    final Set<String> overrides = SetFactory.unmodifiable( values( el, EL_OVERRIDES ) );
                                    
                                    final Class<? extends Service> implementation = context.findClass( value( el, EL_IMPLEMENTATION ).required() );
                                    
                                    final String conditionClassName = value( el, EL_CONDITION ).optional();
                                    Class<? extends ServiceCondition> condition = null;
                                    
                                    if( conditionClassName != null )
                                    {
                                        condition = context.findClass( conditionClassName );
                                    }
                                    
                                    if( implementation != null )
                                    {
                                        serviceExtensionsFactory.add( new ServiceExtension( id, implementation, condition, contexts, overrides ) );
                                    }
                                }
                                else if( elname.equals( EL_FUNCTION ) )
                                {
                                    final String name = value( el, EL_NAME ).required().toLowerCase();
                                    final Class<? extends Function> impl = context.findClass( value( el, EL_IMPL ).required() );
                                    
                                    final Element signatureElement = element( el, EL_SIGNATURE ).optional();
                                    final List<Class<?>> signature;
                                    
                                    if( signatureElement == null )
                                    {
                                        signature = null;
                                    }
                                    else
                                    {
                                        final ListFactory<Class<?>> parameters = ListFactory.start();
                                        
                                        for( String string : values( signatureElement, EL_PARAMETER ) )
                                        {
                                            final Class<?> parameter;
                                            
                                            try
                                            {
                                                parameter = context.findClass( string );
                                            }
                                            catch( IllegalArgumentException e )
                                            {
                                                throw new InvalidExtensionException();
                                            }
                                            
                                            if( parameter == null )
                                            {
                                                throw new InvalidExtensionException();
                                            }
                                            
                                            parameters.add( parameter );
                                        }
                                        
                                        signature = parameters.result();
                                    }
                                    
                                    final ListFactory<FunctionFactory> factories = ListFactory.start();
                                    
                                    factories.add( functionFactories.get( name ) );
                                    factories.add( new FunctionFactory( impl, signature ) );
                                    
                                    functionFactories.put( name, factories.result() );
                                }
                            }
                            catch( InvalidExtensionException e ) {}
                        }
                    }
                }
            }
            
            serviceExtensions = serviceExtensionsFactory.result();
        }
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

    private static String value( final Element element )
    {
        final StringBuilder buf = new StringBuilder();
        final NodeList nodes = element.getChildNodes();
    
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );
    
            if( node instanceof org.w3c.dom.Text )
            {
                buf.append( ( (org.w3c.dom.Text) node ).getData() );
            }
        }
    
        return buf.toString().trim();
    }
    
    private static Result<String> value( final Element element, final String valueElementName )
    {
        final Element el = element( element, valueElementName ).optional();
        
        if( el != null )
        {
            return success( value( el ) );
        }
        
        return failure( new InvalidExtensionException() );
    }

    private static List<String> values( final Element root, final String entryElementName )
    {
        final ListFactory<String> factory = ListFactory.start();
        final NodeList nodes = root.getChildNodes();
    
        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );
    
            if( node instanceof Element && node.getLocalName().equals( entryElementName ) )
            {
                final String text = value( (Element) node );
                
                if( text.length() > 0 )
                {
                    factory.add( text );
                }
            }
        }
        
        return factory.result();
    }
    
    private static Result<Element> element( final Element element, final String childElementName )
    {
        final NodeList nodes = element.getChildNodes();

        for( int i = 0, n = nodes.getLength(); i < n; i++ )
        {
            final Node node = nodes.item( i );

            if( node instanceof Element )
            {
                final Element el = (Element) node;

                if( childElementName.equals( el.getLocalName() ) )
                {
                    return success( el );
                }
            }
        }
        
        return failure( new InvalidExtensionException() );
    }

    public static final class InvalidExtensionException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }
    
    public static final class ServiceExtension
    {
        private final String id;
        private final Class<? extends Service> implementation;
        private final Class<? extends ServiceCondition> condition;
        private final Set<String> contexts;
        private final Set<String> overrides;
        
        public ServiceExtension( final String id,
                                 final Class<? extends Service> implementation,
                                 final Class<? extends ServiceCondition> condition,
                                 final Set<String> contexts,
                                 final Set<String> overrides )
        {
            this.id = id;
            this.implementation = implementation;
            this.condition = condition;
            this.contexts = contexts;
            this.overrides = overrides;
        }
        
        public String id()
        {
            return this.id;
        }
        
        public Class<? extends Service> implementation()
        {
            return this.implementation;
        }
        
        public Class<? extends ServiceCondition> condition()
        {
            return this.condition;
        }
        
        public Set<String> contexts()
        {
            return this.contexts;
        }
        
        public Set<String> overrides()
        {
            return this.overrides;
        }
    }

    private static final class FunctionFactory
    {
        private final Class<? extends Function> functionClass;
        private boolean functionInstantiationFailed;
        private final List<Class<?>> signature;

        public FunctionFactory( final Class<? extends Function> functionClass,
                                final List<Class<?>> signature )
        {
            this.functionClass = functionClass;
            this.signature = signature;
        }
        
        public List<Class<?>> signature()
        {
            return this.signature;
        }

        public Function create()
        {
            Function function = null;

            if( ! this.functionInstantiationFailed )
            {
                try
                {
                    function = this.functionClass.newInstance();
                    function.initSignature( this.signature );
                }
                catch( Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                    function = null;
                    this.functionInstantiationFailed = true;
                }
            }

            return function;
        }
    }

}