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

package org.eclipse.sapphire.tests.modeling.misc.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests the annotation processor's case-insensitivity when looking for property getter and
 * setter methods.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Misc0003Test

    extends SapphireTestCase
    
{
    private Misc0003Test( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Misc0003" );

        suite.addTest( new Misc0003Test( "test" ) );
        
        return suite;
    }
    
    public void test() throws Exception
    {
        final IMisc0003TestRootElement element = IMisc0003TestRootElement.TYPE.instantiate();
        
        // String Value Property
        
        element.sEtVaLuEpRoPeRtY1( "abc" );
        assertEquals( "abc", element.getvalueproperty1().getText() );
        
        // Integer Value Property
        
        element.sEtVaLuEpRoPeRtY2( "1" );
        assertEquals( Integer.valueOf( 1 ), element.GETVALUEPROPERTY2().getContent() );
        
        element.SeTvAlUePrOpErTy2( 2 );
        assertEquals( Integer.valueOf( 2 ), element.GETVALUEPROPERTY2().getContent() );

        // List Property
        
        element.gEtLiStPrOpErTy().addNewElement();
        assertEquals( 1, element.gEtLiStPrOpErTy().size() );
        
        // Explicit Element Property
        
        element.gEtElEmEnTpRoPeRtY().element( true );
        assertNotNull( element.gEtElEmEnTpRoPeRtY().element() );
        
        // Implied Element Property
        
        element.GETIMPLIEDELEMENTPROPERTY().setText( "xyz" );
        assertEquals( "xyz", element.GETIMPLIEDELEMENTPROPERTY().getText().getText() );
        
        // Transient Property
        
        element.SetTrAnSiEnTpRoPeRtY( this );
        assertSame( this, element.gettransientproperty().content() );
    }

}
