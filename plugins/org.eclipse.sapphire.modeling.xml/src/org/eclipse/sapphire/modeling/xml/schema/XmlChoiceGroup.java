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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlChoiceGroup extends XmlGroupContentModel
{
    XmlChoiceGroup( final XmlDocumentSchema schema,
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
        
        if( position.listIndex < nodeListLength )
        {
            for( int i = 0; ; i++ )
            {
                while( position.listIndex < nodeListLength && 
                       nodeList.item( position.listIndex ).getNodeType() != Node.ELEMENT_NODE )
                {
                    position.listIndex++;
                }
                 
                // Try the different branches. Pick the one that makes the most progress.
                
                final int startingPosition = position.listIndex;

                int maxProgress = 0;
                InsertionPosition maxProgressResult = new InsertionPosition();
                
                for( XmlContentModel entry : getNestedContent() )
                {
                    position.listIndex = startingPosition;
                    
                    final InsertionPosition branchResult
                        = entry.findInsertionPosition( nodeList, nodeListLength, qname, position );
                    
                    final int branchProgress = position.listIndex - startingPosition;
                    
                    if( branchProgress > maxProgress || ( maxProgress == 0 && branchResult.grade > 0 ) )
                    {
                        maxProgress = branchProgress;
                        maxProgressResult = branchResult;
                    }
                }
                
                position.listIndex = startingPosition + maxProgress;
                
                if( position.listIndex == startingPosition )
                {
                    i++;
                    
                    if( this.maxOccur != -1 && i > this.maxOccur )
                    {
                        maxProgressResult.grade = InsertionPosition.G1_EXCEEDS_MAX_OCCUR;
                    }
                    else if( i <= this.minOccur )
                    {
                        maxProgressResult.grade = InsertionPosition.G4_MEETS_OCCUR_REQUIREMENT;
                    }
                    else
                    {
                        maxProgressResult.grade = InsertionPosition.G2_OK_TO_INSERT;
                    }
                    
                    result.merge( maxProgressResult );
                    
                    break;
                }
                else
                {
                    result.merge( maxProgressResult );
                }
            }
        }
        
        return result;
    }
    
    @Override
    protected void toString( final StringBuilder buf,
                             final String indent )
    {
        buf.append( indent );
        buf.append( "choice [" ); //$NON-NLS-1$
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
            
            return new XmlChoiceGroup( schema, this.minOccur, this.maxOccur, nestedContent );
        }
    }
    
}

