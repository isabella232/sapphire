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
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResource;

/**
 * ConversionService implementation for XmlResource to XmlElement conversions.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlResourceToXmlElementConversionService extends ConversionService<XmlResource,XmlElement>
{
    public XmlResourceToXmlElementConversionService()
    {
        super( XmlResource.class, XmlElement.class );
    }

    @Override
    public XmlElement convert( final XmlResource resource )
    {
        return resource.getXmlElement();
    }
    
}
