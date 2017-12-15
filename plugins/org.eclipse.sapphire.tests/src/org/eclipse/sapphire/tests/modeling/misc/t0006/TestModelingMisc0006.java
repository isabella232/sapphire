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

package org.eclipse.sapphire.tests.modeling.misc.t0006;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests ModelElementType.getImplClassName() method. 
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0006

    extends SapphireTestCase
    
{
    private TestModelingMisc0006( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "TestModelingMisc0006" );

        suite.addTest( new TestModelingMisc0006( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        test( ILevel1.class, "internal.Level1" );
        test( Level1.class, "internal.Level1Impl" );
        test( Level1.Level2.class, "internal.Level1$Level2Impl" );
        test( Level1.Level2.Level3.class, "internal.Level1$Level2$Level3Impl" );
        test( Level1.ILevel2.class, "internal.Level1$Level2" );
        test( Level1.ILevel2.ILevel3.class, "internal.Level1$ILevel2$Level3" );
        test( Level1.Level2ExplicitPackageName.class, "explicit.Level1$Level2ExplicitPackageNameImpl" );
        test( Level1.Level2ExplicitClassName.class, "internal.Level2ExpClass" );
        test( Level1.Level2ExplicitBoth.class, "explicit.Level2ExpBoth" );
    }
    
    private static void test( final Class<?> typeClass,
                              final String expected )
    {
        assertEquals( "org.eclipse.sapphire.tests.modeling.misc.t0006." + expected, ModelElementType.getImplClassName( typeClass ) );
        final ModelElementType type = ModelElementType.getModelElementType( typeClass );
        assertNotNull( type.instantiate() );
    }

}
