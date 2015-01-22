/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363551] JavaTypeConstraintService
 ******************************************************************************/

package org.eclipse.sapphire.java.internal;

import java.util.Set;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeValidationService extends ValidationService
{
    @Text( "Class {0} does not implement or extend {1}" )
    private static LocalizableText classDoesNotImplementOrExtend;
    
    @Text( "Interface {0} does not extend {1}" )
    private static LocalizableText interfaceDoesNotExtend;
    
    @Text( "Class {0} does not implement or extend one of [{1}]" )
    private static LocalizableText classDoesNotImplementOrExtendOneOf;
    
    @Text( "Interface {0} does not extend one of [{1}]" )
    private static LocalizableText interfaceDoesNotExtendOneOf;
    
    @Text( "Type {0} is an abstract class, which is not allowed for {1}" )
    private static LocalizableText abstractClassNotAllowed;
    
    @Text( "Type {0} is a class, which is not allowed for {1}" )
    private static LocalizableText classNotAllowed;
    
    @Text( "Type {0} is an interface, which is not allowed for {1}" )
    private static LocalizableText interfaceNotAllowed;
    
    @Text( "Type {0} is an annotation, which is not allowed for {1}" )
    private static LocalizableText annotationNotAllowed;
    
    @Text( "Type {0} is an enum, which is not allowed for {1}" )
    private static LocalizableText enumNotAllowed;
    
    static
    {
        LocalizableText.init( JavaTypeValidationService.class );
    }

    @Override
    protected void initValidationService()
    {
        final Property property = context( Property.class );

        property.service( JavaTypeConstraintService.class ).attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refresh();
                }
            }
        );
        
        property.service( ReferenceService.class ).attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refresh();
                }
            }
        );
    }
    
    @Override
    protected Status compute()
    {
        final ReferenceValue<?,?> value = context( ReferenceValue.class );
        final JavaTypeConstraintService javaTypeConstraintService = value.service( JavaTypeConstraintService.class );
        
        if( javaTypeConstraintService == null )
        {
            return Status.createOkStatus();
        }
        
        final Set<JavaTypeKind> kinds = javaTypeConstraintService.kinds();
        final Set<String> requiredBaseTypes = javaTypeConstraintService.types();
        final JavaTypeConstraintBehavior behavior = javaTypeConstraintService.behavior();
        
        final String val = value.text( false );
        
        if( val != null )
        {
            final JavaType type = (JavaType) value.target();
            
            if( type == null )
            {
                return Status.createOkStatus();
            }
            
            final JavaTypeKind kind = type.kind();
            
            switch( kind )
            {
                case CLASS:
                {
                    if( ! kinds.contains( JavaTypeKind.CLASS ) )
                    {
                        final String label = value.definition().getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = classNotAllowed.format( val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case ABSTRACT_CLASS:
                {
                    if( ! kinds.contains( JavaTypeKind.ABSTRACT_CLASS ) )
                    {
                        final String label = value.definition().getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = abstractClassNotAllowed.format( val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case INTERFACE:
                {
                    if( ! kinds.contains( JavaTypeKind.INTERFACE ) )
                    {
                        final String label = value.definition().getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = interfaceNotAllowed.format( val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case ANNOTATION:
                {
                    if( ! kinds.contains( JavaTypeKind.ANNOTATION ) )
                    {
                        final String label = value.definition().getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = annotationNotAllowed.format( val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case ENUM:
                {
                    if( ! kinds.contains( JavaTypeKind.ENUM ) )
                    {
                        final String label = value.definition().getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = enumNotAllowed.format( val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                default:
                {
                    throw new IllegalStateException();
                }
            }
            
            if( kind != JavaTypeKind.ENUM && kind != JavaTypeKind.ANNOTATION )
            {
                if( behavior == JavaTypeConstraintBehavior.ALL )
                {
                    for( String baseType : requiredBaseTypes )
                    {
                        if( ! type.isOfType( baseType ) )
                        {
                            final LocalizableText template = ( type.kind() == JavaTypeKind.INTERFACE ? interfaceDoesNotExtend : classDoesNotImplementOrExtend );
                            final String msg = template.format( val, baseType );
                            return Status.createErrorStatus( msg );
                        }
                    }
                }
                else
                {
                    boolean satisfied = false;
                    
                    for( String baseType : requiredBaseTypes )
                    {
                        if( type.isOfType( baseType ) )
                        {
                            satisfied = true;
                            break;
                        }
                    }
                    
                    if( ! satisfied )
                    {
                        final StringBuilder list = new StringBuilder();
                        
                        for( String baseType : requiredBaseTypes )
                        {
                            if( list.length() > 0 )
                            {
                                list.append( ", " );
                            }
                            
                            list.append( baseType );
                        }
                        
                        final LocalizableText template = ( type.kind() == JavaTypeKind.INTERFACE ? interfaceDoesNotExtendOneOf : classDoesNotImplementOrExtendOneOf );
                        final String msg = template.format( val, list.toString() );
                        return Status.createErrorStatus( msg );
                    }
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            
            if( property != null && property.definition().getTypeClass() == JavaTypeName.class )
            {
                final Reference referenceAnnotation = property.definition().getAnnotation( Reference.class );
                
                if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
                {
                    return property.service( JavaTypeConstraintService.class ) != null;
                }
            }
            
            return false;
        }
    }
    
}
