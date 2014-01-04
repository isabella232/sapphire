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

package org.eclipse.sapphire.tests.modeling.misc.t0003;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests the annotation processor's case-insensitivity when looking for property getter and
 * setter methods.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestModelingMisc0003 extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final Misc0003TestRootElement element = Misc0003TestRootElement.TYPE.instantiate();
        
        // String Value Property
        
        element.sEtVaLuEpRoPeRtY1( "abc" );
        assertEquals( "abc", element.getvalueproperty1().text() );
        
        // Integer Value Property
        
        element.sEtVaLuEpRoPeRtY2( "1" );
        assertEquals( Integer.valueOf( 1 ), element.GETVALUEPROPERTY2().content() );
        
        element.SeTvAlUePrOpErTy2( 2 );
        assertEquals( Integer.valueOf( 2 ), element.GETVALUEPROPERTY2().content() );

        // List Property
        
        element.gEtLiStPrOpErTy().insert();
        assertEquals( 1, element.gEtLiStPrOpErTy().size() );
        
        // Explicit Element Property
        
        element.gEtElEmEnTpRoPeRtY().content( true );
        assertNotNull( element.gEtElEmEnTpRoPeRtY().content() );
        
        // Implied Element Property
        
        element.GETIMPLIEDELEMENTPROPERTY().setText( "xyz" );
        assertEquals( "xyz", element.GETIMPLIEDELEMENTPROPERTY().getText().text() );
        
        // Transient Property
        
        element.SetTrAnSiEnTpRoPeRtY( this );
        assertSame( this, element.gettransientproperty().content() );
    }

}
