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

package org.eclipse.sapphire.tests.conversion.enumeration;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public enum ThreeChoiceAnswerToString
{
    YES( "1" ),
    MAYBE( "0" ),
    NO( "-1" );
    
    private final String string;
    
    private ThreeChoiceAnswerToString( final String string )
    {
        this.string = string;
    }
    
    @Override
    public String toString()
    {
        return this.string;
    }
    
}
