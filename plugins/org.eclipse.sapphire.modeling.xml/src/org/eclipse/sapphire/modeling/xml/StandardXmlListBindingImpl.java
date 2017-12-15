/******************************************************************************
 * Copyright (c) 2012 Oracle and Red Hat
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LayeredListBindingImpl;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:rcernich@redhat.com">Rob Cernich</a>
 */

public class StandardXmlListBindingImpl extends LayeredListBindingImpl
{
    private PossibleTypesService possibleTypesService;
    private Listener possibleTypesServiceListener;
    protected XmlPath path;
    protected QName[] xmlElementNames;
    protected ModelElementType[] modelElementTypes;

    @Override
    public void init( final IModelElement element,
                      final ModelProperty property,
                      final String[] params )
    {
        super.init( element, property, params );
        
        this.possibleTypesService = element.service( property, PossibleTypesService.class );
        
        this.possibleTypesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                try
                {
                    initBindingMetadata( element, property, params );
                }
                catch( Exception e )
                {
                    final String msg = NLS.bind( Resources.failure, element.getModelElementType().getSimpleName(), property.getName(), e.getMessage() );
                    LoggingService.log( Status.createErrorStatus( msg ) );
                }
            }
        };
        
        this.possibleTypesService.attach( this.possibleTypesServiceListener );
        
        try
        {
            initBindingMetadata( element, property, params );
        }
        catch( Exception e )
        {
            final String msg = NLS.bind( Resources.failure, element.getModelElementType().getSimpleName(), property.getName(), e.getMessage() );
            throw new RuntimeException( msg, e );
        }
    }
    
    protected void initBindingMetadata( final IModelElement element,
                                        final ModelProperty property,
                                        final String[] params )
    {
        final XmlListBinding annotation = property.getAnnotation( XmlListBinding.class );
        final XmlNamespaceResolver xmlNamespaceResolver = ( (XmlResource) element.resource() ).getXmlNamespaceResolver();
        
        final SortedSet<ModelElementType> possible = this.possibleTypesService.types();
        this.modelElementTypes = possible.toArray( new ModelElementType[ possible.size() ] );

        if( annotation == null )
        {
            this.path = new XmlPath( property.getName(), xmlNamespaceResolver );
            
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
                final ModelElementType type = this.modelElementTypes[ i ];
                        
                for( XmlListBinding.Mapping mapping : mappings )
                {
                    if( mapping.type() == type.getModelElementClass() )
                    {
                        final String mappingElementName = mapping.element().trim();
                        
                        if( mappingElementName.length() == 0 )
                        {
                            throw new RuntimeException( Resources.mustSpecifyElementNameMsg );
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
    
    protected QName createDefaultElementName( final ModelElementType type, 
                                              final XmlNamespaceResolver xmlNamespaceResolver )
    {
        return XmlUtil.createDefaultElementName( type );
    }

    @Override
    public ModelElementType type( final Resource resource )
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
    protected List<?> readUnderlyingList()
    {
        final XmlElement parent = getXmlElement( false );
        
        if( parent == null )
        {
            return Collections.emptyList();
        }
        else
        {
            final List<XmlElement> list = new ArrayList<XmlElement>();
            
            for( XmlElement element : parent.getChildElements() )
            {
                final QName xmlElementName = createQualifiedName( element.getDomNode() );
                
                if( contains( this.xmlElementNames, xmlElementName, xmlElementName.getNamespaceURI() ) )
                {
                    list.add( element );
                }
            }
            
            return list;
        }
    }

    @Override
    protected Object addUnderlyingObject( final ModelElementType type )
    {
        final XmlElement parent = getXmlElement( true );
        QName xmlElementName = this.xmlElementNames[ indexOf( this.modelElementTypes, type ) ];
        
        if( xmlElementName.getNamespaceURI().equals( "" ) )
        {
            xmlElementName = new QName( parent.getNamespace(), xmlElementName.getLocalPart() );
        }
        
        return parent.addChildElement( xmlElementName );
    }

    @Override
    protected Resource createResource( final Object obj )
    {
        final XmlElement xmlElement = (XmlElement) obj;
        final XmlResource parentXmlResource = (XmlResource) element().resource();
        
        return new ChildXmlResource( parentXmlResource, xmlElement );
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

    @Override
    public void swap( final Resource a, 
                      final Resource b )
    {
        final XmlElement x = ( (XmlResource) a ).getXmlElement();
        final XmlElement y = ( (XmlResource) b ).getXmlElement();
        
        x.swap( y );
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
        final XmlResource resource = (XmlResource) element().resource();
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

    private static final class Resources extends NLS
    {
        public static String failure;
        public static String mustSpecifyElementNameMsg; 
        
        static
        {
            initializeMessages( StandardXmlListBindingImpl.class.getName(), Resources.class );
        }
    }
    
}
