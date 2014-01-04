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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;

/**
 * Thrown by DefinitionLoader when attempting to use a definition that has validation errors.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "serial" )

public final class InvalidDefinitionException extends RuntimeException
{
    @Text( "Sapphire user interface definition has failed validation." )
    private static LocalizableText message;
    
    static
    {
        LocalizableText.init( InvalidDefinitionException.class );
    }

    private final Status validation;
    
    public InvalidDefinitionException( final Status validation )
    {
        if( validation == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( validation.severity() != Status.Severity.ERROR )
        {
            throw new IllegalArgumentException();
        }
        
        this.validation = validation;
    }
    
    public Status validation()
    {
        return this.validation;
    }
    
    @Override
    public String toString()
    {
        return message + " " + this.validation.message();
    }
    
}
