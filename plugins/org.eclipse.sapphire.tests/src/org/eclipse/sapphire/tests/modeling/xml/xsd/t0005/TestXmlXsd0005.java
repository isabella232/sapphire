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

package org.eclipse.sapphire.tests.modeling.xml.xsd.t0005;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.schema.XmlContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlContentModelReference;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchema;
import org.eclipse.sapphire.modeling.xml.schema.XmlDocumentSchemasCache;
import org.eclipse.sapphire.modeling.xml.schema.XmlElementDefinition;
import org.eclipse.sapphire.modeling.xml.schema.XmlGroupContentModel;
import org.eclipse.sapphire.modeling.xml.schema.XmlSequenceGroup;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests treatment of namespaces when including a schema. This variant covers the case where included schema
 * defines target namespace.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestXmlXsd0005 extends SapphireTestCase
{
    private static final String NAMESPACE = "http://www.eclipse.org/sapphire/tests/xml/xsd/0005/workbook";

    @Test
    
    public void testSchemaParsing() throws Exception
    {
        final XmlDocumentSchema schema = XmlDocumentSchemasCache.getSchema( NAMESPACE );
        
        final XmlElementDefinition workbookElementDef = schema.getElement( "workbook" );
        final XmlSequenceGroup workbookContentModel = (XmlSequenceGroup) workbookElementDef.getContentModel();
        final List<XmlContentModel> workbookNestedContent = workbookContentModel.getNestedContent();
        
        assertEquals( 1, workbookNestedContent.size() );
        
        final XmlContentModelReference shapesGroupRef = (XmlContentModelReference) workbookNestedContent.get( 0 );
        final XmlGroupContentModel shapesGroup = (XmlGroupContentModel) shapesGroupRef.getContentModel();
        final List<XmlContentModel> shapesGroupNestedContent = shapesGroup.getNestedContent();
        
        assertEquals( 2, shapesGroupNestedContent.size() );
        
        final XmlElementDefinition circleElementDef = (XmlElementDefinition) shapesGroupNestedContent.get( 0 );
        final XmlSequenceGroup circleContentModel = (XmlSequenceGroup) circleElementDef.getContentModel();
        final List<XmlContentModel> circleNestedContent = circleContentModel.getNestedContent();
        
        assertEquals( "circle", circleElementDef.getName().getLocalPart() );
        assertEquals( 3, circleNestedContent.size() );
        assertEquals( "x", ( (XmlElementDefinition) circleNestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "y", ( (XmlElementDefinition) circleNestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "radius", ( (XmlElementDefinition) circleNestedContent.get( 2 ) ).getName().getLocalPart() );
        
        final XmlElementDefinition rectangleElementDef = (XmlElementDefinition) shapesGroupNestedContent.get( 1 );
        final XmlSequenceGroup rectangleContentModel = (XmlSequenceGroup) rectangleElementDef.getContentModel();
        final List<XmlContentModel> rectangleNestedContent = rectangleContentModel.getNestedContent();
        
        assertEquals( "rectangle", rectangleElementDef.getName().getLocalPart() );
        assertEquals( 4, rectangleNestedContent.size() );
        assertEquals( "x", ( (XmlElementDefinition) rectangleNestedContent.get( 0 ) ).getName().getLocalPart() );
        assertEquals( "y", ( (XmlElementDefinition) rectangleNestedContent.get( 1 ) ).getName().getLocalPart() );
        assertEquals( "width", ( (XmlElementDefinition) rectangleNestedContent.get( 2 ) ).getName().getLocalPart() );
        assertEquals( "height", ( (XmlElementDefinition) rectangleNestedContent.get( 3 ) ).getName().getLocalPart() );
        
        final XmlContentModel foundCircleContentModel = workbookContentModel.findChildElementContentModel( new QName( NAMESPACE, "circle" ) );
        assertNotNull( foundCircleContentModel );
        
        final XmlContentModel foundRectangleContentModel = workbookContentModel.findChildElementContentModel( new QName( NAMESPACE, "rectangle" ) );
        assertNotNull( foundRectangleContentModel );
    }
    
    @Test
    
    public void testInsertOrder() throws Exception
    {
        final ByteArrayResourceStore resourceStore = new ByteArrayResourceStore();
        final TestXmlXsd0005Workbook workbook = TestXmlXsd0005Workbook.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( resourceStore ) ) );
        
        final TestXmlXsd0005Circle circle = workbook.getShapes().insert( TestXmlXsd0005Circle.class );
        circle.setRadius( 3 );
        circle.setY( 2 );
        circle.setX( 1 );
        
        final TestXmlXsd0005Rectangle rectangle = workbook.getShapes().insert( TestXmlXsd0005Rectangle.class );
        rectangle.setHeight( 4 );
        rectangle.setWidth( 3 );
        rectangle.setY( 2 );
        rectangle.setX( 1 );

        workbook.resource().save();
        
        final String result = new String( resourceStore.getContents(), UTF8 );
        
        assertEqualsIgnoreNewLineDiffs( loadResource( "ExpectedInsertionOrder.txt" ), result );
    }
    
}
