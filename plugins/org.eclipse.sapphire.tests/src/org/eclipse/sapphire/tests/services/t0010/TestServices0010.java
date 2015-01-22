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

package org.eclipse.sapphire.tests.services.t0010;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.MasterVersionCompatibilityService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibilityService;
import org.eclipse.sapphire.VersionCompatibilityTargetService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests for various services involved in the version compatibility feature, including ContextVersionService, 
 * VersionCompatibilityService, MasterVersionCompatibilityService, VersionCompatibilityValidationService, 
 * VersionCompatibilityEnablementService, and VersionCompatibilityFactsService.
 *  
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestServices0010 extends SapphireTestCase
{
    @Test
    
    public void testVersionCompatibility() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        // Start with null context version.
        
        final VersionCompatibilityTargetService rootContextVersionService = root.service( VersionCompatibilityTargetService.class );
        
        assertNotNull( rootContextVersionService );
        assertNull( rootContextVersionService.version() );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, false );
        
        // Test with context version set so that no compatibility constraints match.
        
        root.setVersion( "1.0" );
        
        assertEquals( new Version( "1.0" ), rootContextVersionService.version() );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, false );
        
        // Test with context version set so that some compatibility constraints match.
        
        root.setVersion( "1.2" );
        
        assertEquals( new Version( "1.2" ), rootContextVersionService.version() );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE, true );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, true );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, false );
        
        // Test with context version set so that all compatibility constraints match.
        
        root.setVersion( "1.2.3" );
        
        assertEquals( new Version( "1.2.3" ), rootContextVersionService.version() );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE, true );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, true );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY, true );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, true );
    }
    
    @Test
    
    public void testDynamicVersionCompatibility() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        final VersionCompatibilityTargetService rootContextVersionService = root.service( VersionCompatibilityTargetService.class );
        
        assertNotNull( rootContextVersionService );
        assertNull( rootContextVersionService.version() );
        
        root.setVersion( "1.2.5" );
        
        assertEquals( new Version( "1.2.5" ), rootContextVersionService.version() );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, true );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, true );
        
        root.setSwitch( true );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, false );
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, false );
    }
    
    @Test
    
    public void testCustomVersionCompatibilityService() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        final VersionCompatibilityTargetService rootContextVersionService = root.service( VersionCompatibilityTargetService.class );
        
        assertNotNull( rootContextVersionService );
        assertNull( rootContextVersionService.version() );

        final TestVersionCompatibilityService service 
            = (TestVersionCompatibilityService) root.property( RootElement.PROP_VALUE_VERSION_COMPATIBILITY_SERVICE ).service( VersionCompatibilityService.class );
        
        assertNotNull( service );
        
        root.setVersion( "2.0" );
        
        assertEquals( new Version( "2.0" ), rootContextVersionService.version() );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_SERVICE, true );
        
        service.update( "3.0" );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_SERVICE, false );
        
        service.update( "1.0" );
        
        assertVersionCompatibility( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_SERVICE, true );
    }
    
    @Test
    
    public void testCascadingVersionCompatibility() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        final ChildElement elementPropertyChild = root.getChild().content( true );
        final ChildElement impliedElementPropertyChild = root.getChildImplied();
        final ChildElement listElementChild1 = root.getChildren().insert();
        final ChildElement listElementChild2 = root.getChildren().insert();
        
        root.setVersion( "1.0" );

        assertVersionCompatibility( root, RootElement.PROP_CHILD, false );
        assertVersionCompatibility( elementPropertyChild, ChildElement.PROP_VALUE_UNCONSTRAINED, false );
        assertVersionCompatibility( elementPropertyChild, ChildElement.PROP_VALUE_SINCE, false );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILD_IMPLIED, false );
        assertVersionCompatibility( impliedElementPropertyChild, ChildElement.PROP_VALUE_UNCONSTRAINED, false );
        assertVersionCompatibility( impliedElementPropertyChild, ChildElement.PROP_VALUE_SINCE, false );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILDREN, false );
        assertVersionCompatibility( listElementChild1, ChildElement.PROP_VALUE_UNCONSTRAINED, false );
        assertVersionCompatibility( listElementChild1, ChildElement.PROP_VALUE_SINCE, false );
        assertVersionCompatibility( listElementChild2, ChildElement.PROP_VALUE_UNCONSTRAINED, false );
        assertVersionCompatibility( listElementChild2, ChildElement.PROP_VALUE_SINCE, false );
        
        root.setVersion( "2.0" );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILD, true );
        assertVersionCompatibility( elementPropertyChild, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( elementPropertyChild, ChildElement.PROP_VALUE_SINCE, false );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILD_IMPLIED, true );
        assertVersionCompatibility( impliedElementPropertyChild, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( impliedElementPropertyChild, ChildElement.PROP_VALUE_SINCE, false );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILDREN, true );
        assertVersionCompatibility( listElementChild1, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild1, ChildElement.PROP_VALUE_SINCE, false );
        assertVersionCompatibility( listElementChild2, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild2, ChildElement.PROP_VALUE_SINCE, false );
        
        final ChildElement listElementChild3 = root.getChildren().insert();
        
        assertVersionCompatibility( listElementChild3, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild3, ChildElement.PROP_VALUE_SINCE, false );
        
        root.setVersion( "3.0" );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILD, true );
        assertVersionCompatibility( elementPropertyChild, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( elementPropertyChild, ChildElement.PROP_VALUE_SINCE, true );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILD_IMPLIED, true );
        assertVersionCompatibility( impliedElementPropertyChild, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( impliedElementPropertyChild, ChildElement.PROP_VALUE_SINCE, true );
        
        assertVersionCompatibility( root, RootElement.PROP_CHILDREN, true );
        assertVersionCompatibility( listElementChild1, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild1, ChildElement.PROP_VALUE_SINCE, true );
        assertVersionCompatibility( listElementChild2, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild2, ChildElement.PROP_VALUE_SINCE, true );
        assertVersionCompatibility( listElementChild3, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild3, ChildElement.PROP_VALUE_SINCE, true );
        
        final ChildElement listElementChild4 = root.getChildren().insert();
        
        assertVersionCompatibility( listElementChild4, ChildElement.PROP_VALUE_UNCONSTRAINED, true );
        assertVersionCompatibility( listElementChild4, ChildElement.PROP_VALUE_SINCE, true );
    }
    
    @Test
    
    public void testVersionCompatibilityValidationServiceForValue() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        assertValidationOk( root.getValueSince() );
        
        root.setValueSince( "abc" );
        assertValidationError( root.getValueSince(), "Version constraint exists, but no version constraint target was found" );

        root.setValueSince( null );
        root.setVersion( "1.0" );
        assertValidationOk( root.getValueSince() );
        
        root.setValueSince( "abc" );
        assertValidationError( root.getValueSince(), "Not compatible with version 1 of Test Versioned System" );
        
        root.setValueSince( null );
        assertValidationOk( root.getValueSince() );
    }
    
    @Test
    
    public void testVersionCompatibilityValidationServiceForElement() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        assertValidationOk( root.getChild() );
        
        root.getChild().content( true );
        assertValidationError( root.getChild(), "Version constraint exists, but no version constraint target was found" );

        root.getChild().clear();
        root.setVersion( "1.0" );
        assertValidationOk( root.getChild() );
        
        root.getChild().content( true );
        assertValidationError( root.getChild(), "Not compatible with version 1 of Test Versioned System" );
        
        root.getChild().clear();
        assertValidationOk( root.getChild() );
    }
    
    @Test
    
    public void testVersionCompatibilityValidationServiceForList() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        assertValidationOk( root.getChildren() );
        
        root.getChildren().insert();
        assertValidationError( root.getChildren(), "Version constraint exists, but no version constraint target was found" );

        root.getChildren().remove( 0 );
        root.setVersion( "1.0" );
        assertValidationOk( root.getChildren() );
        
        root.getChildren().insert();
        assertValidationError( root.getChildren(), "Not compatible with version 1 of Test Versioned System" );
        
        root.getChildren().remove( 0 );
        assertValidationOk( root.getChildren() );
    }
    
    @Test
    
    public void testVersionCompatibilityEnablementServiceForValue() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final Property property = root.property( RootElement.PROP_VALUE_SINCE );
        
        assertFalse( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
        
        root.setVersion( "3.0" );
        assertTrue( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
    }
    
    @Test
    
    public void testVersionCompatibilityEnablementServiceForElement() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final Property property = root.property( RootElement.PROP_CHILD );
        
        assertFalse( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
        
        root.setVersion( "3.0" );
        assertTrue( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
    }

    @Test
    
    public void testVersionCompatibilityEnablementServiceForElementImplied() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final Property property = root.property( RootElement.PROP_CHILD_IMPLIED );
        
        assertFalse( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
        
        root.setVersion( "3.0" );
        assertTrue( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
    }
    
    @Test
    
    public void testVersionCompatibilityEnablementServiceForList() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        final Property property = root.property( RootElement.PROP_CHILDREN );
        
        assertFalse( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
        
        root.setVersion( "3.0" );
        assertTrue( property.enabled() );
        
        root.setVersion( "1.0" );
        assertFalse( property.enabled() );
    }

    @Test
    
    public void testVersionCompatibilityFactsService() throws Exception
    {
        final RootElement root = RootElement.TYPE.instantiate();
        
        root.setVersion( "1.0" );

        assertFact( root, RootElement.PROP_VALUE_SINCE, "Since Test Versioned System 1.2" );
        assertFact( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, "Since Test Versioned System 1.2" );
        assertFact( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY, "For Test Versioned System [1.2.3-1.3)" );
        assertFact( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, "For Test Versioned System [1.2.3-1.3)" );
        
        root.setSwitch( true );
        
        assertFact( root, RootElement.PROP_VALUE_SINCE, "Since Test Versioned System 1.2" );
        assertFact( root, RootElement.PROP_VALUE_SINCE_DYNAMIC, "Since Test Versioned System 2" );
        assertFact( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY, "For Test Versioned System [1.2.3-1.3)" );
        assertFact( root, RootElement.PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC, "Since Test Versioned System 2" );
        
        final ChildElement child = root.getChildImplied();
        
        assertFact( child, ChildElement.PROP_VALUE_UNCONSTRAINED, "Since Test Versioned System 2" );
        assertFact( child, ChildElement.PROP_VALUE_SINCE, "Since Test Versioned System 3" );
    }

    private static void assertVersionCompatibility( final Element element,
                                                    final PropertyDef property,
                                                    final boolean expectedVersionCompatibility )
    {
        final MasterVersionCompatibilityService service = element.property( property ).service( MasterVersionCompatibilityService.class );
        
        assertNotNull( service );
        assertEquals( expectedVersionCompatibility, service.compatible() );
    }

}
