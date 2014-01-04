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

package org.eclipse.sapphire.tests.ui.def.loader;

import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.osgi.BundleBasedContext;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Tests for DefinitionLoader.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DefinitionLoaderTests extends SapphireTestCase
{
    @Test
    
    public void testCacheInClassContext()
    {
        final DefinitionLoader.Reference<DialogDef> h1 = DefinitionLoader.context( getClass() ).sdef( "TestDefinition" ).dialog();
        
        try
        {
            final DefinitionLoader.Reference<DialogDef> h2 = DefinitionLoader.context( getClass() ).sdef( "TestDefinition" ).dialog();

            try
            {
                assertSame( h1.resolve(), h2.resolve() );
            }
            finally
            {
                h2.dispose();
            }
        }
        finally
        {
            h1.dispose();
        }
    }
    
    @Test
    
    public void testCacheInClassLoaderContext()
    {
        final Class<?> cl = getClass();
        final ClassLoader cldr = cl.getClassLoader();
        final String pkg = cl.getPackage().getName();
        
        final DefinitionLoader.Reference<DialogDef> h1 = DefinitionLoader.context( cldr ).sdef( pkg + ".TestDefinition" ).dialog();
        
        try
        {
            final DefinitionLoader.Reference<DialogDef> h2 = DefinitionLoader.context( cldr ).sdef( pkg + ".TestDefinition" ).dialog();

            try
            {
                assertSame( h1.resolve(), h2.resolve() );
            }
            finally
            {
                h2.dispose();
            }
        }
        finally
        {
            h1.dispose();
        }
    }
    
    @Test
    
    public void testCacheInBundleContext()
    {
        final Bundle bundle = Platform.getBundle( "org.eclipse.sapphire.tests" );
        final Class<?> cl = getClass();
        final String pkg = cl.getPackage().getName();
        
        final DefinitionLoader.Reference<DialogDef> h1 = DefinitionLoader.context( BundleBasedContext.adapt( bundle ) ).sdef( pkg + ".TestDefinition" ).dialog();
        
        try
        {
            final DefinitionLoader.Reference<DialogDef> h2 = DefinitionLoader.context( BundleBasedContext.adapt( bundle ) ).sdef( pkg + ".TestDefinition" ).dialog();

            try
            {
                assertSame( h1.resolve(), h2.resolve() );
            }
            finally
            {
                h2.dispose();
            }
        }
        finally
        {
            h1.dispose();
        }
    }

}
