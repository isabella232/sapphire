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

package org.eclipse.sapphire.tests.ui.def.t0001;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.MasterDetailsEditorPageDef;

/**
 * Tests class resolution via import statements in the Sapphire UI Definition model.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestUiDef0001 extends SapphireTestCase
{
    private TestUiDef0001( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "UiDef0001" );

        suite.addTest( new TestUiDef0001( "test" ) );
        
        return suite;
    }
    
    public void test()
    {
        final DefinitionLoader.Reference<EditorPageDef> handle = DefinitionLoader.context( getClass() ).sdef( "TestDefinition" ).page();
        
        try
        {
            final MasterDetailsEditorPageDef page = (MasterDetailsEditorPageDef) handle.resolve();
            assertNotNull( page );
            
            final ModelElementList<ActionHandlerDef> handlers = page.getActionHandlers();
            assertNotNull( handlers );
            assertEquals( 1, handlers.size() );
            
            JavaType type;
            Class<?> cl;
            
            type = handlers.get( 0 ).getImplClass().resolve();
            assertNotNull( type );
            cl = type.artifact();
            assertNotNull( cl );
            assertEquals( TestActionHandler.class, cl );
        }
        finally
        {
            handle.dispose();
        }
    }

}
