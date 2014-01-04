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

package org.eclipse.sapphire.tests.conversion.enumeration;

import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests MasterConversionService and the various conversions included with Sapphire.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnumConversionTests extends SapphireTestCase
{
    @Test
    
    public void testStringToEnum_Plain() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( ThreeChoiceAnswer.YES, service.convert( "YES", ThreeChoiceAnswer.class ) );
        assertEquals( ThreeChoiceAnswer.YES, service.convert( "yes", ThreeChoiceAnswer.class ) );
        assertEquals( ThreeChoiceAnswer.YES, service.convert( "yEs", ThreeChoiceAnswer.class ) );
        assertNull( service.convert( "1", ThreeChoiceAnswer.class ) );
        assertNull( service.convert( "true", ThreeChoiceAnswer.class ) );
        
        assertEquals( ThreeChoiceAnswer.MAYBE, service.convert( "MAYBE", ThreeChoiceAnswer.class ) );
        assertEquals( ThreeChoiceAnswer.MAYBE, service.convert( "maybe", ThreeChoiceAnswer.class ) );
        assertEquals( ThreeChoiceAnswer.MAYBE, service.convert( "mAyBe", ThreeChoiceAnswer.class ) );
        assertNull( service.convert( "0", ThreeChoiceAnswer.class ) );
        
        assertEquals( ThreeChoiceAnswer.NO, service.convert( "NO", ThreeChoiceAnswer.class ) );
        assertEquals( ThreeChoiceAnswer.NO, service.convert( "no", ThreeChoiceAnswer.class ) );
        assertEquals( ThreeChoiceAnswer.NO, service.convert( "nO", ThreeChoiceAnswer.class ) );
        assertNull( service.convert( "-1", ThreeChoiceAnswer.class ) );
        assertNull( service.convert( "false", ThreeChoiceAnswer.class ) );
    }
    
    @Test
    
    public void testStringToEnum_Customized() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "YES", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "yes", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "yEs", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "1", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "TRUE", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "true", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.YES, service.convert( "tRuE", ThreeChoiceAnswerCustomized.class ) );
        
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, service.convert( "MAYBE", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, service.convert( "maybe", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, service.convert( "mAyBe", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, service.convert( "0", ThreeChoiceAnswerCustomized.class ) );
        
        assertNull( service.convert( "NO", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.NO, service.convert( "no", ThreeChoiceAnswerCustomized.class ) );
        assertNull( service.convert( "nO", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.NO, service.convert( "-1", ThreeChoiceAnswerCustomized.class ) );
        assertNull( service.convert( "FALSE", ThreeChoiceAnswerCustomized.class ) );
        assertEquals( ThreeChoiceAnswerCustomized.NO, service.convert( "false", ThreeChoiceAnswerCustomized.class ) );
        assertNull( service.convert( "fAlSe", ThreeChoiceAnswerCustomized.class ) );
    }
    
    @Test
    
    public void testStringToEnum_ToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( ThreeChoiceAnswerToString.YES, service.convert( "YES", ThreeChoiceAnswerToString.class ) );
        assertEquals( ThreeChoiceAnswerToString.MAYBE, service.convert( "MAYBE", ThreeChoiceAnswerToString.class ) );
        assertEquals( ThreeChoiceAnswerToString.NO, service.convert( "NO", ThreeChoiceAnswerToString.class ) );
        
        assertNull( service.convert( "1", ThreeChoiceAnswerToString.class ) );
        assertNull( service.convert( "0", ThreeChoiceAnswerToString.class ) );
        assertNull( service.convert( "-1", ThreeChoiceAnswerToString.class ) );
    }
    
    @Test

    public void testEnumToString_Plain() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "YES", service.convert( ThreeChoiceAnswer.YES, String.class ) );
        assertEquals( "MAYBE", service.convert( ThreeChoiceAnswer.MAYBE, String.class ) );
        assertEquals( "NO", service.convert( ThreeChoiceAnswer.NO, String.class ) );
    }
    
    @Test

    public void testEnumToString_Customized() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "yes", service.convert( ThreeChoiceAnswerCustomized.YES, String.class ) );
        assertEquals( "maybe", service.convert( ThreeChoiceAnswerCustomized.MAYBE, String.class ) );
        assertEquals( "no", service.convert( ThreeChoiceAnswerCustomized.NO, String.class ) );
    }
    
    @Test

    public void testEnumToString_ToString() throws Exception
    {
        final MasterConversionService service = Sapphire.service( MasterConversionService.class );
        
        assertEquals( "YES", service.convert( ThreeChoiceAnswerToString.YES, String.class ) );
        assertEquals( "MAYBE", service.convert( ThreeChoiceAnswerToString.MAYBE, String.class ) );
        assertEquals( "NO", service.convert( ThreeChoiceAnswerToString.NO, String.class ) );
    }
    
}
