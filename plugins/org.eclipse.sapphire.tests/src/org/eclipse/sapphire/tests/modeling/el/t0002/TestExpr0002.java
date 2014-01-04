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

package org.eclipse.sapphire.tests.modeling.el.t0002;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.tests.modeling.el.TestExpr;
import org.junit.Test;

/**
 * Tests using array notation to index into a list.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestExpr0002 extends TestExpr
{
    @Test
    
    public void test()
    {
        final List<String> list = new ArrayList<String>();
        list.add( "1" );
        list.add( "2" );
        
        final String[] objectArray = new String[] { "1", "2" };
        final char[] charArray = new char[] { '1', '2' };
        final byte[] byteArray = new byte[] { 1, 2 };
        final short[] shortArray = new short[] { 1, 2 };
        final int[] intArray = new int[] { 1, 2 };
        final long[] longArray = new long[] { 1, 2 };
        final float[] floatArray = new float[] { 1, 2 };
        final double[] doubleArray = new double[] { 1, 2 };
        
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
        
        testForExpectedValue( context, "${ List[ 0 ] }", "1" );
        testForExpectedValue( context, "${ List[ 1 ] }", "2" );
        testForExpectedValue( context, "${ List[ '1' ] }", "2" );
        testForExpectedValue( context, "${ List[ \"1\" ] }", "2" );
        
        testForExpectedError( context, "${ List[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ List[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ List[ 'abc' ] }", "Property \"abc\" is undefined for java.util.ArrayList objects." );
        
        testForExpectedValue( context, "${ ObjectArray[ 0 ] }", "1" );
        testForExpectedValue( context, "${ ObjectArray[ 1 ] }", "2" );
        testForExpectedValue( context, "${ ObjectArray[ '1' ] }", "2" );
        testForExpectedValue( context, "${ ObjectArray[ \"1\" ] }", "2" );
        
        testForExpectedError( context, "${ ObjectArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ ObjectArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ ObjectArray[ 'abc' ] }", "Property \"abc\" is undefined for java.lang.String[] objects." );
        
        testForExpectedValue( context, "${ CharArray[ 0 ] }", '1' );
        testForExpectedValue( context, "${ CharArray[ 1 ] }", '2' );
        testForExpectedValue( context, "${ CharArray[ '1' ] }", '2' );
        testForExpectedValue( context, "${ CharArray[ \"1\" ] }", '2' );
        
        testForExpectedError( context, "${ CharArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ CharArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ CharArray[ 'abc' ] }", "Property \"abc\" is undefined for char[] objects." );

        testForExpectedValue( context, "${ ByteArray[ 0 ] }", (byte) 1 );
        testForExpectedValue( context, "${ ByteArray[ 1 ] }", (byte) 2 );
        testForExpectedValue( context, "${ ByteArray[ '1' ] }", (byte) 2 );
        testForExpectedValue( context, "${ ByteArray[ \"1\" ] }", (byte) 2 );
        
        testForExpectedError( context, "${ ByteArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ ByteArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ ByteArray[ 'abc' ] }", "Property \"abc\" is undefined for byte[] objects." );

        testForExpectedValue( context, "${ ShortArray[ 0 ] }", (short) 1 );
        testForExpectedValue( context, "${ ShortArray[ 1 ] }", (short) 2 );
        testForExpectedValue( context, "${ ShortArray[ '1' ] }", (short) 2 );
        testForExpectedValue( context, "${ ShortArray[ \"1\" ] }", (short) 2 );
        
        testForExpectedError( context, "${ ShortArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ ShortArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ ShortArray[ 'abc' ] }", "Property \"abc\" is undefined for short[] objects." );

        testForExpectedValue( context, "${ IntArray[ 0 ] }", (int) 1 );
        testForExpectedValue( context, "${ IntArray[ 1 ] }", (int) 2 );
        testForExpectedValue( context, "${ IntArray[ '1' ] }", (int) 2 );
        testForExpectedValue( context, "${ IntArray[ \"1\" ] }", (int) 2 );
        
        testForExpectedError( context, "${ IntArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ IntArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ IntArray[ 'abc' ] }", "Property \"abc\" is undefined for int[] objects." );

        testForExpectedValue( context, "${ LongArray[ 0 ] }", (long) 1 );
        testForExpectedValue( context, "${ LongArray[ 1 ] }", (long) 2 );
        testForExpectedValue( context, "${ LongArray[ '1' ] }", (long) 2 );
        testForExpectedValue( context, "${ LongArray[ \"1\" ] }", (long) 2 );
        
        testForExpectedError( context, "${ LongArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ LongArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ LongArray[ 'abc' ] }", "Property \"abc\" is undefined for long[] objects." );

        testForExpectedValue( context, "${ FloatArray[ 0 ] }", (float) 1 );
        testForExpectedValue( context, "${ FloatArray[ 1 ] }", (float) 2 );
        testForExpectedValue( context, "${ FloatArray[ '1' ] }", (float) 2 );
        testForExpectedValue( context, "${ FloatArray[ \"1\" ] }", (float) 2 );
        
        testForExpectedError( context, "${ FloatArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ FloatArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ FloatArray[ 'abc' ] }", "Property \"abc\" is undefined for float[] objects." );

        testForExpectedValue( context, "${ DoubleArray[ 0 ] }", (double) 1 );
        testForExpectedValue( context, "${ DoubleArray[ 1 ] }", (double) 2 );
        testForExpectedValue( context, "${ DoubleArray[ '1' ] }", (double) 2 );
        testForExpectedValue( context, "${ DoubleArray[ \"1\" ] }", (double) 2 );
        
        testForExpectedError( context, "${ DoubleArray[ -1 ] }", "Index -1 is outside the bounds of the collection." );
        testForExpectedError( context, "${ DoubleArray[ 55 ] }", "Index 55 is outside the bounds of the collection." );
        testForExpectedError( context, "${ DoubleArray[ 'abc' ] }", "Property \"abc\" is undefined for double[] objects." );
    }
    
}

