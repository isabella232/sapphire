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

package org.eclipse.sapphire.tests.concurrency.service;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests service lookup while in presence of concurrent model access that utilizes the same service context. Of particular
 * note is the case where service initialization involves model access. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ServiceLookupConcurrencyTest extends SapphireTestCase
{
    @Test
    
    public void testPropertyServiceLookup()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        final Thread t1 = new Thread()
        {
            @Override
            public void run()
            {
                element.getValue().services( TestService.class );
            }
        };
        
        final Thread t2 = new Thread()
        {
            @Override
            public void run()
            {
                for( int i = 0; i < 1000000; i++ )
                {
                    // The refresh method is used here because it involves accessing property
                    // instance service context.
                    
                    element.getValue().refresh();
                }
            }
        };
        
        t1.start();
        t2.start();
        
        while( t1.isAlive() || t2.isAlive() )
        {
            if( t1.getState() == Thread.State.BLOCKED && t2.getState() == Thread.State.BLOCKED )
            {
                fail( "Deadlock!" );
            }
            
            try
            {
                Thread.sleep( 100 );
            }
            catch( InterruptedException e ) {}
        }
    }
    
    @Test

    public void testElementServiceLookup()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        final Thread t1 = new Thread()
        {
            @Override
            public void run()
            {
                element.services( TestService.class );
            }
        };
        
        final Thread t2 = new Thread()
        {
            @Override
            public void run()
            {
                final TestElement other = TestElement.TYPE.instantiate();
                
                for( int i = 0; i < 1000000; i++ )
                {
                    // The equals method is used here because it involves accessing element
                    // instance service context.
                    
                    element.equals( other );
                }
            }
        };
        
        t1.start();
        t2.start();
        
        while( t1.isAlive() || t2.isAlive() )
        {
            if( t1.getState() == Thread.State.BLOCKED && t2.getState() == Thread.State.BLOCKED )
            {
                fail( "Deadlock!" );
            }
            
            try
            {
                Thread.sleep( 100 );
            }
            catch( InterruptedException e ) {}
        }
    }

}
