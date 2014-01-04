/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0003;

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
 * Tests XML Schema with abstract and substitutionGroup attributes.
 * 
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class TestXmlXsd0003 extends SapphireTestCase
{
    private final static String SCHEMA_LOCATION = "http://www.eclipse.org/sapphire/tests/xml/xsd/0003";

    @Test
    
    public void testSchemaParsing() throws Exception
    {
        final XmlDocumentSchema schema = XmlDocumentSchemasCache.getSchema( SCHEMA_LOCATION );
        
        final XmlElementDefinition rootElementDef = schema.getElement( "element" );
        final XmlSequenceGroup rootContentModel = (XmlSequenceGroup) rootElementDef.getContentModel();
        final List<XmlContentModel> nestedContent = rootContentModel.getNestedContent();
        
        assertEquals( 4, nestedContent.size() );
        assertEquals( "shape", ( (XmlElementDefinition) nestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "aaa", ( (XmlElementDefinition) nestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "bbb", ( (XmlElementDefinition) nestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "ccc", ( (XmlElementDefinition) nestedContent.get( 3 ) ).getName().getLocalPart() );

        final XmlContentModel circleContentModel = rootContentModel.findChildElementContentModel(new QName( SCHEMA_LOCATION, "circle" ));
        assertNotNull(circleContentModel);
        final XmlContentModel squareContentModel = rootContentModel.findChildElementContentModel(new QName( SCHEMA_LOCATION, "square" ));
        assertNotNull(squareContentModel);
    }
    
    @Test
    
    public void testInsertOrder() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final TestXmlXsd0003Element model = TestXmlXsd0003Element.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( resourceStore ) ) );
        
        model.setCcc( "ccc" );
        model.setBbb( "bbb" );
        TestXmlXsd0003Circle circle=  model.getCircle();
        circle.setCircle3( "333");
        circle.setCircle2( "222");
        circle.setCircle1( "111");
        model.setAaa( "aaa" );
        TestXmlXsd0003Square square =  model.getSquare();
        square.setSquare3( "333");
        square.setSquare2( "222");
        square.setSquare1( "111");

        model.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "0003.txt" ), result );
    }
    
}
