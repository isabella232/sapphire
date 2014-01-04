/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.java.internal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeConstraintServiceData;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class StandardJavaTypeConstraintService extends JavaTypeConstraintService
{
    private JavaTypeConstraintServiceData data;

    @Override
    protected void initJavaTypeConstraintService()
    {
        final PropertyDef property = context().find( PropertyDef.class );
        final JavaTypeConstraint javaTypeConstraintAnnotation = property.getAnnotation( JavaTypeConstraint.class );
        
        final Set<JavaTypeKind> kind = EnumSet.noneOf( JavaTypeKind.class );
        
        for( JavaTypeKind k : javaTypeConstraintAnnotation.kind() )
        {
            kind.add( k );
        }
        
        final Set<String> type = new HashSet<String>();
        
        for( String t : javaTypeConstraintAnnotation.type() )
        {
            if( t != null )
            {
                t = t.trim();
                
                if( t.length() > 0 )
                {
                    type.add( t );
                }
            }
        }
        
        this.data = new JavaTypeConstraintServiceData( kind, type, javaTypeConstraintAnnotation.behavior() );
    }

    @Override
    protected JavaTypeConstraintServiceData compute()
    {
        return this.data;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null && property.getTypeClass() == JavaTypeName.class )
            {
                final JavaTypeConstraint constraintAnnotation = property.getAnnotation( JavaTypeConstraint.class );
                
                if ( constraintAnnotation != null )
                {
                    return true;
                }
            }
            
            return false;
        }
    }

}
