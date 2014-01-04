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

package org.eclipse.sapphire.tests.modeling.el.functions.parent.part;

import java.util.Collections;

import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.eclipse.sapphire.ui.PartFunctionContext;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.DialogPart;
import org.eclipse.sapphire.ui.forms.GroupPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.junit.Test;

/**
 * Tests Parent function for parts.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PartParentFunctionTests extends TestExpr
{
    @Test
    
    public void testPartParentFunction()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final DefinitionLoader.Reference<DialogDef> definition = DefinitionLoader.sdef( PartParentFunctionTests.class ).dialog();
            final DialogPart dialogPart = new DialogPart();
            
            try
            {
                dialogPart.init( null, element, definition.resolve(), Collections.<String,String>emptyMap() );
                dialogPart.initialize();
                
                final GroupPart groupPart = (GroupPart) dialogPart.children().all().get( 0 );
                final PropertyEditorPart propertyEditorPart = (PropertyEditorPart) groupPart.children().all().get( 0 );
                
                testForExpectedValue( new PartFunctionContext( propertyEditorPart, element ), "${ Part }", propertyEditorPart );
                testForExpectedValue( new PartFunctionContext( propertyEditorPart, element ), "${ Part.Parent }", groupPart );
                testForExpectedValue( new PartFunctionContext( propertyEditorPart, element ), "${ Part.Parent.Parent }", dialogPart );
            }
            finally
            {
                dialogPart.dispose();
            }
        }
        finally
        {
            element.dispose();
        }
    }

}
