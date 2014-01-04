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

package org.eclipse.sapphire.tests.modeling.misc.t0004;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests making an existing property read-only when extending from an another model element
 * interface.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0004 extends SapphireTestCase
{
    @Test
    
    public void testWritable() throws Exception
    {
        final Misc0004TestElementWritable element = Misc0004TestElementWritable.TYPE.instantiate();
        
        element.setText( "abc" );
        assertEquals( "abc", element.getText().content() );
        
        element.setInteger( "1" );
        assertEquals( Integer.valueOf( 1 ), element.getInteger().content() );

        element.setInteger( 2 );
        assertEquals( Integer.valueOf( 2 ), element.getInteger().content() );
    }
    
    @Test

    public void testReadOnly() throws Exception
    {
        final Misc0004TestElementReadOnly element = Misc0004TestElementReadOnly.TYPE.instantiate();

        boolean caughtExpectedException = false;
        
        try
        {
            element.setText( "abc" );
        }
        catch( UnsupportedOperationException e )
        {
            caughtExpectedException = true;
        }
        
        if( ! caughtExpectedException )
        {
            fail( "Did not catch expected UnsupportedOperationException." );
        }

        caughtExpectedException = false;
        
        try
        {
            element.setInteger( "1" );
        }
        catch( UnsupportedOperationException e )
        {
            caughtExpectedException = true;
        }
        
        if( ! caughtExpectedException )
        {
            fail( "Did not catch expected UnsupportedOperationException." );
        }

        caughtExpectedException = false;
        
        try
        {
            element.setInteger( 2 );
        }
        catch( UnsupportedOperationException e )
        {
            caughtExpectedException = true;
        }
        
        if( ! caughtExpectedException )
        {
            fail( "Did not catch expected UnsupportedOperationException." );
        }
    }

}
