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

package org.eclipse.sapphire.modeling.xml.internal;

import org.eclipse.sapphire.ConversionService;
import org.eclipse.sapphire.ElementImpl;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResource;

/**
 * ConversionService implementation for ModelElement to XmlElement conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ModelElementToXmlElementConversionService extends ConversionService<ElementImpl,XmlElement>
{
    public ModelElementToXmlElementConversionService()
    {
        super( ElementImpl.class, XmlElement.class );
    }

    @Override
    public XmlElement convert( final ElementImpl element )
    {
        final Resource resource = element.resource();
        
        if( resource instanceof XmlResource )
        {
            return ( (XmlResource) resource ).getXmlElement();
        }
        
        return null;
    }
    
}
