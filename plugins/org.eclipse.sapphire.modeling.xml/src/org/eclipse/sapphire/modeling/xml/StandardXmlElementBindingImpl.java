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

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.util.MiscUtil.indexOf;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.contains;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.createQualifiedName;
import static org.eclipse.sapphire.modeling.xml.XmlUtil.equal;

import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LayeredElementBindingImpl;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.services.PossibleTypesService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardXmlElementBindingImpl extends LayeredElementBindingImpl
{
    private PossibleTypesService possibleTypesService;
    private Listener possibleTypesServiceListener;
    private XmlPath path;
    private QName[] xmlElementNames;
    private ModelElementType[] modelElementTypes;
    
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
                initBindingMetadata();
            }
        };
        
        this.possibleTypesService.attach( this.possibleTypesServiceListener );
        
        initBindingMetadata();
    }
    
    private void initBindingMetadata()
    {
        final IModelElement element = element();
        final ModelProperty property = property();
        
        try
        {
            final XmlElementBinding xmlElementBindingAnnotation = property.getAnnotation( XmlElementBinding.class );
            final XmlNamespaceResolver xmlNamespaceResolver = ( (XmlResource) element.resource() ).getXmlNamespaceResolver();
            
            final SortedSet<ModelElementType> possible = this.possibleTypesService.types();
            this.modelElementTypes = possible.toArray( new ModelElementType[ possible.size() ] );

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
                    this.path = new XmlPath( property.getName(), ( (XmlResource) element.resource() ).getXmlNamespaceResolver() );
                    
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
                    final ModelElementType type = this.modelElementTypes[ i ];
                            
                    for( XmlElementBinding.Mapping mapping : mappings )
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
        catch( Exception e )
        {
            final String msg = NLS.bind( Resources.failure, element.getModelElementType().getSimpleName(), property.getName(), e.getMessage() );
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
    protected Object readUnderlyingObject()
    {
        XmlElement parent = ( (XmlResource) element().resource() ).getXmlElement( false );
        
        if( parent != null )
        {
            if( this.path != null )
            {
                parent = (XmlElement) parent.getChildNode( this.path, false );
            }
            
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
        }
        
        return null;
    }

    @Override
    protected Object createUnderlyingObject( final ModelElementType type )
    {
        XmlElement parent = ( (XmlResource) element().resource() ).getXmlElement( true );
        
        if( this.path != null )
        {
            parent = (XmlElement) parent.getChildNode( this.path, true );
        }
        
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
        final XmlResource parentXmlResource = (XmlResource) element().resource();
        
        return new ChildXmlResource( parentXmlResource, xmlElement );
    }
    
    @Override
    public void remove()
    {
        XmlElement base = ( (XmlResource) element().resource() ).getXmlElement( false );
        
        if( base != null )
        {
            XmlElement parent = base;
            
            if( this.path != null )
            {
                parent = (XmlElement) parent.getChildNode( this.path, false );
            }
            
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

    @Override
    public boolean removable()
    {
        return true;
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
            initializeMessages( StandardXmlElementBindingImpl.class.getName(), Resources.class );
        }
    }
    
}
