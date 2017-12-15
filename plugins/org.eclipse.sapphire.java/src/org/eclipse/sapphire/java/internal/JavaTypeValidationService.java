/******************************************************************************
 * Copyright (c) 2012 Oracle and Liferay
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
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraintBehavior;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeValidationService extends ValidationService
{
    @Override
    protected void init()
    {
        super.init();

        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );

        final JavaTypeConstraintService javaTypeConstraintService = element.service( property, JavaTypeConstraintService.class );
        
        if( javaTypeConstraintService != null )
        {
            javaTypeConstraintService.attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        element.refresh( property );
                    }
                }
            );
        }
    }
    
    @Override
    public Status validate()
    {
        final IModelElement element = context( IModelElement.class );
        final ValueProperty property = context( ValueProperty.class );
        
        final JavaTypeConstraintService javaTypeConstraintService = element.service( property, JavaTypeConstraintService.class );
        
        if( javaTypeConstraintService == null )
        {
            return Status.createOkStatus();
        }
        
        final Set<JavaTypeKind> kinds = javaTypeConstraintService.kinds();
        final Set<String> requiredBaseTypes = javaTypeConstraintService.types();
        final JavaTypeConstraintBehavior behavior = javaTypeConstraintService.behavior();
        
        final ReferenceValue<?,?> value = (ReferenceValue<?,?>) element.read( property );
        final String val = value.getText( false );
        
        if( val != null )
        {
            final JavaType type = (JavaType) value.resolve();
            
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
                        final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = Resources.bind( Resources.classNotAllowed, val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case ABSTRACT_CLASS:
                {
                    if( ! kinds.contains( JavaTypeKind.ABSTRACT_CLASS ) )
                    {
                        final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = Resources.bind( Resources.abstractClassNotAllowed, val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case INTERFACE:
                {
                    if( ! kinds.contains( JavaTypeKind.INTERFACE ) )
                    {
                        final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = Resources.bind( Resources.interfaceNotAllowed, val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case ANNOTATION:
                {
                    if( ! kinds.contains( JavaTypeKind.ANNOTATION ) )
                    {
                        final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = Resources.bind( Resources.annotationNotAllowed, val, label );
                        return Status.createErrorStatus( msg );
                    }
                    
                    break;
                }
                case ENUM:
                {
                    if( ! kinds.contains( JavaTypeKind.ENUM ) )
                    {
                        final String label = property.getLabel( true, CapitalizationType.NO_CAPS, false );
                        final String msg = Resources.bind( Resources.enumNotAllowed, val, label );
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
                            final String template = ( type.kind() == JavaTypeKind.INTERFACE ? Resources.interfaceDoesNotExtend : Resources.classDoesNotImplementOrExtend );
                            final String msg = Resources.bind( template, val, baseType );
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
                        
                        final String template = ( type.kind() == JavaTypeKind.INTERFACE ? Resources.interfaceDoesNotExtendOneOf : Resources.classDoesNotImplementOrExtendOneOf );
                        final String msg = Resources.bind( template, val, list.toString() );
                        return Status.createErrorStatus( msg );
                    }
                }
            }
        }
        
        return Status.createOkStatus();
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            final IModelElement element = context.find( IModelElement.class );
            
            if ( property != null && element != null && property.getTypeClass() == JavaTypeName.class )
            {
                final Reference referenceAnnotation = property.getAnnotation( Reference.class );
                
                if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
                {
                    return element.service( property, JavaTypeConstraintService.class ) != null;
                }
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new JavaTypeValidationService();
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String classDoesNotImplementOrExtend;
        public static String interfaceDoesNotExtend;
        public static String classDoesNotImplementOrExtendOneOf;
        public static String interfaceDoesNotExtendOneOf;
        public static String abstractClassNotAllowed;
        public static String classNotAllowed;
        public static String interfaceNotAllowed;
        public static String annotationNotAllowed;
        public static String enumNotAllowed;
        
        static
        {
            initializeMessages( JavaTypeValidationService.class.getName(), Resources.class );
        }
    }
    
}
