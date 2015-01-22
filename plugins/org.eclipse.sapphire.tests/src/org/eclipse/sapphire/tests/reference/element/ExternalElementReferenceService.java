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

package org.eclipse.sapphire.tests.reference.element;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementReferenceService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ExternalElementReferenceService extends ElementReferenceService
{
    private ElementList<?> list;
    
    @Override
    public ElementList<?> list()
    {
        return this.list;
    }
    
    public void list( final ElementList<?> list )
    {
        this.list = list;
        
        broadcast( new ListEvent() );
    }

    @Override
    public String key()
    {
        return "Name";
    }

}
