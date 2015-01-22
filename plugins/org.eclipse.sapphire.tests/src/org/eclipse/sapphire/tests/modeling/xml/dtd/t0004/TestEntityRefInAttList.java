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

package org.eclipse.sapphire.tests.modeling.xml.dtd.t0004;

import org.eclipse.sapphire.modeling.xml.dtd.DtdParser;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests parsing of a DTD with an entity ref in ATTLIST.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestEntityRefInAttList extends SapphireTestCase
{
    @Test
    
    public void testEntityRefInAttList() throws Exception
    {
        DtdParser.parse( loadResource( "TestCase.dtd" ) );
    }

}
