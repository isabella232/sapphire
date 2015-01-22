/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [337232] Certain schema causes elements to be out of order in corresponding xml files
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToEmptyString;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class XmlElementDefinition extends XmlContentModel
{
    private QName elementName;
    private QName contentModelName;
    private boolean isAbstract;
    private QName substitutionGroup;
    private List<XmlElementDefinition> substitutionList;
    
    protected XmlElementDefinition( final XmlDocumentSchema schema,
                                    final QName elementName,
                                    final QName contentModelName,
                                    final int minOccur,
                                    final int maxOccur )
    {
        this( schema, elementName, contentModelName, minOccur, maxOccur, false, null );
    }
    
    protected XmlElementDefinition( final XmlDocumentSchema schema,
            final QName elementName,
            final QName contentModelName,
            final int minOccur,
            final int maxOccur,
            final boolean isAbstract, 
            final QName substitutionGroup )
    {
        super( schema, minOccur, maxOccur );
        
        this.elementName = elementName;
        this.contentModelName = contentModelName;
        this.isAbstract = isAbstract;
        this.substitutionGroup = substitutionGroup;
    }

    public QName getName()
    {
        return this.elementName;
    }
    
    public QName getContentModelName()
    {
        return this.contentModelName;
    }
    
    public boolean isAbstract() {
        return this.isAbstract;
    }

    public QName getSubstitutionGroup() {
        return this.substitutionGroup;
    }

    public List<XmlElementDefinition> getSubstitutionList() {
        return this.substitutionList;
    }

    public void setSubstitutionList(List<XmlElementDefinition> substitutionList) {
        this.substitutionList = substitutionList;
    }

    public XmlContentModel getContentModel()
    {
        final QName contentModelName = getContentModelName();
        
        if( contentModelName == null )
        {
            return null;
        }
        else
        {
            final String namespace = contentModelName.getNamespaceURI();
            final String localName = contentModelName.getLocalPart();
            
            if( namespace.equals( normalizeToEmptyString( this.schema.getNamespace() ) ) )
            {
                return this.schema.getContentModel( localName );
            }
            else
            {
                final String importedSchemaLocation = this.schema.getSchemaLocation( namespace );
                final XmlDocumentSchema importedSchema = XmlDocumentSchemasCache.getSchema( importedSchemaLocation );
                return importedSchema.getContentModel( localName );
            }
        }
    }
    
    @Override
    public XmlContentModel findChildElementContentModel( final QName childElementName )
    {
        if( this.elementName.equals( childElementName ) )
        {
            return getContentModel();
        }
        
        return null;
    }
    
    @Override
    protected InsertionPosition findInsertionPosition( final NodeList nodeList,
                                                       final int nodeListLength,
                                                       final QName qname,
                                                       final Position position )
    {
        int elementsConsumed = 0;
        
        while( position.listIndex < nodeListLength )
        {
            final Node node = nodeList.item( position.listIndex );
            
            if( node.getNodeType() != Node.ELEMENT_NODE )
            {
                position.listIndex++;
            }
            else
            {
                final QName eln = new QName( node.getNamespaceURI(), node.getLocalName() );
                
                if ( sameElementName( eln ) )
                {
                    elementsConsumed++;
                    position.listIndex++;
                }
                else
                {
                    break;
                }
            }
        }
        
        final InsertionPosition result = new InsertionPosition();

        if( sameElementName( qname ) )
        {
            result.listIndex = position.listIndex;
            
            final int elementCountAfterAdd = elementsConsumed + 1;
            
            if( this.maxOccur != -1 && elementCountAfterAdd > this.maxOccur )
            {
                result.grade = InsertionPosition.G1_EXCEEDS_MAX_OCCUR;
            }
            else if( elementCountAfterAdd <= this.minOccur )
            {
                result.grade = InsertionPosition.G4_MEETS_OCCUR_REQUIREMENT;
            }
            else
            {
                result.grade = InsertionPosition.G3_OK_TO_INSERT;
            }
        }
        
        return result;
    }
    
    protected boolean sameElementName(QName qname) {
        if (this.elementName.equals( qname )) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void toString( final StringBuilder buf,
                             final String indent )
    {
        buf.append( indent );
        buf.append( "element [" ); //$NON-NLS-1$
        buf.append( this.minOccur );
        buf.append( ':' );
        buf.append( this.maxOccur );
        buf.append( "]\n" ); //$NON-NLS-1$
        
        buf.append( indent );
        buf.append( "{\n" ); //$NON-NLS-1$
        
        buf.append( indent );
        buf.append( "    name = " ); //$NON-NLS-1$
        buf.append( this.elementName );
        buf.append( '\n' );

        final QName contentModelName = getContentModelName();
        
        if( contentModelName != null )
        {
            buf.append( indent );
            buf.append( "    type = " ); //$NON-NLS-1$
            buf.append( contentModelName );
            buf.append( '\n' );
        }
        
        if ( isAbstract() ) 
        {
            buf.append( indent );
            buf.append( "    abstract=\"true\" "); //$NON-NLS-1$
            buf.append( '\n' );
        }
        
        if ( getSubstitutionGroup() != null )
        {
            buf.append( indent );
            buf.append( "    SubstitutionGroup = " ); //$NON-NLS-1$
            buf.append( getSubstitutionGroup() );
            buf.append( '\n' );
        }

        buf.append( indent );
        buf.append( '}' );
    }
    
    public static class Factory extends XmlContentModel.Factory
    {
        protected QName elementName;
        private QName contentModelName;
        private boolean isAbstract = false;
        private QName substitutionGroup = null;
        
        public final QName getName()
        {
            return this.elementName;
        }

        public final void setName( final QName elementName )
        {
            this.elementName = elementName;
        }
        
        public final void setName( final String elementName )
        {
            this.elementName = new QName( elementName );
        }
        
        public QName getContentModelName()
        {
            return this.contentModelName;
        }
        
        public void setContentModelName( final QName contentModelName )
        {
            this.contentModelName = contentModelName;
        }
        
        public void setContentModelName( final String contentModelName )
        {
            this.contentModelName = new QName( contentModelName );
        }

        public final boolean isAbstract() {
            return this.isAbstract;
        }

        public final void setAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
        }

        public final QName getSubstitutionGroup() {
            return this.substitutionGroup;
        }

        public final void setSubstitutionGroup(QName substitutionGroup) {
            this.substitutionGroup = substitutionGroup;
        }

        @Override
        public XmlContentModel create( final XmlDocumentSchema schema )
        {
            return new XmlElementDefinition( schema, this.elementName, this.contentModelName, this.minOccur, this.maxOccur, this.isAbstract, this.substitutionGroup );
        }
    }
    
}

