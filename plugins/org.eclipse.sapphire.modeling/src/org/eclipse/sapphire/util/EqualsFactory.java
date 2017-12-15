/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.util;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class EqualsFactory
{
    private static EqualsFactory TRUE = new TrueEqualsFactory();
    private static EqualsFactory FALSE = new FalseEqualsFactory();
    
    private EqualsFactory()
    {
    }
    
    public static EqualsFactory start()
    {
        return TRUE;
    }
    
    public abstract EqualsFactory add( Object x, Object y );
    
    public abstract boolean result();
    
    private static final class TrueEqualsFactory extends EqualsFactory
    {
        @Override
        public EqualsFactory add( final Object x,
                                  final Object y )
        {
            if( x == y )
            {
                return TRUE;
            }
            else if( x != null && y != null )
            {
                return ( x.equals( y ) ? TRUE : FALSE );
            }

            return FALSE;
        }

        @Override
        public boolean result()
        {
            return true;
        }
    }

    private static final class FalseEqualsFactory extends EqualsFactory
    {
        @Override
        public EqualsFactory add( final Object x,
                                  final Object y )
        {
            return this;
        }

        @Override
        public boolean result()
        {
            return false;
        }
    }
    
}
