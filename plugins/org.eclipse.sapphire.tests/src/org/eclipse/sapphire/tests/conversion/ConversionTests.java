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

package org.eclipse.sapphire.tests.conversion;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.sapphire.FileName;
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionConstraint;
import org.eclipse.sapphire.java.JavaIdentifier;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests MasterConversionService and the various conversions included with Sapphire.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ConversionTests extends SapphireTestCase
{
    @Test
    
    public void testStringToBoolean() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Boolean.TRUE, service.convert( "true", Boolean.class ) );
        assertEquals( Boolean.TRUE, service.convert( "TRUE", Boolean.class ) );
        assertEquals( Boolean.TRUE, service.convert( "True", Boolean.class ) );
        assertEquals( Boolean.TRUE, service.convert( "tRuE", Boolean.class ) );
        
        assertEquals( Boolean.FALSE, service.convert( "false", Boolean.class ) );
        assertEquals( Boolean.FALSE, service.convert( "FALSE", Boolean.class ) );
        assertEquals( Boolean.FALSE, service.convert( "False", Boolean.class ) );
        assertEquals( Boolean.FALSE, service.convert( "fAlSe", Boolean.class ) );
        
        assertNull( service.convert( "yes", Boolean.class ) );
        assertNull( service.convert( "no", Boolean.class ) );
        assertNull( service.convert( "0", Boolean.class ) );
        assertNull( service.convert( "1", Boolean.class ) );
        assertNull( service.convert( "abcdef", Boolean.class ) );
    }
    
    @Test

    public void testBooleanToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "true", service.convert( Boolean.TRUE, String.class ) );
        assertEquals( "false", service.convert( Boolean.FALSE, String.class ) );
    }
    
    @Test
    
    public void testStringToByte() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Byte.valueOf( (byte) -128 ), service.convert( "-128", Byte.class ) );
        assertEquals( Byte.valueOf( (byte) -48 ), service.convert( "-48", Byte.class ) );
        assertEquals( Byte.valueOf( (byte) 0 ), service.convert( "0", Byte.class ) );
        assertEquals( Byte.valueOf( (byte) 47 ), service.convert( "47", Byte.class ) );
        assertEquals( Byte.valueOf( (byte) 127 ), service.convert( "127", Byte.class ) );
    }
    
    @Test

    public void testByteToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-128", service.convert( Byte.valueOf( (byte) -128 ), String.class ) );
        assertEquals( "-48", service.convert( Byte.valueOf( (byte) -48 ), String.class ) );
        assertEquals( "0", service.convert( Byte.valueOf( (byte) 0 ), String.class ) );
        assertEquals( "47", service.convert( Byte.valueOf( (byte) 47 ), String.class ) );
        assertEquals( "127", service.convert( Byte.valueOf( (byte) 127 ), String.class ) );
    }
    
    @Test
    
    public void testStringToShort() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Short.valueOf( (short) -32768 ), service.convert( "-32768", Short.class ) );
        assertEquals( Short.valueOf( (short) -128 ), service.convert( "-128", Short.class ) );
        assertEquals( Short.valueOf( (short) -48 ), service.convert( "-48", Short.class ) );
        assertEquals( Short.valueOf( (short) 0 ), service.convert( "0", Short.class ) );
        assertEquals( Short.valueOf( (short) 47 ), service.convert( "47", Short.class ) );
        assertEquals( Short.valueOf( (short) 127 ), service.convert( "127", Short.class ) );
        assertEquals( Short.valueOf( (short) 32767 ), service.convert( "32767", Short.class ) );
    }
    
    @Test

    public void testShortToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-32768", service.convert( Short.valueOf( (short) -32768 ), String.class ) );
        assertEquals( "-128", service.convert( Short.valueOf( (short) -128 ), String.class ) );
        assertEquals( "-48", service.convert( Short.valueOf( (short) -48 ), String.class ) );
        assertEquals( "0", service.convert( Short.valueOf( (short) 0 ), String.class ) );
        assertEquals( "47", service.convert( Short.valueOf( (short) 47 ), String.class ) );
        assertEquals( "127", service.convert( Short.valueOf( (short) 127 ), String.class ) );
        assertEquals( "32767", service.convert( Short.valueOf( (short) 32767 ), String.class ) );
    }
    
    @Test

    public void testStringToInteger() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Integer.valueOf( -2147483648 ), service.convert( "-2147483648", Integer.class ) );
        assertEquals( Integer.valueOf( -32768 ), service.convert( "-32768", Integer.class ) );
        assertEquals( Integer.valueOf( -128 ), service.convert( "-128", Integer.class ) );
        assertEquals( Integer.valueOf( -48 ), service.convert( "-48", Integer.class ) );
        assertEquals( Integer.valueOf( 0 ), service.convert( "0", Integer.class ) );
        assertEquals( Integer.valueOf( 47 ), service.convert( "47", Integer.class ) );
        assertEquals( Integer.valueOf( 127 ), service.convert( "127", Integer.class ) );
        assertEquals( Integer.valueOf( 32767 ), service.convert( "32767", Integer.class ) );
        assertEquals( Integer.valueOf( 2147483647 ), service.convert( "2147483647", Integer.class ) );
    }
    
    @Test

    public void testIntegerToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-2147483648", service.convert( Integer.valueOf( -2147483648 ), String.class ) );
        assertEquals( "-32768", service.convert( Integer.valueOf( -32768 ), String.class ) );
        assertEquals( "-128", service.convert( Integer.valueOf( -128 ), String.class ) );
        assertEquals( "-48", service.convert( Integer.valueOf( -48 ), String.class ) );
        assertEquals( "0", service.convert( Integer.valueOf( 0 ), String.class ) );
        assertEquals( "47", service.convert( Integer.valueOf( 47 ), String.class ) );
        assertEquals( "127", service.convert( Integer.valueOf( 127 ), String.class ) );
        assertEquals( "32767", service.convert( Integer.valueOf( 32767 ), String.class ) );
        assertEquals( "2147483647", service.convert( Integer.valueOf( 2147483647 ), String.class ) );
    }
    
    @Test
    
    public void testStringToLong() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Long.valueOf( -9223372036854775808L ), service.convert( "-9223372036854775808", Long.class ) );
        assertEquals( Long.valueOf( -2147483648L ), service.convert( "-2147483648", Long.class ) );
        assertEquals( Long.valueOf( -32768L ), service.convert( "-32768", Long.class ) );
        assertEquals( Long.valueOf( -128L ), service.convert( "-128", Long.class ) );
        assertEquals( Long.valueOf( -48L ), service.convert( "-48", Long.class ) );
        assertEquals( Long.valueOf( 0L ), service.convert( "0", Long.class ) );
        assertEquals( Long.valueOf( 47L ), service.convert( "47", Long.class ) );
        assertEquals( Long.valueOf( 127L ), service.convert( "127", Long.class ) );
        assertEquals( Long.valueOf( 32767L ), service.convert( "32767", Long.class ) );
        assertEquals( Long.valueOf( 2147483647L ), service.convert( "2147483647", Long.class ) );
        assertEquals( Long.valueOf( 9223372036854775807L ), service.convert( "9223372036854775807", Long.class ) );
    }
    
    @Test

    public void testLongToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-9223372036854775808", service.convert( Long.valueOf( -9223372036854775808L ), String.class ) );
        assertEquals( "-2147483648", service.convert( Long.valueOf( -2147483648L ), String.class ) );
        assertEquals( "-32768", service.convert( Long.valueOf( -32768L ), String.class ) );
        assertEquals( "-128", service.convert( Long.valueOf( -128L ), String.class ) );
        assertEquals( "-48", service.convert( Long.valueOf( -48L ), String.class ) );
        assertEquals( "0", service.convert( Long.valueOf( 0L ), String.class ) );
        assertEquals( "47", service.convert( Long.valueOf( 47L ), String.class ) );
        assertEquals( "127", service.convert( Long.valueOf( 127L ), String.class ) );
        assertEquals( "32767", service.convert( Long.valueOf( 32767L ), String.class ) );
        assertEquals( "2147483647", service.convert( Long.valueOf( 2147483647L ), String.class ) );
        assertEquals( "9223372036854775807", service.convert( Long.valueOf( 9223372036854775807L ), String.class ) );
    }
    
    @Test
    
    public void testStringToFloat() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Float.valueOf( -15.773523F ), service.convert( "-15.773523", Float.class ) );
        assertEquals( Float.valueOf( 0.0F ), service.convert( "0.0", Float.class ) );
        assertEquals( Float.valueOf( 15.773523F ), service.convert( "15.773523", Float.class ) );
    }
    
    @Test

    public void testFloatToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-15.773523", service.convert( Float.valueOf( -15.773523F ), String.class ) );
        assertEquals( "0.0", service.convert( Float.valueOf( 0.0F ), String.class ) );
        assertEquals( "15.773523", service.convert( Float.valueOf( 15.773523F ), String.class ) );
    }
    
    @Test
    
    public void testStringToDouble() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( Double.valueOf( -15.773523D ), service.convert( "-15.773523", Double.class ) );
        assertEquals( Double.valueOf( 0.0D ), service.convert( "0.0", Double.class ) );
        assertEquals( Double.valueOf( 15.773523D ), service.convert( "15.773523", Double.class ) );
    }
    
    @Test

    public void testDoubleToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-15.773523", service.convert( Double.valueOf( -15.773523D ), String.class ) );
        assertEquals( "0.0", service.convert( Double.valueOf( 0.0D ), String.class ) );
        assertEquals( "15.773523", service.convert( Double.valueOf( 15.773523D ), String.class ) );
    }
    
    @Test
    
    public void testStringToBigInteger() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new BigInteger( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808" ), service.convert( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808", BigInteger.class ) );
        assertEquals( new BigInteger( "-9223372036854775808" ), service.convert( "-9223372036854775808", BigInteger.class ) );
        assertEquals( new BigInteger( "-2147483648" ), service.convert( "-2147483648", BigInteger.class ) );
        assertEquals( new BigInteger( "-32768" ), service.convert( "-32768", BigInteger.class ) );
        assertEquals( new BigInteger( "-128" ), service.convert( "-128", BigInteger.class ) );
        assertEquals( new BigInteger( "-48" ), service.convert( "-48", BigInteger.class ) );
        assertEquals( new BigInteger( "0" ), service.convert( "0", BigInteger.class ) );
        assertEquals( new BigInteger( "47" ), service.convert( "47", BigInteger.class ) );
        assertEquals( new BigInteger( "127" ), service.convert( "127", BigInteger.class ) );
        assertEquals( new BigInteger( "32767" ), service.convert( "32767", BigInteger.class ) );
        assertEquals( new BigInteger( "2147483647" ), service.convert( "2147483647", BigInteger.class ) );
        assertEquals( new BigInteger( "9223372036854775807" ), service.convert( "9223372036854775807", BigInteger.class ) );
        assertEquals( new BigInteger( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" ), service.convert( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807", BigInteger.class ) );
    }
    
    @Test

    public void testBigIntegerToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808", service.convert( new BigInteger( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808" ), String.class ) );
        assertEquals( "-9223372036854775808", service.convert( new BigInteger( "-9223372036854775808" ), String.class ) );
        assertEquals( "-2147483648", service.convert( new BigInteger( "-2147483648" ), String.class ) );
        assertEquals( "-32768", service.convert( new BigInteger( "-32768" ), String.class ) );
        assertEquals( "-128", service.convert( new BigInteger( "-128" ), String.class ) );
        assertEquals( "-48", service.convert( new BigInteger( "-48" ), String.class ) );
        assertEquals( "0", service.convert( new BigInteger( "0" ), String.class ) );
        assertEquals( "47", service.convert( new BigInteger( "47" ), String.class ) );
        assertEquals( "127", service.convert( new BigInteger( "127" ), String.class ) );
        assertEquals( "32767", service.convert( new BigInteger( "32767" ), String.class ) );
        assertEquals( "2147483647", service.convert( new BigInteger( "2147483647" ), String.class ) );
        assertEquals( "9223372036854775807", service.convert( new BigInteger( "9223372036854775807" ), String.class ) );
        assertEquals( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807", service.convert( new BigInteger( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" ), String.class ) );
    }
    
    @Test
    
    public void testStringToBigDecimal() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new BigDecimal( "-922337203685477580892233720368547758089223372036854775808.92233720368547758089223372036854775808" ), service.convert( "-922337203685477580892233720368547758089223372036854775808.92233720368547758089223372036854775808", BigDecimal.class ) );
        assertEquals( new BigDecimal( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808" ), service.convert( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808", BigDecimal.class ) );
        assertEquals( new BigDecimal( "-9223372036854775808" ), service.convert( "-9223372036854775808", BigDecimal.class ) );
        assertEquals( new BigDecimal( "-2147483648" ), service.convert( "-2147483648", BigDecimal.class ) );
        assertEquals( new BigDecimal( "-32768" ), service.convert( "-32768", BigDecimal.class ) );
        assertEquals( new BigDecimal( "-128" ), service.convert( "-128", BigDecimal.class ) );
        assertEquals( new BigDecimal( "-48" ), service.convert( "-48", BigDecimal.class ) );
        assertEquals( new BigDecimal( "0" ), service.convert( "0", BigDecimal.class ) );
        assertEquals( new BigDecimal( "47" ), service.convert( "47", BigDecimal.class ) );
        assertEquals( new BigDecimal( "127" ), service.convert( "127", BigDecimal.class ) );
        assertEquals( new BigDecimal( "32767" ), service.convert( "32767", BigDecimal.class ) );
        assertEquals( new BigDecimal( "2147483647" ), service.convert( "2147483647", BigDecimal.class ) );
        assertEquals( new BigDecimal( "9223372036854775807" ), service.convert( "9223372036854775807", BigDecimal.class ) );
        assertEquals( new BigDecimal( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" ), service.convert( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807", BigDecimal.class ) );
        assertEquals( new BigDecimal( "922337203685477580792233720368547758079223372036854775807.92233720368547758079223372036854775807" ), service.convert( "922337203685477580792233720368547758079223372036854775807.92233720368547758079223372036854775807", BigDecimal.class ) );
    }
    
    @Test

    public void testBigDecimalToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "-922337203685477580892233720368547758089223372036854775808.92233720368547758089223372036854775808", service.convert( new BigDecimal( "-922337203685477580892233720368547758089223372036854775808.92233720368547758089223372036854775808" ), String.class ) );
        assertEquals( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808", service.convert( new BigDecimal( "-92233720368547758089223372036854775808922337203685477580892233720368547758089223372036854775808" ), String.class ) );
        assertEquals( "-9223372036854775808", service.convert( new BigDecimal( "-9223372036854775808" ), String.class ) );
        assertEquals( "-2147483648", service.convert( new BigDecimal( "-2147483648" ), String.class ) );
        assertEquals( "-32768", service.convert( new BigDecimal( "-32768" ), String.class ) );
        assertEquals( "-128", service.convert( new BigDecimal( "-128" ), String.class ) );
        assertEquals( "-48", service.convert( new BigDecimal( "-48" ), String.class ) );
        assertEquals( "0", service.convert( new BigDecimal( "0" ), String.class ) );
        assertEquals( "47", service.convert( new BigDecimal( "47" ), String.class ) );
        assertEquals( "127", service.convert( new BigDecimal( "127" ), String.class ) );
        assertEquals( "32767", service.convert( new BigDecimal( "32767" ), String.class ) );
        assertEquals( "2147483647", service.convert( new BigDecimal( "2147483647" ), String.class ) );
        assertEquals( "9223372036854775807", service.convert( new BigDecimal( "9223372036854775807" ), String.class ) );
        assertEquals( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807", service.convert( new BigDecimal( "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" ), String.class ) );
        assertEquals( "922337203685477580792233720368547758079223372036854775807.92233720368547758079223372036854775807", service.convert( new BigDecimal( "922337203685477580792233720368547758079223372036854775807.92233720368547758079223372036854775807" ), String.class ) );
    }
    
    @Test

    public void testStringToDate() throws Exception
    {
        MasterConversionService service;
        
        service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2013-01-15" ), service.convert( "2013-01-15", Date.class ) );
        assertEquals( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).parse( "2013-01-15T18:38:24" ), service.convert( "2013-01-15T18:38:24", Date.class ) );
        assertNull( service.convert( "2013.01.15", Date.class ) );
        
        DateConversionTestElement element = DateConversionTestElement.TYPE.instantiate();
        
        service = element.property( DateConversionTestElement.PROP_DATE_1 ).service( MasterConversionService.class );
        
        assertEquals( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2013-01-15" ), service.convert( "2013-01-15", Date.class ) );
        assertEquals( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).parse( "2013-01-15T18:38:24" ), service.convert( "2013-01-15T18:38:24", Date.class ) );
        assertNull( service.convert( "2013.01.15", Date.class ) );
        
        service = element.property( DateConversionTestElement.PROP_DATE_2 ).service( MasterConversionService.class );
        
        assertEquals( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2013-01-15" ), service.convert( "2013.01.15", Date.class ) );
        assertEquals( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2013-01-15" ), service.convert( "01/15/2013", Date.class ) );
        assertNull( service.convert( "2013-01-15", Date.class ) );
    }
    
    @Test

    public void testDateToString() throws Exception
    {
        final SimpleDateFormat fullDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'.'SSSZ" );
        final Date date = fullDateFormat.parse( "2013-01-15T18:38:24.000-0800" );
        final DateConversionTestElement element = DateConversionTestElement.TYPE.instantiate();
        
        MasterConversionService service;
        
        service = Sapphire.service( MasterConversionService.class );
        assertEquals( fullDateFormat.format( date ), service.convert( date, String.class ) );

        service = element.property( DateConversionTestElement.PROP_DATE_1 ).service( MasterConversionService.class );
        assertEquals( fullDateFormat.format( date ), service.convert( date, String.class ) );
        
        service = element.property( DateConversionTestElement.PROP_DATE_2 ).service( MasterConversionService.class );
        assertEquals( "2013.01.15", service.convert( date, String.class ) );
    }
    
    @Test

    public void testStringToFileName() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new FileName( "abc" ), service.convert( "abc", FileName.class ) );
        assertEquals( new FileName( "abc.txt" ), service.convert( "abc.txt", FileName.class ) );
        
        assertNull( service.convert( "folder/abc.txt", FileName.class ) );
    }
    
    @Test

    public void testFileNameToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "abc", service.convert( new FileName( "abc" ), String.class ) );
        assertEquals( "abc.txt", service.convert( new FileName( "abc.txt" ), String.class ) );
    }
    
    @Test

    public void testStringToPath() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new Path( "abc" ), service.convert( "abc", Path.class ) );
        assertEquals( new Path( "abc.txt" ), service.convert( "abc.txt", Path.class ) );
        assertEquals( new Path( "folder/abc.txt" ), service.convert( "folder/abc.txt", Path.class ) );
        assertEquals( new Path( "x/y/z/folder/abc.txt" ), service.convert( "x/y/z/folder/abc.txt", Path.class ) );
    }
    
    @Test

    public void testPathToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "abc", service.convert( new Path( "abc" ), String.class ) );
        assertEquals( "abc.txt", service.convert( new Path( "abc.txt" ), String.class ) );
        assertEquals( "folder/abc.txt", service.convert( new Path( "folder/abc.txt" ), String.class ) );
        assertEquals( "x/y/z/folder/abc.txt", service.convert( new Path( "x/y/z/folder/abc.txt" ), String.class ) );
    }
    
    @Test

    public void testStringToUri() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new URI( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt" ), service.convert( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt", URI.class ) );
        assertEquals( new URI( "ftp://example.org/resource.txt" ), service.convert( "ftp://example.org/resource.txt", URI.class ) );
        assertEquals( new URI( "relative/path/to/resource.txt" ), service.convert( "relative/path/to/resource.txt", URI.class ) );
        assertEquals( new URI( "../../../resource.txt" ), service.convert( "../../../resource.txt", URI.class ) );
    }
    
    @Test

    public void testUriToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt", service.convert( new URI( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt" ), String.class ) );
        assertEquals( "ftp://example.org/resource.txt", service.convert( new URI( "ftp://example.org/resource.txt" ), String.class ) );
        assertEquals( "relative/path/to/resource.txt", service.convert( new URI( "relative/path/to/resource.txt" ), String.class ) );
        assertEquals( "../../../resource.txt", service.convert( new URI( "../../../resource.txt" ), String.class ) );
    }
    
    @Test

    public void testStringToUrl() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new URL( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt" ), service.convert( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt", URL.class ) );
        assertEquals( new URL( "ftp://example.org/resource.txt" ), service.convert( "ftp://example.org/resource.txt", URL.class ) );
        
        assertNull( service.convert( "relative/path/to/resource.txt", URL.class ) );
        assertNull( service.convert( "../../../resource.txt", URL.class ) );
    }
    
    @Test

    public void testUrlToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt", service.convert( new URL( "http://example.org/absolute/URI/with/absolute/path/to/resource.txt" ), String.class ) );
        assertEquals( "ftp://example.org/resource.txt", service.convert( new URL( "ftp://example.org/resource.txt" ), String.class ) );
    }
    
    @Test

    public void testStringToVersion() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new Version( "1" ), service.convert( "1", Version.class ) );
        assertEquals( new Version( "1.2" ), service.convert( "1.2", Version.class ) );
        assertEquals( new Version( "1.2.3" ), service.convert( "1.2.3", Version.class ) );
        assertEquals( new Version( "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20" ), service.convert( "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20", Version.class ) );
        
        assertNull( service.convert( "1..2", Version.class ) );
        assertNull( service.convert( "1.abc", Version.class ) );
    }
    
    @Test

    public void testVersionToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "1", service.convert( new Version( "1" ), String.class ) );
        assertEquals( "1.2", service.convert( new Version( "1.2" ), String.class ) );
        assertEquals( "1.2.3", service.convert( new Version( "1.2.3" ), String.class ) );
        assertEquals( "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20", service.convert( new Version( "1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20" ), String.class ) );
    }
    
    @Test

    public void testStringToVersionConstraint() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new VersionConstraint( "1" ), service.convert( "1", VersionConstraint.class ) );
        assertEquals( new VersionConstraint( "1.2" ), service.convert( "1.2", VersionConstraint.class ) );
        assertEquals( new VersionConstraint( "1.2,3.4,5.6" ), service.convert( "1.2,3.4,5.6", VersionConstraint.class ) );
        assertEquals( new VersionConstraint( "[1.2-3.4)" ), service.convert( "[1.2-3.4)", VersionConstraint.class ) );
        assertEquals( new VersionConstraint( "[1.2-3.4),[5.6" ), service.convert( "[1.2-3.4),[5.6", VersionConstraint.class ) );
        
        assertNull( service.convert( "[1.2--3", VersionConstraint.class ) );
        assertNull( service.convert( "[1.2-3.4}", VersionConstraint.class ) );
    }
    
    @Test
    
    public void testVersionConstraintToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "1", service.convert( new VersionConstraint( "1" ), String.class ) );
        assertEquals( "1.2", service.convert( new VersionConstraint( "1.2" ), String.class ) );
        assertEquals( "1.2,3.4,5.6", service.convert( new VersionConstraint( "1.2,3.4,5.6" ), String.class ) );
        assertEquals( "[1.2-3.4)", service.convert( new VersionConstraint( "[1.2-3.4)" ), String.class ) );
        assertEquals( "[1.2-3.4),[5.6", service.convert( new VersionConstraint( "[1.2-3.4),[5.6" ), String.class ) );
    }
    
    @Test
    
    public void testIFileToWorkspaceFileResourceStore() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        final IProject project = project();
        
        final IFile xmlFile = project.getFile( "file.xml" );
        xmlFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final WorkspaceFileResourceStore xmlFileStore = service.convert( xmlFile, WorkspaceFileResourceStore.class );
        assertNotNull( xmlFileStore );
        
        final IFile txtFile = project.getFile( "file.txt" );
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final WorkspaceFileResourceStore txtFileStore = service.convert( txtFile, WorkspaceFileResourceStore.class );
        assertNotNull( txtFileStore );
        
        final IFile binFile = project.getFile( "file.bin" );
        binFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final WorkspaceFileResourceStore binFileStore = service.convert( binFile, WorkspaceFileResourceStore.class );
        assertNotNull( binFileStore );
    }
    
    @Test
    
    public void testIFileToByteArrayResourceStore() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        final IProject project = project();
        
        final IFile xmlFile = project.getFile( "file.xml" );
        xmlFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final ByteArrayResourceStore xmlFileStore = service.convert( xmlFile, ByteArrayResourceStore.class );
        assertNotNull( xmlFileStore );
        
        final IFile txtFile = project.getFile( "file.txt" );
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final ByteArrayResourceStore txtFileStore = service.convert( txtFile, ByteArrayResourceStore.class );
        assertNotNull( txtFileStore );
        
        final IFile binFile = project.getFile( "file.bin" );
        binFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final ByteArrayResourceStore binFileStore = service.convert( binFile, ByteArrayResourceStore.class );
        assertNotNull( binFileStore );
    }
    
    @Test

    public void testIFileToResourceStore() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        final IProject project = project();
        
        final IFile xmlFile = project.getFile( "file.xml" );
        xmlFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final ResourceStore xmlFileStore = service.convert( xmlFile, ResourceStore.class );
        assertNotNull( xmlFileStore );
        
        final IFile txtFile = project.getFile( "file.txt" );
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final ResourceStore txtFileStore = service.convert( txtFile, ResourceStore.class );
        assertNotNull( txtFileStore );
        
        final IFile binFile = project.getFile( "file.bin" );
        binFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final ResourceStore binFileStore = service.convert( binFile, ResourceStore.class );
        assertNotNull( binFileStore );
    }
    
    @Test

    public void testIFileToRootXmlResource() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        final IProject project = project();
        
        final IFile xmlFile = project.getFile( "file.xml" );
        xmlFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final RootXmlResource xmlFileResource = service.convert( xmlFile, RootXmlResource.class );
        assertNotNull( xmlFileResource );

        final IFile txtFile = project.getFile( "file.txt" );
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final RootXmlResource txtFileStore = service.convert( txtFile, RootXmlResource.class );
        assertNull( txtFileStore );
    }
    
    @Test

    public void testIFileToXmlResource() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        final IProject project = project();
        
        final IFile xmlFile = project.getFile( "file.xml" );
        xmlFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final XmlResource xmlFileResource = service.convert( xmlFile, XmlResource.class );
        assertNotNull( xmlFileResource );

        final IFile txtFile = project.getFile( "file.txt" );
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final XmlResource txtFileStore = service.convert( txtFile, XmlResource.class );
        assertNull( txtFileStore );
    }
    
    @Test

    public void testIFileToResource() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        final IProject project = project();
        
        final IFile xmlFile = project.getFile( "file.xml" );
        xmlFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final Resource xmlFileResource = service.convert( xmlFile, Resource.class );
        assertNotNull( xmlFileResource );

        final IFile txtFile = project.getFile( "file.txt" );
        txtFile.create( new ByteArrayInputStream( new byte[ 0 ] ), true, null );
        
        final Resource txtFileStore = service.convert( txtFile, Resource.class );
        assertNull( txtFileStore );
    }
    
    @Test
    
    public void testModelElementToDomDocument() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final RootXmlResource xmlResource = new RootXmlResource( xmlResourceStore );
        final XmlConversionTestElement elementOnXml = XmlConversionTestElement.TYPE.instantiate( xmlResource );

        final Document document = service.convert( elementOnXml, Document.class );
        
        assertNotNull( document );
        assertSame( document, xmlResource.getDomDocument() );
        assertSame( document, elementOnXml.adapt( Document.class ) );
        assertSame( document, elementOnXml.getList().insert().adapt( Document.class ) );
        
        final XmlConversionTestElement elementNotOnXml = XmlConversionTestElement.TYPE.instantiate();
        
        assertNull( service.convert( elementNotOnXml, Document.class ) );
        assertNull( service.convert( elementNotOnXml.getList().insert(), Document.class ) );
    }
    
    @Test

    public void testModelElementToDomElement() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final RootXmlResource xmlResource = new RootXmlResource( xmlResourceStore );
        final XmlConversionTestElement elementOnXml = XmlConversionTestElement.TYPE.instantiate( xmlResource );
        
        xmlResource.save();

        final Element xmlElement = service.convert( elementOnXml, Element.class );
        
        assertNotNull( xmlElement );
        assertSame( xmlElement, xmlResource.getXmlElement().getDomNode() );
        assertSame( xmlElement, elementOnXml.adapt( Element.class ) );
        
        final XmlConversionTestElement.ListEntry childElement = elementOnXml.getList().insert();
        final Element childXmlElement = service.convert( childElement, Element.class );
        
        assertNotNull( childXmlElement );
        assertSame( childXmlElement, ( (XmlResource) childElement.resource() ).getXmlElement().getDomNode() );
        assertSame( childXmlElement, childElement.adapt( Element.class ) );
        assertNotSame( childXmlElement, xmlElement );
        
        final XmlConversionTestElement elementNotOnXml = XmlConversionTestElement.TYPE.instantiate();
        
        assertNull( service.convert( elementNotOnXml, Element.class ) );
        assertNull( service.convert( elementNotOnXml.getList().insert(), Element.class ) );
    }
    
    @Test
    
    public void testModelElementToXmlElement() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final RootXmlResource xmlResource = new RootXmlResource( xmlResourceStore );
        final XmlConversionTestElement elementOnXml = XmlConversionTestElement.TYPE.instantiate( xmlResource );
        
        xmlResource.save();

        final XmlElement xmlElement = service.convert( elementOnXml, XmlElement.class );
        
        assertNotNull( xmlElement );
        assertSame( xmlElement, xmlResource.getXmlElement() );
        assertSame( xmlElement, elementOnXml.adapt( XmlElement.class ) );
        
        final XmlConversionTestElement.ListEntry childElement = elementOnXml.getList().insert();
        final XmlElement childXmlElement = service.convert( childElement, XmlElement.class );
        
        assertNotNull( childXmlElement );
        assertSame( childXmlElement, ( (XmlResource) childElement.resource() ).getXmlElement() );
        assertSame( childXmlElement, childElement.adapt( XmlElement.class ) );
        assertNotSame( childXmlElement, xmlElement );
        
        final XmlConversionTestElement elementNotOnXml = XmlConversionTestElement.TYPE.instantiate();
        
        assertNull( service.convert( elementNotOnXml, XmlElement.class ) );
        assertNull( service.convert( elementNotOnXml.getList().insert(), XmlElement.class ) );
    }
    
    @Test
    
    public void testXmlResourceToDomDocument() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final RootXmlResource xmlResource = new RootXmlResource( xmlResourceStore );

        final Document document = service.convert( xmlResource, Document.class );
        
        assertNotNull( document );
        assertSame( document, xmlResource.getDomDocument() );
        assertSame( document, xmlResource.adapt( Document.class ) );
    }
    
    @Test
    
    public void testXmlResourceToDomElement() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final RootXmlResource xmlResource = new RootXmlResource( xmlResourceStore );
        final XmlConversionTestElement elementOnXml = XmlConversionTestElement.TYPE.instantiate( xmlResource );
        
        xmlResource.save();

        final Element xmlElement = service.convert( xmlResource, Element.class );
        
        assertNotNull( xmlElement );
        assertSame( xmlElement, xmlResource.getXmlElement().getDomNode() );
        assertSame( xmlElement, xmlResource.adapt( Element.class ) );
        
        final XmlConversionTestElement.ListEntry childElement = elementOnXml.getList().insert();
        final Element childXmlElement = service.convert( childElement.resource(), Element.class );
        final XmlResource childXmlResource = (XmlResource) childElement.resource();
        
        assertNotNull( childXmlElement );
        assertSame( childXmlElement, childXmlResource.getXmlElement().getDomNode() );
        assertSame( childXmlElement, childXmlResource.adapt( Element.class ) );
        assertNotSame( childXmlElement, xmlElement );
    }
    
    @Test
    
    public void testXmlResourceToXmlElement() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        final XmlResourceStore xmlResourceStore = new XmlResourceStore();
        final RootXmlResource xmlResource = new RootXmlResource( xmlResourceStore );
        final XmlConversionTestElement elementOnXml = XmlConversionTestElement.TYPE.instantiate( xmlResource );
        
        xmlResource.save();

        final XmlElement xmlElement = service.convert( xmlResource, XmlElement.class );
        
        assertNotNull( xmlElement );
        assertSame( xmlElement, xmlResource.getXmlElement() );
        assertSame( xmlElement, xmlResource.adapt( XmlElement.class ) );
        
        final XmlConversionTestElement.ListEntry childElement = elementOnXml.getList().insert();
        final XmlElement childXmlElement = service.convert( childElement.resource(), XmlElement.class );
        final XmlResource childXmlResource = (XmlResource) childElement.resource();
        
        assertNotNull( childXmlElement );
        assertSame( childXmlElement, childXmlResource.getXmlElement() );
        assertSame( childXmlElement, childXmlResource.adapt( XmlElement.class ) );
        assertNotSame( childXmlElement, xmlElement );
    }
    
    @Test

    public void testStringToJavaIdentifier() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( new JavaIdentifier( "_" ), service.convert( "_", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "$" ), service.convert( "$", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "a" ), service.convert( "a", JavaIdentifier.class ) );

        assertEquals( new JavaIdentifier( "_abc" ), service.convert( "_abc", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "$abc" ), service.convert( "$abc", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "aabc" ), service.convert( "aabc", JavaIdentifier.class ) );

        assertEquals( new JavaIdentifier( "AbC_" ), service.convert( "AbC_", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "AbC$" ), service.convert( "AbC$", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "AbCa" ), service.convert( "AbCa", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "AbC1" ), service.convert( "AbC1", JavaIdentifier.class ) );
        
        assertEquals( new JavaIdentifier( "abc123" ), service.convert( "abc123", JavaIdentifier.class ) );
        assertEquals( new JavaIdentifier( "abc$_123" ), service.convert( "abc$_123", JavaIdentifier.class ) );

        assertNull( service.convert( "1", JavaIdentifier.class ) );
        assertNull( service.convert( "1abc", JavaIdentifier.class ) );
        assertNull( service.convert( "ab#c", JavaIdentifier.class ) );
        assertNull( service.convert( "ab.c", JavaIdentifier.class ) );
    }
    
}
