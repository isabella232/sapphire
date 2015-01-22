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

package org.eclipse.sapphire.modeling.xml.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlSequenceGroup extends XmlGroupContentModel
{
    XmlSequenceGroup( final XmlDocumentSchema schema,
                      final int minOccur,
                      final int maxOccur,
                      final List<XmlContentModel> list )
    {
        super( schema, minOccur, maxOccur, list );
    }
    
    @Override
    protected InsertionPosition findInsertionPosition( final NodeList nodeList,
                                                       final int nodeListLength,
                                                       final QName qname,
                                                       final Position position )
    {
        final InsertionPosition result = new InsertionPosition();
        
        for( int i = 0; ; i++ )
        {
            final int startingPosition = position.listIndex;
            final InsertionPosition localResult = new InsertionPosition();
            
            for( XmlContentModel entry : getNestedContent() )
            {
                localResult.merge( entry.findInsertionPosition( nodeList, nodeListLength, qname, position ) );
            }
            
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
        buf.append( "sequence [" ); //$NON-NLS-1$
        buf.append( this.minOccur );
        buf.append( ':' );
        buf.append( this.maxOccur );
        buf.append( "]\n" ); //$NON-NLS-1$
        
        buf.append( indent );
        buf.append( "{\n" ); //$NON-NLS-1$
        
        final String nextLevelIndent = indent + "    "; //$NON-NLS-1$
        
        for( XmlContentModel childContentModel : getNestedContent() )
        {
            childContentModel.toString( buf, nextLevelIndent );
            buf.append( '\n' );
        }
        
        buf.append( indent );
        buf.append( '}' );
    }

    public static final class Factory extends XmlGroupContentModel.Factory
    {
        @Override
        public XmlContentModel create( final XmlDocumentSchema schema )
        {
            final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>();
            
            for( XmlContentModel.Factory f : this.nestedContent )
            {
                nestedContent.add( f.create( schema ) );
            }
            
            return new XmlSequenceGroup( schema, this.minOccur, this.maxOccur, nestedContent );
        }
    }
    
}

