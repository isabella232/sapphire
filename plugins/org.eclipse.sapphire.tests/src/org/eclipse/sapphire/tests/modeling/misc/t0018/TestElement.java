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
import java.io.OutputStream;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Transient;
import org.eclipse.sapphire.TransientProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** StringValue ***
    
    ValueProperty PROP_STRING_VALUE = new ValueProperty( TYPE, "StringValue" );
    
    Value<String> getStringValue();
    void setStringValue( String value );
    
    // *** IntegerValue ***
    
    @Type( base = Integer.class )

    ValueProperty PROP_INTEGER_VALUE = new ValueProperty( TYPE, "IntegerValue" );
    
    Value<Integer> getIntegerValue();
    void setIntegerValue( String value );
    void setIntegerValue( Integer value );
    
    // *** JavaTypeReferenceValue ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    
    ValueProperty PROP_JAVA_TYPE_REFERENCE_VALUE = new ValueProperty( TYPE, "JavaTypeReferenceValue" );
    
    ReferenceValue<JavaTypeName,JavaType> getJavaTypeReferenceValue();
    void setJavaTypeReferenceValue( String value );
    void setJavaTypeReferenceValue( JavaTypeName value );
    
    // *** Transient ***
    
    @Type( base = OutputStream.class )
    
    TransientProperty PROP_TRANSIENT = new TransientProperty( TYPE, "Transient" );
    
    Transient<OutputStream> getTransient();
    void setTransient( OutputStream value );
    
    // *** List ***
    
    @Type( base = TestChildElement.class )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<TestChildElement> getList();
    
    // *** Element ***
    
    @Type( base = TestChildElement.class )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ElementHandle<TestChildElement> getElement();
    
    // *** ImpliedElement ***
    
    @Type( base = TestChildElement.class )
    
    ImpliedElementProperty PROP_IMPLIED_ELEMENT = new ImpliedElementProperty( TYPE, "ImpliedElement" );
    
    TestChildElement getImpliedElement();
    
    // *** Method: method1 ***
    
    @DelegateImplementation( TestElementMethods.class )
    
    void method1();
    
    // *** Method: method2 ***
    
    @DelegateImplementation( TestElementMethods.class )
    
    String[] method2( int a, String b, String[] c, List<String> d ) throws IOException;
    
}