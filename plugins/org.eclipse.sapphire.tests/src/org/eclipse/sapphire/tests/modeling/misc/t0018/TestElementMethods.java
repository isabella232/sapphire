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

package org.eclipse.sapphire.tests.modeling.misc.t0018;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class TestElementMethods
{
    public static void method1( final TestElement element )
    {
        
    }
    
    public static String[] method2( final TestElement element, 
                                    final int a,
                                    final String b,
                                    final String[] c,
                                    final List<String> d )
                                            
        throws IOException
        
    {
        return new String[] { "foo", "bar" };
    }
    
}