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

package org.eclipse.sapphire.tests.concurrency.service;

import org.eclipse.sapphire.services.Service;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestService extends Service
{
    @Override
    protected void init()
    {
        super.init();
        
        final TestElement element = context( TestElement.class );
        
        for( int i = 0; i < 1000000; i++ )
        {
            element.getValue().content();
        }
    }
    
}
