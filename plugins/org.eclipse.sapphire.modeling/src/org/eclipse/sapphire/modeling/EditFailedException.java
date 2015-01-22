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

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class EditFailedException

    extends RuntimeException
    
{
    private static final long serialVersionUID = 1L;
    
    public static EditFailedException findAsCause( final Throwable e )
    {
        if( e instanceof EditFailedException )
        {
            return (EditFailedException) e;
        }
        else
        {
            final Throwable cause = e.getCause();
            
            if( cause != null )
            {
                return findAsCause( cause );
            }
            else
            {
                return null;
            }
        }
    }
    
}
