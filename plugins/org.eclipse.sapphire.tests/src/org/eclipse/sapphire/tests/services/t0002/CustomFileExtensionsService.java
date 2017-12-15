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

package org.eclipse.sapphire.tests.services.t0002;

import java.util.List;

import org.eclipse.sapphire.services.FileExtensionsService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CustomFileExtensionsService extends FileExtensionsService
{
    @Override
    protected void compute( final List<String> extensions )
    {
        extensions.add( "avi" );
        extensions.add( "mpeg" );
    }
    
}
