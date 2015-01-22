/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0002;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.util.List;

import javax.xml.namespace.QName;

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
 * Tests handling of XML Schema include directive.
 * 
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class TestXmlXsd0002 extends SapphireTestCase
{   
    private final static String SCHEMA_LOCATION = "http://www.eclipse.org/sapphire/tests/xml/xsd/0002";

    @Test
    
    public void testSchemaParsing() throws Exception
    {
        final XmlDocumentSchema schema = XmlDocumentSchemasCache.getSchema( SCHEMA_LOCATION );
        
        final XmlElementDefinition rootElementDef = schema.getElement( "element" );
        final XmlSequenceGroup rootContentModel = (XmlSequenceGroup) rootElementDef.getContentModel();
        final List<XmlContentModel> nestedContent = rootContentModel.getNestedContent();
        
        assertEquals( 4, nestedContent.size() );
        assertEquals( "aaa", ( (XmlElementDefinition) nestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "bbb", ( (XmlElementDefinition) nestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "ccc", ( (XmlElementDefinition) nestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "element-2b", ( (XmlElementDefinition) nestedContent.get( 3 ) ).getName().getLocalPart() );

        final XmlSequenceGroup childContentModel = (XmlSequenceGroup)rootContentModel.findChildElementContentModel(new QName( SCHEMA_LOCATION, "element-2b" ));
        final List<XmlContentModel> childNestedContent = childContentModel.getNestedContent();
        assertEquals( 3, childNestedContent.size() );
        assertEquals( "aaa-2b", ( (XmlElementDefinition) childNestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "bbb-2b", ( (XmlElementDefinition) childNestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "ccc-2b", ( (XmlElementDefinition) childNestedContent.get( 2 ) ).getName().getLocalPart() );
    }
    
    @Test
    
    public void testInsertOrder() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final TestXmlXsd0002ModelRoot model = TestXmlXsd0002ModelRoot.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( resourceStore ) ) );
        
        model.setCcc( "ccc" );
        model.setAaa( "aaa" );
        
        TestXmlXsd0002Element2b element2b = model.getElement2();
        element2b.setCcc2( "ccc2" );
        element2b.setBbb2( "bbb2" );
        element2b.setAaa2( "aaa2" );

        model.setBbb( "bbb" );
        
        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "0002.txt" ), result );
    }
}
