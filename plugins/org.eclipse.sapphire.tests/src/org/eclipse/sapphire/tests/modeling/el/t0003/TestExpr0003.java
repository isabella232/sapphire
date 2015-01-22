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

package org.eclipse.sapphire.tests.modeling.el.t0003;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests accessing size of a collection using Size property.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0003 extends TestExpr
{
    @Test
    
    public void test()
    {
        final List<Integer> list = new ArrayList<Integer>();
        
        for( int i = 0; i < 2; i++ )
        {
            list.add( i );
        }
        
        final Set<Integer> set = new HashSet<Integer>();
        
        for( int i = 0; i < 3; i++ )
        {
            set.add( i );
        }
        
        final Map<Integer,Integer> map = new HashMap<Integer,Integer>();
        
        for( int i = 0; i < 4; i++ )
        {
            map.put( i, i );
        }
        
        final String[] objectArray = new String[] { "a", "b" };
        final char[] charArray = new char[] { 'a', 'b', 'c' };
        final byte[] byteArray = new byte[] { 1, 2, 3, 4 };
        final short[] shortArray = new short[] { 1, 2, 3, 4, 5 };
        final int[] intArray = new int[] { 1, 2, 3, 4, 5, 6 };
        final long[] longArray = new long[] { 1, 2, 3, 4, 5, 6, 7 };
        final float[] floatArray = new float[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        final double[] doubleArray = new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        
        final FunctionContext context = new FunctionContext()
        {
            @Override
            public FunctionResult property( final Object element,
                                            final String name )
            {
                if( element == this && name.equalsIgnoreCase( "List" ) )
                {
                    return Literal.create( list ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "Set" ) )
                {
                    return Literal.create( set ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "Map" ) )
                {
                    return Literal.create( map ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "ObjectArray" ) )
                {
                    return Literal.create( objectArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "CharArray" ) )
                {
                    return Literal.create( charArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "ByteArray" ) )
                {
                    return Literal.create( byteArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "ShortArray" ) )
                {
                    return Literal.create( shortArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "IntArray" ) )
                {
                    return Literal.create( intArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "LongArray" ) )
                {
                    return Literal.create( longArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "FloatArray" ) )
                {
                    return Literal.create( floatArray ).evaluate( this );
                }
                else if( element == this && name.equalsIgnoreCase( "DoubleArray" ) )
                {
                    return Literal.create( doubleArray ).evaluate( this );
                }
                
                return super.property( element, name );
            }
        };
        
        testForExpectedValue( context, "${ List.Size }", 2 );
        testForExpectedValue( context, "${ Set.Size }", 3 );
        testForExpectedValue( context, "${ Map.Size() }", 4 );
        testForExpectedValue( context, "${ ObjectArray.Size }", 2 );
        testForExpectedValue( context, "${ CharArray.Size }", 3 );
        testForExpectedValue( context, "${ ByteArray.Size }", 4 );
        testForExpectedValue( context, "${ ShortArray.Size }", 5 );
        testForExpectedValue( context, "${ IntArray.Size }", 6 );
        testForExpectedValue( context, "${ LongArray.Size }", 7 );
        testForExpectedValue( context, "${ FloatArray.Size }", 8 );
        testForExpectedValue( context, "${ DoubleArray.Size }", 9 );
    }
    
}

