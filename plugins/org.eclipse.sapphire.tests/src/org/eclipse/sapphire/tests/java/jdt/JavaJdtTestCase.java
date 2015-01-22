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

package org.eclipse.sapphire.tests.java.jdt;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.After;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class JavaJdtTestCase extends SapphireTestCase
{
    private final JavaJdtTestHelper helper;
    
    protected JavaJdtTestCase()
    {
        this.helper = new JavaJdtTestHelper( getClass() );
    }
    
    @After
    public void disposeTestHelper() throws Exception
    {
        this.helper.dispose();
    }

    protected final IJavaProject getJavaProject() throws Exception
    {
        return this.helper.getJavaProject();
    }
    
    protected final void writeJavaSourceFile( final String packageName,
                                              final String className,
                                              final String content )
    
        throws Exception
        
    {
        this.helper.writeJavaSourceFile( packageName, className, content );
    }
    
    protected final void create( final IFolder folder ) throws Exception
    {
        this.helper.create( folder );
    }
    
}
