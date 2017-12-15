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

package org.eclipse.sapphire.modeling.xml;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class RootElementController
{
    public static final String XMLNS = "xmlns"; //$NON-NLS-1$
    public static final String XMLNS_COLON = XMLNS + ":"; //$NON-NLS-1$
    public static final String XSI_NAMESPACE_PREFIX = "xsi"; //$NON-NLS-1$
    public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
    public static final String XSI_SCHEMA_LOCATION_ATTR = "xsi:schemaLocation"; //$NON-NLS-1$
    
    protected ModelStoreForXml modelStore;
    protected ModelElementType rootModelElementType;
    
    public void init( final ModelStoreForXml modelStore,
                      final ModelElementType rootModelElementType )
    {
        this.modelStore = modelStore;
        this.rootModelElementType = rootModelElementType;
    }
    
    public abstract void createRootElement( final Document document );
    public abstract boolean checkRootElement( final Document document );
}
