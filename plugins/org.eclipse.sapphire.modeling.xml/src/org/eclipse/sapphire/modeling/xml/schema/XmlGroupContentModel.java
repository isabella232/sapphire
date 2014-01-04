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
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class XmlGroupContentModel extends XmlContentModel
{
    private final List<XmlContentModel> nestedContent = new ArrayList<XmlContentModel>();
    private final List<XmlContentModel> nestedContentReadOnly = Collections.unmodifiableList( this.nestedContent );
    
    protected XmlGroupContentModel( final XmlDocumentSchema schema,
                                    final int minOccur,
                                    final int maxOccur,
                                    final List<XmlContentModel> list )
    {
        super( schema, minOccur, maxOccur );
        
        this.nestedContent.addAll( list );
    }
    
    public List<XmlContentModel> getNestedContent()
    {
        return this.nestedContentReadOnly;
    }
    
    @Override
    public XmlContentModel findChildElementContentModel( final QName childElementName )
    {
        for( XmlContentModel childContentModel : getNestedContent() )
        {
            final XmlContentModel res = childContentModel.findChildElementContentModel( childElementName );
            
            if( res != null )
            {
                return res;
            }
        }
        
        return null;
    }
    
    public static abstract class Factory extends XmlContentModel.Factory
    {
        protected final List<XmlContentModel.Factory> nestedContent = new ArrayList<XmlContentModel.Factory>();
        
        public final List<XmlContentModel.Factory> getNestedContent()
        {
            return Collections.unmodifiableList( this.nestedContent );
        }
        
        public final void addNestedContent( final XmlContentModel.Factory child )
        {
            if( child != null )
            {
                this.nestedContent.add( child );
            }
        }
        
        public final boolean hasNestedContent()
        {
            return ! this.nestedContent.isEmpty();
        }
    }
    
}

