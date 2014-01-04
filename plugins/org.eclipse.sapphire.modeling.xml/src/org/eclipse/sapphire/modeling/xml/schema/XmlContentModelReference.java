/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import static org.eclipse.sapphire.modeling.util.MiscUtil.normalizeToEmptyString;

import javax.xml.namespace.QName;

import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlContentModelReference extends XmlContentModel
{
    private QName contentModelName;
    
    protected XmlContentModelReference( final XmlDocumentSchema schema,
                                        final QName contentModelName,
                                        final int minOccur,
                                        final int maxOccur )
    {
        super( schema, minOccur, maxOccur );

        this.contentModelName = contentModelName;
    }
    
    public QName getContentModelName()
    {
        return this.contentModelName;
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
        final XmlContentModel contentModel = getContentModel();
        
        if( contentModel != null )
        {
            return contentModel.findChildElementContentModel( childElementName );
        }
        
        return null;
    }
    
    @Override
    protected InsertionPosition findInsertionPosition( final NodeList nodeList,
                                                       final int nodeListLength,
                                                       final QName qname,
                                                       final Position position )
    {
        final InsertionPosition result = new InsertionPosition();
        final XmlContentModel contentModel = getContentModel();
        
        if( contentModel == null )
        {
            return result;
        }
        
        for( int i = 0; ; i++ )
        {
            final int startingPosition = position.listIndex;

            final InsertionPosition localResult 
                = contentModel.findInsertionPosition( nodeList, nodeListLength, qname, position );
            
            if( position.listIndex == startingPosition )
            {
                i++;
                
                if( this.maxOccur != -1 && i > this.maxOccur )
                {
                    localResult.grade = InsertionPosition.G1_EXCEEDS_MAX_OCCUR;
                }
                else if( i <= this.minOccur )
                {
                    localResult.grade = InsertionPosition.G4_MEETS_OCCUR_REQUIREMENT;
                }
                else
                {
                    localResult.grade = InsertionPosition.G2_OK_TO_INSERT;
                }
                
                result.merge( localResult );
                
                break;
            }
            else
            {
                result.merge( localResult );
            }
        }
        
        return result;
    }
    
    @Override
    protected void toString( final StringBuilder buf,
                             final String indent )
    {
        buf.append( indent );
        buf.append( "content-model-ref [" ); //$NON-NLS-1$
        buf.append( this.minOccur );
        buf.append( ':' );
        buf.append( this.maxOccur );
        buf.append( "]\n" ); //$NON-NLS-1$
        
        buf.append( indent );
        buf.append( "{\n" ); //$NON-NLS-1$
        
        buf.append( indent );
        buf.append( "    ref = " ); //$NON-NLS-1$
        buf.append( this.contentModelName );
        buf.append( '\n' );

        buf.append( indent );
        buf.append( '}' );
    }
    
    public static final class Factory extends XmlContentModel.Factory
    {
        private QName contentModelName;
        
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

        @Override
        public XmlContentModel create( final XmlDocumentSchema schema )
        {
            return new XmlContentModelReference( schema, this.contentModelName, this.minOccur, this.maxOccur );
        }
    }
    
}

