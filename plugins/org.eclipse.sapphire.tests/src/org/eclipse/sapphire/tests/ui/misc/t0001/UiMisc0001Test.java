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

package org.eclipse.sapphire.tests.ui.misc.t0001;

import static org.eclipse.sapphire.ui.util.MiscUtil.findSelectionPostDelete;

import java.util.List;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests for findSelectionPostDelete algorithm. 
 * 
 * <p>The algorithm is defined as follows:</p>
 * 
 * <ol>
 *   <li>Try to select the item following the last to-be-deleted item.</li>
 *   <li>Failing that, try to select the last item not on the to-be-deleted list.</li>
 * </ol>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class UiMisc0001Test extends SapphireTestCase
{
    @Test
    
    public void test() throws Exception
    {
        final String a = "a";
        final String b = "b";
        final String c = "c";
        final String d = "d";
        final String e = "e";
        
        final List<String> all = list( a, b, c, d, e );
        
        assertEquals( d, findSelectionPostDelete( all, list( c ) ) );
        assertEquals( e, findSelectionPostDelete( all, list( d ) ) );
        assertEquals( d, findSelectionPostDelete( all, list( e ) ) );
        
        assertEquals( e, findSelectionPostDelete( all, list( b, d ) ) );
        assertEquals( d, findSelectionPostDelete( all, list( c, e ) ) );
        
        assertEquals( a, findSelectionPostDelete( all, list( e, d, c, b ) ) );
        assertEquals( b, findSelectionPostDelete( all, list( a, c, d, e ) ) );
        assertEquals( null, findSelectionPostDelete( all, list( a, b, c, d, e ) ) );
    }

}
