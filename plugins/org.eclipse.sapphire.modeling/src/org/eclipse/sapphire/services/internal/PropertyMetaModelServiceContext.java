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

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyMetaModelServiceContext extends PropertyServiceContext
{
    public PropertyMetaModelServiceContext( final PropertyDef property )
    {
        super( ID_PROPERTY_METAMODEL, Sapphire.services(), property, null, null );
    }
    
}
