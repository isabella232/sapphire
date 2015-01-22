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

package org.eclipse.sapphire.tests.java.t0005;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestRootElement extends Element
{
    ElementType TYPE = new ElementType( TestRootElement.class );
    
    // *** KindOne ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS )
    
    ValueProperty PROP_KIND_ONE = new ValueProperty( TYPE, "KindOne" );
    
    ReferenceValue<JavaTypeName,JavaType> getKindOne();
    void setKindOne( String value );
    void setKindOne( JavaTypeName value );

    // *** KindTwo ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS } )
    
    ValueProperty PROP_KIND_TWO = new ValueProperty( TYPE, "KindTwo" );
    
    ReferenceValue<JavaTypeName,JavaType> getKindTwo();
    void setKindTwo( String value );
    void setKindTwo( JavaTypeName value );

    // *** KindThree ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS, JavaTypeKind.INTERFACE } )
    
    ValueProperty PROP_KIND_THREE = new ValueProperty( TYPE, "KindThree" );
    
    ReferenceValue<JavaTypeName,JavaType> getKindThree();
    void setKindThree( String value );
    void setKindThree( JavaTypeName value );

    // *** KindFour ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS, JavaTypeKind.INTERFACE, JavaTypeKind.ANNOTATION } )
    
    ValueProperty PROP_KIND_FOUR = new ValueProperty( TYPE, "KindFour" );
    
    ReferenceValue<JavaTypeName,JavaType> getKindFour();
    void setKindFour( String value );
    void setKindFour( JavaTypeName value );

    // *** TypeOne ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( type = "java.util.List" )
    
    ValueProperty PROP_TYPE_ONE = new ValueProperty( TYPE, "TypeOne" );
    
    ReferenceValue<JavaTypeName,JavaType> getTypeOne();
    void setTypeOne( String value );
    void setTypeOne( JavaTypeName value );

    // *** TypeOneOf ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( type = { "java.util.List", "java.util.Set", "java.util.Map" }, behavior = JavaTypeConstraintBehavior.AT_LEAST_ONE )
    
    ValueProperty PROP_TYPE_ONE_OF = new ValueProperty( TYPE, "TypeOneOf" );
    
    ReferenceValue<JavaTypeName,JavaType> getTypeOneOf();
    void setTypeOneOf( String value );
    void setTypeOneOf( JavaTypeName value );

    // *** TypeAll ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( type = { "java.util.List", "java.lang.Comparable", "java.lang.Cloneable" } )
    
    ValueProperty PROP_TYPE_ALL = new ValueProperty( TYPE, "TypeAll" );
    
    ReferenceValue<JavaTypeName,JavaType> getTypeAll();
    void setTypeAll( String value );
    void setTypeAll( JavaTypeName value );

    // *** Combo1 ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "java.util.List" )
    
    ValueProperty PROP_COMBO_1 = new ValueProperty( TYPE, "Combo1" );
    
    ReferenceValue<JavaTypeName,JavaType> getCombo1();
    void setCombo1( String value );
    void setCombo1( JavaTypeName value );

    // *** Combo2 ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "java.util.AbstractList" )
    
    ValueProperty PROP_COMBO_2 = new ValueProperty( TYPE, "Combo2" );
    
    ReferenceValue<JavaTypeName,JavaType> getCombo2();
    void setCombo2( String value );
    void setCombo2( JavaTypeName value );

    // *** Combo3 ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS, JavaTypeKind.INTERFACE }, type = "java.util.List" )
    
    ValueProperty PROP_COMBO_3 = new ValueProperty( TYPE, "Combo3" );
    
    ReferenceValue<JavaTypeName,JavaType> getCombo3();
    void setCombo3( String value );
    void setCombo3( JavaTypeName value );

}
