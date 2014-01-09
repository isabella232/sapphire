/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class XmlAllGroup extends XmlGroupContentModel
{
    XmlAllGroup( final XmlDocumentSchema schema,
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
        return new InsertionPosition();
    }
    
    @Override
    protected void toString( final StringBuilder buf,
                             final String indent )
    {
        buf.append( indent );
        buf.append( "all [" ); //$NON-NLS-1$
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
            
            return new XmlAllGroup( schema, this.minOccur, this.maxOccur, nestedContent );
        }
    }
    
}

