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

package org.eclipse.sapphire.modeling.xml;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class RootElementController
{
    public static final String XMLNS = XmlUtil.XMLNS;
    public static final String XMLNS_COLON = XmlUtil.XMLNS_COLON;
    public static final String XSI_NAMESPACE_PREFIX = XmlUtil.XSI_NAMESPACE_PREFIX;
    public static final String XSI_NAMESPACE = XmlUtil.XSI_NAMESPACE;
    public static final String XSI_SCHEMA_LOCATION_ATTR = XmlUtil.XSI_SCHEMA_LOCATION_ATTR_QUALIFIED;
    
    private XmlResource resource;
    
    public void init( final XmlResource resource )
    {
        this.resource = resource;
    }
    
    public final XmlResource resource()
    {
        return this.resource;
    }
    
    public abstract void createRootElement();
    public abstract boolean checkRootElement();
    
}
