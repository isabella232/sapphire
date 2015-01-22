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

package org.eclipse.sapphire.tests.modeling.xml.xsd.all;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.util.List;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.schema.XmlAllGroup;
import org.eclipse.sapphire.modeling.xml.schema.XmlContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.eclipse.sapphire.modeling.xml.schema.XmlElementDefinition;
import org.eclipse.sapphire.modeling.xml.schema.XmlSequenceGroup;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests handling of XML Schema all construct.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlSchemaAllGroupTests extends SapphireTestCase
{
    @Test
    
    public void testSchemaParsing() throws Exception
    {
        final XmlDocumentSchema schema = XmlDocumentSchemasCache.getSchema( "http://www.eclipse.org/sapphire/tests/xml/xsd/0007" );
        
        final XmlElementDefinition rootElementDef = schema.getElement( "root" );
        final XmlAllGroup rootContentModel = (XmlAllGroup) rootElementDef.getContentModel();
        final List<XmlContentModel> rootNestedContent = rootContentModel.getNestedContent();
        
        assertEquals( 2, rootNestedContent.size() );
        
        final XmlElementDefinition aElementDef = (XmlElementDefinition) rootNestedContent.get( 0 );
        assertEquals( "a", aElementDef.getName().getLocalPart() );
        
        final XmlSequenceGroup aContentModel = (XmlSequenceGroup) aElementDef.getContentModel();
        final List<XmlContentModel> aNestedContent = aContentModel.getNestedContent();
        
        assertEquals( 4, aNestedContent.size() );
        assertEquals( "aa", ( (XmlElementDefinition) aNestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "ab", ( (XmlElementDefinition) aNestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "ac", ( (XmlElementDefinition) aNestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "ad", ( (XmlElementDefinition) aNestedContent.get( 3 ) ).getName().getLocalPart() );
        
        final XmlElementDefinition bElementDef = (XmlElementDefinition) rootNestedContent.get( 1 );
        assertEquals( "b", bElementDef.getName().getLocalPart() );
        
        final XmlSequenceGroup bContentModel = (XmlSequenceGroup) bElementDef.getContentModel();
        final List<XmlContentModel> bNestedContent = bContentModel.getNestedContent();
        
        assertEquals( 4, bNestedContent.size() );
        assertEquals( "ba", ( (XmlElementDefinition) bNestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "bb", ( (XmlElementDefinition) bNestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "bc", ( (XmlElementDefinition) bNestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "bd", ( (XmlElementDefinition) bNestedContent.get( 3 ) ).getName().getLocalPart() );
    }
    
    @Test
    
    public void testInsertOrder() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final TestElement root = TestElement.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( resourceStore ) ) );
        
        final TestElement.B b = root.getB().content( true );
        
        b.setBd( "1" );
        b.setBb( "2" );
        b.setBc( "3" );
        b.setBa( "4" );

        final TestElement.A a = root.getA().content( true );
        
        a.setAd( "1" );
        a.setAa( "2" );
        a.setAc( "3" );
        a.setAb( "4" );
        
        root.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "InsertOrderExpectedResult.txt" ), result );
    }
    
}
