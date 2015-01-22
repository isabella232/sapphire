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

package org.eclipse.sapphire.tests.java.t0003;

import java.io.InputStream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Tests resolution of Java type references in the model via StandardJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestJava0003 extends SapphireTestCase
{
    private static final String PACKAGE_NAME = "org.eclipse.sapphire.tests.java.t0003";
    
    @Test
    
    public void testTopLevel()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.setSomeClass( PACKAGE_NAME + ".TestClass" );
        
        final JavaType type = element.getSomeClass().target();

        assertNotNull( type );
    }
    
    @Test

    public void testInner()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.setSomeClass( PACKAGE_NAME + ".TestClass$Inner" );
        
        final JavaType type = element.getSomeClass().target();

        assertNotNull( type );
    }
    
    @Test
    
    public void testWithCustomContext()
    {
        final RootXmlResource resource = new RootXmlResource()
        {
            @Override
            @SuppressWarnings( "unchecked" )
            
            public <A> A adapt( final Class<A> adapterType )
            {
                if( adapterType == Context.class )
                {
                    final Bundle bundle = Platform.getBundle( "org.eclipse.core.resources" );
                    
                    return (A) new Context()
                    {
                        @Override
                        public Class<?> findClass( final String name )
                        {
                            try
                            {
                                return bundle.loadClass( name );
                            }
                            catch( ClassNotFoundException e )
                            {
                                // Intentionally converting ClassNotFoundException to null return.
                            }
                            
                            return null;
                        }

                        @Override
                        public InputStream findResource( final String name )
                        {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                
                return super.adapt( adapterType );
            }
        };
        
        final TestElement element = TestElement.TYPE.instantiate( resource );
        
        JavaType type;
        
        element.setSomeClass( PACKAGE_NAME + ".TestClass" );
        type = element.getSomeClass().target();
        assertNull( type );
        
        element.setSomeClass( "org.eclipse.core.resources.IFile" );
        type = element.getSomeClass().target();
        assertNotNull( type );
    }

}
