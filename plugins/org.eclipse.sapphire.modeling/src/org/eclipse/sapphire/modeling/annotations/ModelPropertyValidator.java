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

package org.eclipse.sapphire.modeling.annotations;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelPropertyValidator<T>
{
    public void init( final String[] params )
    {
    }
    
    public abstract IStatus validate( final T obj );
    
    protected final IStatus createErrorStatus( final String message )
    {
        return createErrorStatus( message, 0 );
    }

    protected final IStatus createErrorStatus( final String message,
                                               final int code )
    {
        return new Status( Status.ERROR, SapphireModelingFrameworkPlugin.PLUGIN_ID, code, message, null );
    }

    protected final IStatus createWarningStatus( final String message )
    {
        return new Status( Status.WARNING, SapphireModelingFrameworkPlugin.PLUGIN_ID, 0, message, null );
    }
}
