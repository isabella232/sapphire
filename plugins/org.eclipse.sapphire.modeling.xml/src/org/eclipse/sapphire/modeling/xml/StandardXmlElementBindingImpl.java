/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [376531] Need ability to distinguish between switch among heterogeneous elements 
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.util.MiscUtil.indexOf;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.contains;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.createQualifiedName;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.equal;

import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.LayeredElementBindingImpl;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class StandardXmlElementBindingImpl extends LayeredElementBindingImpl
{
    @Text( "{0}.{1} : {2}" )
    private static LocalizableText failure;
    
    @Text( "Element name must be specified in @XmlElementBinding.Mapping annotation." )
    private static LocalizableText mustSpecifyElementNameMsg; 
    
    static
    {
        LocalizableText.init( StandardXmlElementBindingImpl.class );
    }

    private PossibleTypesService possibleTypesService;
    private Listener possibleTypesServiceListener;
    private XmlPath path;
    private QName[] xmlElementNames;
    private ElementType[] modelElementTypes;
    
    @Override
    public void init( final Property property )
    {
        super.init( property );
        
        this.possibleTypesService = property.service( PossibleTypesService.class );
        
        this.possibleTypesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                initBindingMetadata();
            }
        };
        
        this.possibleTypesService.attach( this.possibleTypesServiceListener );
        
        initBindingMetadata();
    }
    
    private void initBindingMetadata()
    {
        final Element element = property().element();
        final PropertyDef property = property().definition();
        
        try
        {
            final XmlElementBinding xmlElementBindingAnnotation = property.getAnnotation( XmlElementBinding.class );
            final XmlNamespaceResolver xmlNamespaceResolver = ( (XmlResource) element.resource() ).getXmlNamespaceResolver();
            
            final SortedSet<ElementType> possible = this.possibleTypesService.types();
            this.modelElementTypes = possible.toArray( new ElementType[ possible.size() ] );

            if( xmlElementBindingAnnotation == null )
            {
                final XmlBinding xmlBindingAnnotation = property.getAnnotation( XmlBinding.class );
                
                if( xmlBindingAnnotation != null && possible.size() == 1 )
                {
                    final String path = xmlBindingAnnotation.path();
                    final int slashIndex = path.lastIndexOf( '/' );
                    
                    if( slashIndex == -1 )
                    {
                        this.xmlElementNames = new QName[] { createQualifiedName( path, xmlNamespaceResolver ) };
                    }
                    else if( slashIndex > 0 && slashIndex < path.length() - 1 )
                    {
                        this.path = new XmlPath( path.substring( 0, slashIndex ), xmlNamespaceResolver );
                        this.xmlElementNames = new QName[] { createQualifiedName( path.substring( slashIndex + 1 ), xmlNamespaceResolver ) };
                    }
                }
                
                if( this.xmlElementNames == null )
                {
                    this.path = new XmlPath( property.name(), ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
                    
                    this.xmlElementNames = new QName[ this.modelElementTypes.length ];
                    
                    for( int i = 0; i < this.modelElementTypes.length; i++ )
                    {
                        this.xmlElementNames[ i ] = createDefaultElementName( this.modelElementTypes[ i ], xmlNamespaceResolver );
                    }
                }
            }
            else
            {
                if( xmlElementBindingAnnotation.path().length() > 0 )
                {
                    this.path = new XmlPath( xmlElementBindingAnnotation.path(), xmlNamespaceResolver );
                }
                
                final XmlElementBinding.Mapping[] mappings = xmlElementBindingAnnotation.mappings();
                this.xmlElementNames = new QName[ this.modelElementTypes.length ];
                
                for( int i = 0; i < this.modelElementTypes.length; i++ )
                {
                    final ElementType type = this.modelElementTypes[ i ];
                            
                    for( XmlElementBinding.Mapping mapping : mappings )
                    {
                        if( mapping.type() == type.getModelElementClass() )
                        {
                            final String mappingElementName = mapping.element().trim();
                            
                            if( mappingElementName.length() == 0 )
                            {
                                throw new RuntimeException( mustSpecifyElementNameMsg.text() );
                            }

                            this.xmlElementNames[ i ] = createQualifiedName( mappingElementName, xmlNamespaceResolver );
                            
                            break;
                        }
                    }
                    
                    if( this.xmlElementNames[ i ] == null )
                    {
                        this.xmlElementNames[ i ] = createDefaultElementName( type, xmlNamespaceResolver );
                    }
                }
            }
        }
        catch( Exception e )
        {
            final String msg = failure.format( element.type().getSimpleName(), property.name(), e.getMessage() );
            throw new RuntimeException( msg, e );
        }
    }
    
    /**
     * Creates the XML element name for a type that does not have an explicit mapping. This method can be
     * overridden to provide custom behavior.
     * 
     * @param type the model element type
     * @param xmlNamespaceResolver the resolver of XML namespace suffixes to declared namespaces
     * @return the qualified XML element name for the given model element type
     */
    
    protected QName createDefaultElementName( final ElementType type, 
                                              final XmlNamespaceResolver xmlNamespaceResolver )
    {
        return XmlUtil.createDefaultElementName( type );
    }
    
    @Override
    public ElementType type( final Resource resource )
    {
        final XmlElement xmlElement = ( (XmlResource) resource ).getXmlElement();
        final QName xmlElementName = createQualifiedName( xmlElement.getDomNode() );
        final String xmlElementNamespace = xmlElementName.getNamespaceURI();
        
        for( int i = 0; i < this.xmlElementNames.length; i++ )
        {
            if( equal( this.xmlElementNames[ i ], xmlElementName, xmlElementNamespace ) )
            {
                return this.modelElementTypes[ i ];
            }
        }
        
        throw new IllegalStateException();
    }

    @Override
    protected Object readUnderlyingObject()
    {
        final XmlElement parent = parent( false );
        
        if( parent != null )
        {
            for( XmlElement element : parent.getChildElements() )
            {
                final QName xmlElementName = createQualifiedName( element.getDomNode() );
                
                if( contains( this.xmlElementNames, xmlElementName, xmlElementName.getNamespaceURI() ) )
                {
                    return element;
                }
            }
        }
        
        return null;
    }

    @Override
    protected Object createUnderlyingObject( final ElementType type )
    {
        final XmlElement base = base( false );
        if( base != null )
        {
            final XmlElement parent = parent( false );
            
            if( parent != null )
            {
                for( XmlElement element : parent.getChildElements() )
                {
                    final QName xmlElementName = createQualifiedName( element.getDomNode() );
                    
                    if( contains( this.xmlElementNames, xmlElementName, xmlElementName.getNamespaceURI() ) )
                    {
                        element.remove();
                    }
                }
            }
        }
    	
        final XmlElement parent = parent( true );
        QName xmlElementName = this.xmlElementNames[ indexOf( this.modelElementTypes, type ) ];
        
        if( xmlElementName.getNamespaceURI().equals( "" ) )
        {
            xmlElementName = new QName( parent.getNamespace(), xmlElementName.getLocalPart() );
        }
        
        return parent.getChildElement( xmlElementName, true );
    }

    @Override
    protected Resource createResource( final Object obj )
    {
        final XmlElement xmlElement = (XmlElement) obj;
        final XmlResource parentXmlResource = (XmlResource) property().element().resource();
        
        return new ChildXmlResource( parentXmlResource, xmlElement );
    }
    
    @Override
    public void remove()
    {
        final XmlElement base = base( false );
        
        if( base != null )
        {
            final XmlElement parent = parent( false );
            
            if( parent != null )
            {
                for( XmlElement element : parent.getChildElements() )
                {
                    final QName xmlElementName = createQualifiedName( element.getDomNode() );
                    
                    if( contains( this.xmlElementNames, xmlElementName, xmlElementName.getNamespaceURI() ) )
                    {
                        element.remove();
                    }
                }
                
                if( parent != base && parent.isEmpty() )
                {
                    base.removeChildNode( this.path );
                }
            }
        }
    }

    protected XmlElement parent( final boolean createIfNecessary )
    {
        XmlElement parent = base( createIfNecessary );
        
        if( parent != null && this.path != null )
        {
            parent = (XmlElement) parent.getChildNode( this.path, createIfNecessary );
        }
        
        return parent;
    }
    
    protected XmlElement base( final boolean createIfNecessary )
    {
        final XmlResource resource = (XmlResource) property().element().resource();
        return resource.getXmlElement( createIfNecessary );
    }

    @Override
    public void dispose()
    {
        super.dispose();
        
        if( this.possibleTypesService != null )
        {
            this.possibleTypesService.detach( this.possibleTypesServiceListener );
        }
    }
    
}
