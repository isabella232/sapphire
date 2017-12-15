/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.java.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.java.JavaTypeConstraints;
import org.eclipse.sapphire.modeling.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.java.JavaTypeName;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaTypeNameValidator

    extends ModelPropertyValidator<Value<JavaTypeName>>

{
    private final ValueProperty property;
    private final boolean typeMustExist;
    private final boolean isClassOk;
    private final boolean isAbstractClassOk;
    private final boolean isInterfaceOk;
    private final boolean isAnnotationOk;
    private final boolean isEnumOk;
    private final String[] validBaseTypes;
    
    public JavaTypeNameValidator( final ValueProperty property )
    {
        this.property = property;
        
        this.typeMustExist = ( this.property.getAnnotation( MustExist.class ) != null );

        final JavaTypeConstraints javaTypeConstraintsAnnotation = this.property.getAnnotation( JavaTypeConstraints.class );
        
        if( javaTypeConstraintsAnnotation != null )
        {
            this.validBaseTypes = javaTypeConstraintsAnnotation.type();
            
            boolean c = false, d = false, i = false, a = false, e = false;
            
            for( JavaTypeKind kind : javaTypeConstraintsAnnotation.kind() )
            {
                switch( kind )
                {
                    case CLASS:           c = true; break;
                    case ABSTRACT_CLASS:  d = true; break;
                    case INTERFACE:       i = true; break;
                    case ANNOTATION:      a = true; break;
                    case ENUM:            e = true; break;
                    default:              throw new IllegalStateException();
                }
            }
            
            this.isClassOk = c;
            this.isAbstractClassOk = d;
            this.isInterfaceOk = i;
            this.isAnnotationOk = a;
            this.isEnumOk = e;
        }
        else
        {
            this.validBaseTypes = new String[ 0 ];

            this.isClassOk = true;
            this.isAbstractClassOk = true;
            this.isInterfaceOk = true;
            this.isAnnotationOk = true;
            this.isEnumOk = true;
        }
    }
    
    @Override
    public IStatus validate( final Value<JavaTypeName> value )
    {
        final String val = value.getText( false );
        
        if( val != null )
        {
            final IProject project = value.adapt( IProject.class );
            
            if( project != null )
            {
                try
                {
                    final IType type = getType( val, project );
                    
                    if( type == null || ! type.exists() )
                    {
                        if( this.typeMustExist )
                        {
                            final String msg = Resources.bind( Resources.typeNotFound, val );
                            return createErrorStatus( msg );
                        }
                        else
                        {
                            return Status.OK_STATUS;
                        }
                    }
                    
                    if( type.isClass() )
                    {
                        final boolean isAbstract = Flags.isAbstract( type.getFlags() );
                        
                        if( isAbstract )
                        {
                            if( ! this.isAbstractClassOk )
                            {
                                final String label = this.property.getLabel( false, CapitalizationType.NO_CAPS, false );
                                final String msg = Resources.bind( Resources.abstractClassNotAllowed, val, label );
                                return createErrorStatus( msg );
                            }
                        }
                        else
                        {
                            if( ! this.isClassOk )
                            {
                                final String label = this.property.getLabel( false, CapitalizationType.NO_CAPS, false );
                                final String msg = Resources.bind( Resources.classNotAllowed, val, label );
                                return createErrorStatus( msg );
                            }
                        }
                        
                        final IStatus st = validateRequiredType( type, val );
                        
                        if( st != null )
                        {
                            return st;
                        }
                    }
                    else if( type.isAnnotation() )
                    {
                        if( ! this.isAnnotationOk )
                        {
                            final String label = this.property.getLabel( false, CapitalizationType.NO_CAPS, false );
                            final String msg = Resources.bind( Resources.annotationNotAllowed, val, label );
                            return createErrorStatus( msg );
                        }
                    }
                    else if( type.isInterface() )
                    {
                        if( ! this.isInterfaceOk )
                        {
                            final String label = this.property.getLabel( false, CapitalizationType.NO_CAPS, false );
                            final String msg = Resources.bind( Resources.interfaceNotAllowed, val, label );
                            return createErrorStatus( msg );
                        }
                        
                        final IStatus st = validateRequiredType( type, val );
                        
                        if( st != null )
                        {
                            return st;
                        }
                    }
                    else if( type.isEnum() )
                    {
                        if( ! this.isEnumOk )
                        {
                            final String label = this.property.getLabel( false, CapitalizationType.NO_CAPS, false );
                            final String msg = Resources.bind( Resources.enumNotAllowed, val, label );
                            return createErrorStatus( msg );
                        }
                    }
                }
                catch( CoreException e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
        }
        
        return Status.OK_STATUS;
    }
    
    private IStatus validateRequiredType( final IType type,
                                          final String typeName )
    
        throws JavaModelException
        
    {
        if( this.validBaseTypes.length > 0 )
        {
            final ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy( null );

            for( String baseType : this.validBaseTypes )
            {
                if( ! type.getFullyQualifiedName().equals( baseType ) )
                {
                    boolean hasSuperType = false;
                    
                    for( IType t : typeHierarchy.getAllSupertypes( type ) )
                    {
                        if( t.getFullyQualifiedName().equals( baseType ) )
                        {
                            hasSuperType = true;
                            break;
                        }
                    }
                    
                    if( ! hasSuperType )
                    {
                        final String template = ( type.isClass() ? Resources.classDoesNotImplementOrExtend : Resources.interfaceDoesNotExtend );
                        final String msg = Resources.bind( template, typeName, baseType );
                        return createErrorStatus( msg );
                    }
                }
            }
        }
        
        return null;
    }
    
    private static IType getType( String fullyQualifiedType, IProject project )
    {
        // the JDT returns a non-null anonymous class IType 
        // for empty string and package names that end with a dot
        // if the type starts with a dot, the JDT helpfully removes it 
        // and returns the type referenced without the dot
        // short circuit here for perf and so validation results make sense
        // e.g. if the valid type is "Thing", then ".Thing" and "Thing." should not be valid
        if ( fullyQualifiedType.trim().length() == 0 
                || fullyQualifiedType.startsWith(".") //$NON-NLS-1$
                        || fullyQualifiedType.endsWith(".")) //$NON-NLS-1$
            return null;
        
        IJavaProject javaProject = JavaCore.create( project );
        
        if (( javaProject == null ) || ( !javaProject.exists() ))
        {
            return null;
        }

        IType type = null;
        try
        {
            type = javaProject.findType( fullyQualifiedType );
            if ( type != null && ( !type.exists() || type.isAnonymous() ) )
            {
                type = null;
            }
        }
        catch ( JavaModelException ce )
        {
            SapphireModelingFrameworkPlugin.log( ce );
        }
        return type;
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String typeNotFound;
        public static String classDoesNotImplementOrExtend;
        public static String interfaceDoesNotExtend;
        public static String abstractClassNotAllowed;
        public static String classNotAllowed;
        public static String interfaceNotAllowed;
        public static String annotationNotAllowed;
        public static String enumNotAllowed;
        
        static
        {
            initializeMessages( JavaTypeNameValidator.class.getName(), Resources.class );
        }
    }
    
}
