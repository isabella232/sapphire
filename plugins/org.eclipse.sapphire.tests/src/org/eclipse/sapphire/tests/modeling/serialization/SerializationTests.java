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

package org.eclipse.sapphire.tests.modeling.serialization;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SerializationTests

    extends TestCase
    
{
    private SerializationTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "SerializationTests" );

        suite.addTest( new SerializationTests( "testEnumSerialization1" ) );
        suite.addTest( new SerializationTests( "testEnumSerialization2" ) );
        suite.addTest( new SerializationTests( "testEnumSerialization3" ) );
        
        return suite;
    }
    
    public void testEnumSerialization1() throws Exception
    {
        final SerializationTestsModel model = SerializationTestsModel.TYPE.instantiate();

        model.setEnumProperty1( "YES" );
        assertEquals( ThreeChoiceAnswer.YES, model.getEnumProperty1().getContent() );

        model.setEnumProperty1( "yes" );
        assertEquals( ThreeChoiceAnswer.YES, model.getEnumProperty1().getContent() );
        
        model.setEnumProperty1( "yEs" );
        assertEquals( ThreeChoiceAnswer.YES, model.getEnumProperty1().getContent() );
        
        model.setEnumProperty1( "maybe" );
        assertEquals( ThreeChoiceAnswer.MAYBE, model.getEnumProperty1().getContent() );
        
        model.setEnumProperty1( "mAyBe" );
        assertEquals( ThreeChoiceAnswer.MAYBE, model.getEnumProperty1().getContent() );

        model.setEnumProperty1( "no" );
        assertEquals( ThreeChoiceAnswer.NO, model.getEnumProperty1().getContent() );

        model.setEnumProperty1( "NO" );
        assertEquals( ThreeChoiceAnswer.NO, model.getEnumProperty1().getContent() );

        model.setEnumProperty1( ThreeChoiceAnswer.YES );
        assertEquals( "YES", model.getEnumProperty1().getText() );

        model.setEnumProperty1( ThreeChoiceAnswer.MAYBE );
        assertEquals( "MAYBE", model.getEnumProperty1().getText() );
        
        model.setEnumProperty1( ThreeChoiceAnswer.NO );
        assertEquals( "NO", model.getEnumProperty1().getText() );
        
        model.setEnumProperty1( "true" );
        assertNull( model.getEnumProperty1().getContent() );

        model.setEnumProperty1( "false" );
        assertNull( model.getEnumProperty1().getContent() );
        
        model.setEnumProperty1( "sldkfjsdlfskd" );
        assertNull( model.getEnumProperty1().getContent() );
        
        model.setEnumProperty1( "-55" );
        assertNull( model.getEnumProperty1().getContent() );
    }
    
    public void testEnumSerialization2() throws Exception
    {
        final SerializationTestsModel model = SerializationTestsModel.TYPE.instantiate();
        
        model.setEnumProperty2( "YES" );
        assertEquals( ThreeChoiceAnswerCustomized.YES, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "yes" );
        assertEquals( ThreeChoiceAnswerCustomized.YES, model.getEnumProperty2().getContent() );
        
        model.setEnumProperty2( "yEs" );
        assertEquals( ThreeChoiceAnswerCustomized.YES, model.getEnumProperty2().getContent() );
        
        model.setEnumProperty2( "true" );
        assertEquals( ThreeChoiceAnswerCustomized.YES, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "TRUE" );
        assertEquals( ThreeChoiceAnswerCustomized.YES, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "1" );
        assertEquals( ThreeChoiceAnswerCustomized.YES, model.getEnumProperty2().getContent() );
        
        model.setEnumProperty2( "maybe" );
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, model.getEnumProperty2().getContent() );
        
        model.setEnumProperty2( "mAyBe" );
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "0" );
        assertEquals( ThreeChoiceAnswerCustomized.MAYBE, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "no" );
        assertEquals( ThreeChoiceAnswerCustomized.NO, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "NO" );
        assertNull( model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "false" );
        assertEquals( ThreeChoiceAnswerCustomized.NO, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "FALSE" );
        assertNull( model.getEnumProperty2().getContent() );

        model.setEnumProperty2( "-1" );
        assertEquals( ThreeChoiceAnswerCustomized.NO, model.getEnumProperty2().getContent() );

        model.setEnumProperty2( ThreeChoiceAnswerCustomized.YES );
        assertEquals( "yes", model.getEnumProperty2().getText() );

        model.setEnumProperty2( ThreeChoiceAnswerCustomized.MAYBE );
        assertEquals( "maybe", model.getEnumProperty2().getText() );
        
        model.setEnumProperty2( ThreeChoiceAnswerCustomized.NO );
        assertEquals( "no", model.getEnumProperty2().getText() );

        model.setEnumProperty2( "sldkfjsdlfskd" );
        assertNull( model.getEnumProperty2().getContent() );
        
        model.setEnumProperty2( "-55" );
        assertNull( model.getEnumProperty2().getContent() );
    }
    
    public void testEnumSerialization3() throws Exception
    {
        final SerializationTestsModel model = SerializationTestsModel.TYPE.instantiate();
        
        model.setEnumProperty3( "1" );
        assertEquals( ThreeChoiceAnswer.YES, model.getEnumProperty3().getContent() );

        model.setEnumProperty3( "0" );
        assertEquals( ThreeChoiceAnswer.MAYBE, model.getEnumProperty3().getContent() );

        model.setEnumProperty3( "-1" );
        assertEquals( ThreeChoiceAnswer.NO, model.getEnumProperty3().getContent() );
        
        model.setEnumProperty3( "YES" );
        assertNull( model.getEnumProperty3().getContent() );
        
        model.setEnumProperty3( "MAYBE" );
        assertNull( model.getEnumProperty3().getContent() );
        
        model.setEnumProperty3( "NO" );
        assertNull( model.getEnumProperty3().getContent() );

        model.setEnumProperty3( "5" );
        assertNull( model.getEnumProperty3().getContent() );
    }

}
