/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.build.processor.internal.util;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public enum AccessModifier
{
    PUBLIC( "public " ),
    PROTECTED( "protected " ),
    DEFAULT( "" ),
    PRIVATE( "private " );
    
    private String syntax;
    
    AccessModifier( final String syntax )
    {
        this.syntax = syntax;
    }
    
    public void write( final IndentingPrintWriter pw )
    {
        pw.print( this.syntax );
    }
}
