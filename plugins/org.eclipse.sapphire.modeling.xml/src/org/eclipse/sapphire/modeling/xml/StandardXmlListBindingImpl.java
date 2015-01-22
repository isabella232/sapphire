/******************************************************************************
 * Copyright (c) 2015 Oracle and Red Hat
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Rob Cernich - [360371] Allow subclasses to override default element naming in XML list binding
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.util.MiscUtil.indexOf;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.contains;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.createQualifiedName;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.equal;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.LayeredListPropertyBinding;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:rcernich@redhat.com">Rob Cernich</a>
 */

public class StandardXmlListBindingImpl extends LayeredListPropertyBinding
{
    @Text( "{0}.{1} : {2}" )
    private static LocalizableText failure;
    
    @Text( "Element name must be specified in @XmlListBinding.Mapping annotation." )
    private static LocalizableText mustSpecifyElementNameMsg; 
    
    static
    {
        LocalizableText.init( StandardXmlListBindingImpl.class );
    }

    private PossibleTypesService possibleTypesService;
    private Listener possibleTypesServiceListener;
    protected XmlPath path;
    protected QName[] xmlElementNames;
    protected ElementType[] modelElementTypes;

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
                try
                {
                    initBindingMetadata();
                }
                catch( Exception e )
                {
                    final String msg = failure.format( property.element().type().getSimpleName(), property.name(), e.getMessage() );
                    Sapphire.service( LoggingService.class ).logError( msg );
                }
            }
        };
        
        this.possibleTypesService.attach( this.possibleTypesServiceListener );
        
        try
        {
            initBindingMetadata();
        }
        catch( Exception e )
        {
            final String msg = failure.format( property.element().type().getSimpleName(), property.name(), e.getMessage() );
            throw new RuntimeException( msg, e );
        }
    }
    
    protected void initBindingMetadata()
    {
        final XmlListBinding annotation = property().definition().getAnnotation( XmlListBinding.class );
        final XmlNamespaceResolver xmlNamespaceResolver = ( (XmlResource) property().element().resource() ).getXmlNamespaceResolver();
        
        final SortedSet<ElementType> possible = this.possibleTypesService.types();
        this.modelElementTypes = possible.toArray( new ElementType[ possible.size() ] );

        if( annotation == null )
        {
            this.path = new XmlPath( property().name(), xmlNamespaceResolver );
            
            this.xmlElementNames = new QName[ this.modelElementTypes.length ];
            
            for( int i = 0; i < this.modelElementTypes.length; i++ )
            {
                this.xmlElementNames[ i ] = createDefaultElementName( this.modelElementTypes[ i ], xmlNamespaceResolver );
            }
        }
        else
        {
            if( annotation.path().length() > 0 )
            {
                this.path = new XmlPath( annotation.path(), xmlNamespaceResolver );
            }
            
            final XmlListBinding.Mapping[] mappings = annotation.mappings();
            this.xmlElementNames = new QName[ this.modelElementTypes.length ];
            
            for( int i = 0; i < this.modelElementTypes.length; i++ )
            {
                final ElementType type = this.modelElementTypes[ i ];
                        
                for( XmlListBinding.Mapping mapping : mappings )
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
    protected Resource resource( final Object obj )
    {
        final XmlElement xmlElement = (XmlElement) obj;
        final XmlResource parentXmlResource = (XmlResource) property().element().resource();
        
        return new ChildXmlResource( parentXmlResource, xmlElement );
    }

    @Override
    protected List<?> readUnderlyingList()
    {
        final XmlElement parent = getXmlElement( false );
        
        if( parent == null )
        {
            return Collections.emptyList();
        }
        else
        {
            final ListFactory<XmlElement> list = ListFactory.start();
            
            for( XmlElement element : parent.getChildElements() )
            {
                final QName xmlElementName = createQualifiedName( element.getDomNode() );
                
                if( contains( this.xmlElementNames, xmlElementName, xmlElementName.getNamespaceURI() ) )
                {
                    list.add( element );
                }
            }
            
            return list.result();
        }
    }

    @Override
    protected Object insertUnderlyingObject( final ElementType type,
                                             final int position )
    {
        final XmlElement parent = getXmlElement( true );
        QName xmlElementName = this.xmlElementNames[ indexOf( this.modelElementTypes, type ) ];
        
        if( xmlElementName.getNamespaceURI().equals( "" ) )
        {
            xmlElementName = new QName( parent.getNamespace(), xmlElementName.getLocalPart() );
        }
        
        final List<?> list = readUnderlyingList();
        final XmlElement refXmlElement = (XmlElement) ( position < list.size() ? list.get( position ) : null );
        
        return parent.addChildElement( xmlElementName, refXmlElement );
    }
    
    @Override
    public void move( final Resource resource, 
                      final int position )
    {
        final List<?> list = readUnderlyingList();
        final XmlElement xmlElement = ( (ChildXmlResource) resource ).getXmlElement();
        final XmlElement refXmlElement = (XmlElement) ( position < list.size() ? list.get( position ) : null );
        
        xmlElement.move( refXmlElement );
    }

    @Override
    public void remove( final Resource resource )
    {
        final XmlResource xmlResource = (XmlResource) resource;
        final XmlElement xmlElement = xmlResource.getXmlElement();
        
        xmlElement.remove();
        
        if( this.path != null )
        {
            final XmlElement base = getBaseXmlElement( false );
            
            if( base != null )
            {
                final XmlElement parent = (XmlElement) base.getChildNode( this.path, false );
                
                if( parent != null && parent.isEmpty() )
                {
                    base.removeChildNode( this.path );
                }
            }
        }
    }

    protected XmlElement getXmlElement( final boolean createIfNecessary )
    {
        XmlElement parent = getBaseXmlElement( createIfNecessary );
        
        if( parent != null && this.path != null )
        {
            parent = (XmlElement) parent.getChildNode( this.path, createIfNecessary );
        }
        
        return parent;
    }
    
    protected XmlElement getBaseXmlElement( final boolean createIfNecessary )
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
