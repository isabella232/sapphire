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

package org.eclipse.sapphire.tests.modeling.el.functions.part;

import java.util.Collections;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.eclipse.sapphire.ui.PartFunctionContext;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.DialogPart;
import org.junit.Test;

/**
 * Tests Part function.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PartFunctionTests extends TestExpr
{
    @Test
    
    public void testPartFunctionInPartContext()
    {
        final Element element = Element.TYPE.instantiate();
        
        try
        {
            final DefinitionLoader.Reference<DialogDef> definition = DefinitionLoader.sdef( PartFunctionTests.class ).dialog();
            final SapphirePart part = new DialogPart();
            
            try
            {
                part.init( null, element, definition.resolve(), Collections.<String,String>emptyMap() );
                part.initialize();
                
                testForExpectedValue( new PartFunctionContext( part, element ), "${ Part }", part );
            }
            finally
            {
                part.dispose();
            }
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test

    public void testPartFunctionInWrongContext()
    {
        testForExpectedValue( "${ Part }", null );
    }

}
