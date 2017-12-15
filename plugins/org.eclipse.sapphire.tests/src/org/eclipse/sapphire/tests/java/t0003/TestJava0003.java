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

package org.eclipse.sapphire.tests.java.t0003;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.modeling.ClassLocator;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.osgi.framework.Bundle;

/**
 * Tests resolution of Java type references in the model via StandardJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestJava0003

    extends SapphireTestCase
    
{
    private static final String PACKAGE_NAME = "org.eclipse.sapphire.tests.java.t0003";
    
    private TestJava0003( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Java0003" );

        suite.addTest( new TestJava0003( "testTopLevel" ) );
        suite.addTest( new TestJava0003( "testInner" ) );
        suite.addTest( new TestJava0003( "testWithCustomClassLocator" ) );
        
        return suite;
    }
    
    public void testTopLevel()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.setSomeClass( PACKAGE_NAME + ".TestClass" );
        
        final JavaType type = element.getSomeClass().resolve();

        assertNotNull( type );
    }

    public void testInner()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        element.setSomeClass( PACKAGE_NAME + ".TestClass$Inner" );
        
        final JavaType type = element.getSomeClass().resolve();

        assertNotNull( type );
    }
    
    public void testWithCustomClassLocator()
    {
        final RootXmlResource resource = new RootXmlResource()
        {
            @Override
            @SuppressWarnings( "unchecked" )
            
            public <A> A adapt( final Class<A> adapterType )
            {
                if( adapterType == ClassLocator.class )
                {
                    final Bundle bundle = Platform.getBundle( "org.eclipse.core.resources" );
                    
                    return (A) new ClassLocator()
                    {
                        @Override
                        public Class<?> find( final String name )
                        {
                            try
                            {
                                return bundle.loadClass( name );
                            }
                            catch( ClassNotFoundException e )
                            {
                                // Intentionally ignoring.
                            }
                            
                            return null;
                        }
                        
                    };
                }
                
                return super.adapt( adapterType );
            }
        };
        
        final TestElement element = TestElement.TYPE.instantiate( resource );
        
        JavaType type;
        
        element.setSomeClass( PACKAGE_NAME + ".TestClass" );
        type = element.getSomeClass().resolve();
        assertNull( type );
        
        element.setSomeClass( "org.eclipse.core.resources.IFile" );
        type = element.getSomeClass().resolve();
        assertNotNull( type );
    }

}
