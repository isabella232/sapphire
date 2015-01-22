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

package org.eclipse.sapphire.tests.modeling.xml;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlBindingTests extends SapphireTestCase
{
    @Test
    
    public void testValueProperties1() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final XmlBindingTestModel model = XmlBindingTestModel.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( XmlBindingTests.class.getSimpleName() + ".testValueProperties1.txt" ) );
    }
    
    @Test
    
    public void testValueProperties2() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final XmlBindingTestModelAltB model = XmlBindingTestModelAltB.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( XmlBindingTests.class.getSimpleName() + ".testValueProperties2.txt" ) );
    }
    
    @Test
    
    public void testValueProperties3() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( resourceStore );
        final XmlBindingTestModelAltC model = XmlBindingTestModelAltC.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        
        testValueProperties( resourceStore, model, loadResource( XmlBindingTests.class.getSimpleName() + ".testValueProperties3.txt" ) );
    }

    private void testValueProperties( final ByteArrayResourceStore resourceStore,
                                      final XmlBindingTestModel model,
                                      final String expected )
    
        throws Exception
        
    {
        model.resource().setCorruptedResourceExceptionInterceptor
        (
             new CorruptedResourceExceptionInterceptor()
             {
                @Override
                public boolean shouldAttemptRepair()
                {
                    return true;
                }
             }
        );
        
        model.setValuePropertyA( "aaaa" );
        assertEquals( "aaaa", model.getValuePropertyA().text() );
        
        model.setValuePropertyB( "bbbb" );
        assertEquals( "bbbb", model.getValuePropertyB().text() );
        
        model.setValuePropertyC( "cccc" );
        assertEquals( "cccc", model.getValuePropertyC().text() );
        
        model.setValuePropertyD( "dddd" );
        assertEquals( "dddd", model.getValuePropertyD().text() );
        
        model.setValuePropertyE( "eeee" );
        assertEquals( "eeee", model.getValuePropertyE().text() );
        
        model.setValuePropertyF( "ffff" );
        assertEquals( "ffff", model.getValuePropertyF().text() );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( expected, result );
    }
    
}
