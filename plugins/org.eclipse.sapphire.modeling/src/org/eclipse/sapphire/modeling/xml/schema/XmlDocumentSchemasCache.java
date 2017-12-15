/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.xml.schema;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class XmlDocumentSchemasCache
{
    private static final Map<String,SoftReference<XmlDocumentSchema>> cache 
        = new HashMap<String,SoftReference<XmlDocumentSchema>>();

    public static XmlDocumentSchema getSchema( final String schemaLocation )
    {
        return getSchema( schemaLocation, null );
    }

    public static XmlDocumentSchema getSchema( final String schemaLocation, 
                                               final String baseLocation )
    {
        XmlDocumentSchema schema = null;
        
        synchronized( XmlDocumentSchemasCache.class )
        {
            final SoftReference<XmlDocumentSchema> ref = cache.get( schemaLocation );
            
            if( ref != null )
            {
                schema = ref.get();
                
                if( schema == null )
                {
                    cache.remove( schemaLocation );
                }
            }
        }
        
        if( schema == null )
        {
            schema = new XmlDocumentSchema( schemaLocation, baseLocation );
            
            synchronized( XmlDocumentSchemasCache.class )
            {
                final SoftReference<XmlDocumentSchema> ref = new SoftReference<XmlDocumentSchema>( schema );
                cache.put( schemaLocation, ref );
            }
        }
        
        return schema;
    }
    
}