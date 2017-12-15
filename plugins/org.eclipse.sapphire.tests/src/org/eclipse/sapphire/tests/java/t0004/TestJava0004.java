/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.java.t0004;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests JavaTypeValidationService in context of StandardJavaTypeReferenceService.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class TestJava0004

    extends SapphireTestCase
    
{
    private TestElement element;
    
    protected TestJava0004( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "Java0004" );

        suite.addTest( new TestJava0004( "testOptionalAnyType" ) );
        suite.addTest( new TestJava0004( "testRequiredClass1" ) );
        suite.addTest( new TestJava0004( "testRequiredClass2" ) );
        suite.addTest( new TestJava0004( "testRequiredClass3" ) );
        suite.addTest( new TestJava0004( "testRequiredClass4" ) );
        suite.addTest( new TestJava0004( "testRequiredClass5" ) );
        suite.addTest( new TestJava0004( "testRequiredClass6" ) );
        suite.addTest( new TestJava0004( "testRequiredInterface1" ) );
        suite.addTest( new TestJava0004( "testRequiredInterface2" ) );
        suite.addTest( new TestJava0004( "testRequiredInterface3" ) );
        suite.addTest( new TestJava0004( "testRequiredAnnotation1" ) );
        suite.addTest( new TestJava0004( "testRequiredEnum1" ) );
        suite.addTest( new TestJava0004( "testRequiredMixedType1" ) );
        suite.addTest( new TestJava0004( "testRequiredMixedType2" ) );
        
        return suite;
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.element = createTestElement();
    }
    
    protected TestElement createTestElement() throws Exception
    {
        return TestElement.TYPE.instantiate();
    }
    
    public void testOptionalAnyType()
    {
        final ValueProperty property = TestElement.PROP_OPTIONAL_ANY_TYPE;
        
        test( property, "foo.bar.FooBar" );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList" );
        test( property, "java.util.List" );
        test( property, "java.lang.Deprecated" );
        test( property, "java.lang.annotation.ElementType" );
    }
    
    /**
     * @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS } )
     * @MustExist
     */

    public void testRequiredClass1()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_CLASS_1;
        
        test( property, "foo.bar.FooBar", "Could not resolve required class 1 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList" );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required class 1." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required class 1." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required class 1." );
    }
    
    /**
     * @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS }, type = "java.util.List" )
     * @MustExist
     */

    public void testRequiredClass2()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_CLASS_2;
        
        test( property, "foo.bar.FooBar", "Could not resolve required class 2 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList" );
        test( property, "java.util.HashMap", "Class java.util.HashMap does not implement or extend java.util.List." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required class 2." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required class 2." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required class 2." );
    }

    /**
     * @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS }, type = "java.util.AbstractList" )
     * @MustExist
     */

    public void testRequiredClass3()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_CLASS_3;
        
        test( property, "foo.bar.FooBar", "Could not resolve required class 3 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList" );
        test( property, "java.util.HashMap", "Class java.util.HashMap does not implement or extend java.util.AbstractList." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required class 3." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required class 3." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required class 3." );
    }

    /**
     * @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS }, type = { "java.util.AbstractList", "java.lang.Cloneable" } )
     * @MustExist
     */

    public void testRequiredClass4()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_CLASS_4;
        
        test( property, "foo.bar.FooBar", "Could not resolve required class 4 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList", "Class java.util.AbstractList does not implement or extend java.lang.Cloneable." );
        test( property, "java.util.HashMap", "Class java.util.HashMap does not implement or extend java.util.AbstractList." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required class 4." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required class 4." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required class 4." );
    }

    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "java.util.AbstractList" )
     * @MustExist
     */

    public void testRequiredClass5()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_CLASS_5;
        
        test( property, "foo.bar.FooBar", "Could not resolve required class 5 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required class 5." );
        test( property, "java.util.HashMap", "Class java.util.HashMap does not implement or extend java.util.AbstractList." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required class 5." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required class 5." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required class 5." );
    }

    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = { "java.util.List", "java.util.Map" }, behavior = JavaTypeConstraintBehavior.AT_LEAST_ONE )
     * @MustExist
     */

    public void testRequiredClass6()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_CLASS_6;
        
        test( property, "foo.bar.FooBar", "Could not resolve required class 6 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.HashMap" );
        test( property, "java.util.HashSet", "Class java.util.HashSet does not implement or extend one of [java.util.List, java.util.Map]." );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required class 6." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required class 6." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required class 6." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required class 6." );
    }

    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE )
     * @MustExist
     */

    public void testRequiredInterface1()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_INTERFACE_1;
        
        test( property, "foo.bar.FooBar", "Could not resolve required interface 1 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList", "Type java.util.ArrayList is a class, which is not allowed for required interface 1." );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required interface 1." );
        test( property, "java.util.HashMap", "Type java.util.HashMap is a class, which is not allowed for required interface 1." );
        test( property, "java.util.List" );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required interface 1." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required interface 1." );
    }

    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE, type = "java.util.List" )
     * @MustExist
     */

    public void testRequiredInterface2()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_INTERFACE_2;
        
        test( property, "foo.bar.FooBar", "Could not resolve required interface 2 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList", "Type java.util.ArrayList is a class, which is not allowed for required interface 2." );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required interface 2." );
        test( property, "java.util.HashMap", "Type java.util.HashMap is a class, which is not allowed for required interface 2." );
        test( property, "java.util.List" );
        test( property, "java.util.Set", "Interface java.util.Set does not extend java.util.List." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required interface 2." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required interface 2." );
    }

    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.INTERFACE, type = { "java.util.List", "java.lang.Cloneable" } )
     * @MustExist
     */

    public void testRequiredInterface3()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_INTERFACE_3;
        
        test( property, "foo.bar.FooBar", "Could not resolve required interface 3 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList", "Type java.util.ArrayList is a class, which is not allowed for required interface 3." );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required interface 3." );
        test( property, "java.util.HashMap", "Type java.util.HashMap is a class, which is not allowed for required interface 3." );
        test( property, "java.util.List", "Interface java.util.List does not extend java.lang.Cloneable." );
        test( property, "javax.naming.Name", "Interface javax.naming.Name does not extend java.util.List." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required interface 3." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required interface 3." );
    }
    
    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.ANNOTATION )
     * @MustExist
     */

    public void testRequiredAnnotation1()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_ANNOTATION_1;
        
        test( property, "foo.bar.FooBar", "Could not resolve required annotation 1 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList", "Type java.util.ArrayList is a class, which is not allowed for required annotation 1." );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required annotation 1." );
        test( property, "java.util.HashMap", "Type java.util.HashMap is a class, which is not allowed for required annotation 1." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required annotation 1." );
        test( property, "java.util.Set", "Type java.util.Set is an interface, which is not allowed for required annotation 1." );
        test( property, "java.lang.Deprecated" );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required annotation 1." );
    }
    
    /**
     * @JavaTypeConstraint( kind = JavaTypeKind.ENUM )
     * @MustExist
     */

    public void testRequiredEnum1()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_ENUM_1;
        
        test( property, "foo.bar.FooBar", "Could not resolve required enum 1 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList", "Type java.util.ArrayList is a class, which is not allowed for required enum 1." );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required enum 1." );
        test( property, "java.util.HashMap", "Type java.util.HashMap is a class, which is not allowed for required enum 1." );
        test( property, "java.util.List", "Type java.util.List is an interface, which is not allowed for required enum 1." );
        test( property, "java.util.Set", "Type java.util.Set is an interface, which is not allowed for required enum 1." );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required enum 1." );
        test( property, "java.lang.annotation.ElementType" );
    }
    
    /**
     * @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.INTERFACE } )
     * @MustExist
     */

    public void testRequiredMixedType1()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_MIXED_TYPE_1;
        
        test( property, "foo.bar.FooBar", "Could not resolve required mixed type 1 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required mixed type 1." );
        test( property, "java.util.HashMap" );
        test( property, "java.util.List" );
        test( property, "java.util.Set" );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required mixed type 1." );
        test( property, "java.lang.annotation.ElementType", "Type java.lang.annotation.ElementType is an enum, which is not allowed for required mixed type 1." );
    }

    /**
     * @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.INTERFACE, JavaTypeKind.ENUM } )
     * @MustExist
     */

    public void testRequiredMixedType2()
    {
        final ValueProperty property = TestElement.PROP_REQUIRED_MIXED_TYPE_2;
        
        test( property, "foo.bar.FooBar", "Could not resolve required mixed type 2 \"foo.bar.FooBar\"." );
        test( property, "java.util.ArrayList" );
        test( property, "java.util.AbstractList", "Type java.util.AbstractList is an abstract class, which is not allowed for required mixed type 2." );
        test( property, "java.util.HashMap" );
        test( property, "java.util.List" );
        test( property, "java.util.Set" );
        test( property, "java.lang.Deprecated", "Type java.lang.Deprecated is an annotation, which is not allowed for required mixed type 2." );
        test( property, "java.lang.annotation.ElementType" );
    }

    private void test( final ValueProperty property,
                       final String value )
    {
        test( property, value, null );
        
    }
    
    private void test( final ValueProperty property,
                       final String value,
                       final String expectedErrorMessage )
    {
        this.element.write( property, value );
        
        if( expectedErrorMessage == null )
        {
            assertValidationOk( this.element.read( property ) );
        }
        else
        {
            assertValidationError( this.element.read( property ), expectedErrorMessage );
        }
    }

}
