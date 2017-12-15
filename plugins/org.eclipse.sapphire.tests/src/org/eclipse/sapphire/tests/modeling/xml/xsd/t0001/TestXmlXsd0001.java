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

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0001;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.util.List;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.schema.XmlContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.eclipse.sapphire.modeling.xml.schema.XmlElementDefinition;
import org.eclipse.sapphire.modeling.xml.schema.XmlSequenceGroup;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests handling of XML Schema redefine directive.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class TestXmlXsd0001 extends SapphireTestCase
{
    @Test
    
    public void testSchemaParsing() throws Exception
    {
        final XmlDocumentSchema schema = XmlDocumentSchemasCache.getSchema( "http://www.eclipse.org/sapphire/tests/xml/xsd/0001" );
        
        final XmlElementDefinition rootElementDef = schema.getElement( "root" );
        final XmlSequenceGroup rootContentModel = (XmlSequenceGroup) rootElementDef.getContentModel();
        final List<XmlContentModel> nestedContent = rootContentModel.getNestedContent();
        
        assertEquals( 4, nestedContent.size() );
        assertEquals( "aaa", ( (XmlElementDefinition) nestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "bbb", ( (XmlElementDefinition) nestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "ccc", ( (XmlElementDefinition) nestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "ddd", ( (XmlElementDefinition) nestedContent.get( 3 ) ).getName().getLocalPart() );
    }
    
    @Test
    
    public void testInsertOrder() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final TestXmlXsd0001ModelRoot model = TestXmlXsd0001ModelRoot.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( resourceStore ) ) );
        
        model.setDdd( "ddd" );
        model.setCcc( "ccc" );
        model.setBbb( "bbb" );
        model.setAaa( "aaa" );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "0001.txt" ), result );
    }
    
}
