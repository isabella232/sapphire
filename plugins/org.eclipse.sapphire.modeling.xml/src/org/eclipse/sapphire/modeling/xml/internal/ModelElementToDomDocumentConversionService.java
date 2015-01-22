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

package org.eclipse.sapphire.modeling.xml.internal;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.ElementImpl;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.w3c.dom.Document;

/**
 * ConversionService implementation for ModelElement to DOM Document conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ModelElementToDomDocumentConversionService extends ConversionService<ElementImpl,Document>
{
    public ModelElementToDomDocumentConversionService()
    {
        super( ElementImpl.class, Document.class );
    }

    @Override
    public Document convert( final ElementImpl element )
    {
        final Resource resource = element.resource();
        
        if( resource instanceof XmlResource )
        {
            return resource.adapt( RootXmlResource.class ).getDomDocument();
        }
        
        return null;
    }
    
}
