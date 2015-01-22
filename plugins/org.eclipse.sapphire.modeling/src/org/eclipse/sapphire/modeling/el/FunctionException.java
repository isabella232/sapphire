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

package org.eclipse.sapphire.modeling.el;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;

/**
 * Thrown if a function evaluation fails for any reason.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class FunctionException extends RuntimeException
{
    @Text( "Unexpected exception encountered while executing expression language function." )
    private static LocalizableText unexpectedException;
    
    static
    {
        LocalizableText.init( FunctionException.class );
    }

    private static final long serialVersionUID = 1L;
    
    private final Status status;
    
    public FunctionException( final Status status )
    {
        super( status.message(), status.exception() );
        
        this.status = status;
    }
    
    public FunctionException( final String message )
    {
        this( Status.createErrorStatus( message ) );
    }
    
    public FunctionException( final Throwable throwable )
    {
        this( Status.createErrorStatus( unexpectedException.text(), throwable ) );
    }
    
    public Status status()
    {
        return this.status;
    }
    
}
