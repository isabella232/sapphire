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

package org.eclipse.sapphire.tests.modeling.properties.element;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.tests.modeling.properties.element.TestElement.Child;
import org.eclipse.sapphire.tests.modeling.properties.element.TestElement.ChildVariant1;
import org.eclipse.sapphire.tests.modeling.properties.element.TestElement.ChildVariant2;
import org.eclipse.sapphire.tests.modeling.properties.element.TestElement.ChildVariant3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests element properties.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementPropertyTests extends SapphireTestCase
{
    private TestElement element;
    
    @Before
    
    public void before()
    {
        if( this.element != null )
        {
            throw new IllegalStateException();
        }
        
        this.element = TestElement.TYPE.instantiate();
    }
    
    @After
    
    public void after()
    {
        if( this.element == null )
        {
            throw new IllegalStateException();
        }
        
        this.element.dispose();
        this.element = null;
    }
    
    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content()} method on a homogeneous element property.
     */
    
    @Test
    
    public void Homogeneous_Content()
    {
        assertNull( this.element.getHomogeneous().content() );
        
        final Child child = this.element.getHomogeneous().content( true );
        
        assertNotNull( child );
        assertSame( child, this.element.getHomogeneous().content() );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean )} method on a homogeneous element property.
     */

    @Test
    
    public void Homogeneous_Content_Boolean()
    {
        assertNull( this.element.getHomogeneous().content( false ) );
        
        final Child child = this.element.getHomogeneous().content( true );
        
        assertNotNull( child );
        assertSame( child, this.element.getHomogeneous().content( false ) );
        assertSame( child, this.element.getHomogeneous().content( true ) );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a homogeneous element property.
     */

    @Test
    
    public void Homogeneous_Content_Boolean_ElementType()
    {
        assertNull( this.element.getHomogeneous().content( false, Child.TYPE ) );
        
        final Child child = this.element.getHomogeneous().content( true, Child.TYPE );
        
        assertNotNull( child );
        assertSame( child, this.element.getHomogeneous().content( false, Child.TYPE ) );
        assertSame( child, this.element.getHomogeneous().content( true, Child.TYPE ) );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a homogeneous element property
     * when the property is empty, force is allowed and the specified type is not among possible types.
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Homogeneous_Content_Boolean_ElementType_UnsupportedType()
    {
        this.element.getHomogeneous().content( true, ChildVariant1.TYPE );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a homogeneous element property
     * with a null type.
     */

    @Test
    
    public void Homogeneous_Content_Boolean_ElementType_NullType()
    {
        final Child child = this.element.getHomogeneous().content( true, (ElementType) null );
        
        assertNotNull( child );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a homogeneous element property.
     */

    @Test
    
    public void Homogeneous_Content_Boolean_Class()
    {
        assertNull( this.element.getHomogeneous().content( false, Child.class ) );
        
        final Child child = this.element.getHomogeneous().content( true, Child.class );
        
        assertNotNull( child );
        assertSame( child, this.element.getHomogeneous().content( false, Child.class ) );
        assertSame( child, this.element.getHomogeneous().content( true, Child.class ) );
    }
    
    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a homogeneous element property when
     * the property is empty, force is allowed and the specified type is not among possible types.
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Homogeneous_Content_Boolean_Class_UnsupportedType()
    {
        this.element.getHomogeneous().content( true, ChildVariant1.class );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a homogeneous element property with
     * a null type.
     */

    @Test
    
    public void Homogeneous_Content_Boolean_Class_NullType()
    {
        final Child child = this.element.getHomogeneous().content( true, (Class<Child>) null );
        
        assertNotNull( child );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content()} method on a heterogeneous element property.
     */
    
    @Test
    
    public void Heterogeneous_Content()
    {
        assertNull( this.element.getHeterogeneous().content() );
        
        final ChildVariant1 child = this.element.getHeterogeneous().content( true, ChildVariant1.class );
        
        assertNotNull( child );
        assertSame( child, this.element.getHeterogeneous().content() );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean )} method on a heterogeneous element property.
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean()
    {
        assertNull( this.element.getHeterogeneous().content( false ) );
        
        final Child child = this.element.getHeterogeneous().content( true, ChildVariant1.class );
        
        assertNotNull( child );
        assertSame( child, this.element.getHeterogeneous().content( false ) );
        assertSame( child, this.element.getHeterogeneous().content( true ) );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean )} method on a heterogeneous element property when
     * element must be created but expected type is not specified.
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_Exception()
    {
        this.element.getHeterogeneous().content( true );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property.
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_ElementType()
    {
        assertNull( this.element.getHeterogeneous().content( false, ChildVariant1.TYPE ) );
        
        final ChildVariant1 child = (ChildVariant1) this.element.getHeterogeneous().content( true, ChildVariant1.TYPE );
        
        assertNotNull( child );
        assertSame( child, this.element.getHeterogeneous().content( false, ChildVariant1.TYPE ) );
        assertSame( child, this.element.getHeterogeneous().content( true, ChildVariant1.TYPE ) );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is not empty, force is not allowed and a different type is specified. 
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_ElementType_TypeChange_WithoutForce()
    {
        final ChildVariant1 x = (ChildVariant1) this.element.getHeterogeneous().content( true, ChildVariant1.TYPE );
        
        assertNotNull( x );
        
        this.element.getHeterogeneous().content( false, ChildVariant2.TYPE );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is not empty, force is allowed and a different type is specified. 
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_ElementType_TypeChange_WithForce()
    {
        final ChildVariant1 x = (ChildVariant1) this.element.getHeterogeneous().content( true, ChildVariant1.TYPE );
        
        assertNotNull( x );
        
        final ChildVariant2 y = (ChildVariant2) this.element.getHeterogeneous().content( true, ChildVariant2.TYPE );
        
        assertNotNull( y );
        assertTrue( x.disposed() );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is empty, force is not allowed and a type is not specified
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_ElementType_NullType_WithoutForce_OnEmpty()
    {
        final Child x = this.element.getHeterogeneous().content( false, (ElementType) null );
        
        assertNull( x );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is not empty, force is not allowed and a type is not specified
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_ElementType_NullType_WithoutForce_OnFull()
    {
        final ChildVariant1 x = (ChildVariant1) this.element.getHeterogeneous().content( true, ChildVariant1.TYPE );
        
        assertNotNull( x );

        final Child y = this.element.getHeterogeneous().content( false, (ElementType) null );
        
        assertNotNull( y );
        assertSame( x, y );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is empty, force is allowed and a type is not specified
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_ElementType_NullType_WithForce_OnEmpty()
    {
        this.element.getHeterogeneous().content( true, (ElementType) null );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is not empty, force is allowed and a type is not specified
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_ElementType_NullType_WithForce_OnFull()
    {
        final ChildVariant1 x = (ChildVariant1) this.element.getHeterogeneous().content( true, ChildVariant1.TYPE );
        
        assertNotNull( x );
        
        this.element.getHeterogeneous().content( true, (ElementType) null );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, ElementType )} method on a heterogeneous element property
     * when the property is empty, force is allowed and the specified type is not among possible types.
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_ElementType_UnsupportedType()
    {
        this.element.getHeterogeneous().content( true, ChildVariant3.TYPE );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is not empty, force is not allowed and a different type is specified. 
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_Class_TypeChange_WithoutForce()
    {
        final ChildVariant1 x = this.element.getHeterogeneous().content( true, ChildVariant1.class );
        
        assertNotNull( x );
        
        this.element.getHeterogeneous().content( false, ChildVariant2.class );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is not empty, force is allowed and a different type is specified. 
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_Class_TypeChange_WithForce()
    {
        final ChildVariant1 x = this.element.getHeterogeneous().content( true, ChildVariant1.class );
        
        assertNotNull( x );
        
        final ChildVariant2 y = this.element.getHeterogeneous().content( true, ChildVariant2.class );
        
        assertNotNull( y );
        assertTrue( x.disposed() );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is empty, force is not allowed and a type is not specified
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_Class_NullType_WithoutForce_OnEmpty()
    {
        final Child x = this.element.getHeterogeneous().content( false, (Class<ChildVariant1>) null );
        
        assertNull( x );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is not empty, force is not allowed and a type is not specified
     */

    @Test
    
    public void Heterogeneous_Content_Boolean_Class_NullType_WithoutForce_OnFull()
    {
        final ChildVariant1 x = (ChildVariant1) this.element.getHeterogeneous().content( true, ChildVariant1.class );
        
        assertNotNull( x );

        final Child y = this.element.getHeterogeneous().content( false, (Class<ChildVariant1>) null );
        
        assertNotNull( y );
        assertSame( x, y );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is empty, force is allowed and a type is not specified
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_Class_NullType_WithForce_OnEmpty()
    {
        this.element.getHeterogeneous().content( true, (Class<ChildVariant1>) null );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is not empty, force is allowed and a type is not specified
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_Class_NullType_WithForce_OnFull()
    {
        final ChildVariant1 x = this.element.getHeterogeneous().content( true, ChildVariant1.class );
        
        assertNotNull( x );
        
        this.element.getHeterogeneous().content( true, (ElementType) null );
    }

    /**
     * Tests {@link org.eclipse.sapphire.ElementHandle#content( boolean, Class )} method on a heterogeneous element property when
     * the property is empty, force is allowed and the specified type is not among possible types.
     */

    @Test( expected = IllegalArgumentException.class )
    
    public void Heterogeneous_Content_Boolean_Class_UnsupportedType()
    {
        this.element.getHeterogeneous().content( true, ChildVariant3.class );
    }

}
