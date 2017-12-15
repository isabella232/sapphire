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

import org.eclipse.core.resources.IFile;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsEditorPageDef;

/**
 * Tests class resolution via import statements in the Sapphire UI Definition model.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestUiDef0001

    extends SapphireTestCase
    
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
        final ISapphireUiDef sdef = SapphireUiDefFactory.load( "org.eclipse.sapphire.tests", "org/eclipse/sapphire/tests/ui/def/t0001/TestDefinition.sdef" );

        final IMasterDetailsEditorPageDef page = (IMasterDetailsEditorPageDef) sdef.getPartDef( "TestPage", false, IMasterDetailsEditorPageDef.class );
        assertNotNull( page );
        
        final ModelElementList<ActionHandlerDef> handlers = page.getActionHandlers();
        assertNotNull( handlers );
        assertEquals( 3, handlers.size() );
        
        JavaType type;
        Class<?> cl;
        
        type = handlers.get( 0 ).getImplClass().resolve();
        assertNotNull( type );
        cl = type.artifact();
        assertNotNull( cl );
        assertEquals( IFile.class, cl );

        type = handlers.get( 1 ).getImplClass().resolve();
        assertNotNull( type );
        cl = type.artifact();
        assertNotNull( cl );
        assertEquals( XmlBinding.class, cl );

        type = handlers.get( 2 ).getImplClass().resolve();
        assertNotNull( type );
        cl = type.artifact();
        assertNotNull( cl );
        assertEquals( TestActionHandler.class, cl );
    }

}
