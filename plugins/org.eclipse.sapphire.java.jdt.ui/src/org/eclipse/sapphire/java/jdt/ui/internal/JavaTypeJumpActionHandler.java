/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.java.jdt.ui.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.forms.JumpActionHandler;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JavaTypeJumpActionHandler extends JumpActionHandler
{
    public static final String ID = "Sapphire.Jump.Java.Type";

    @Text( "Type Not Found" )
    private static LocalizableText couldNotFindTypeDialogTitle;
    
    @Text( "Could not find {0} on project classpath." )
    private static LocalizableText couldNotFindTypeDialogMessage;

    static 
    {
        LocalizableText.init( JavaTypeJumpActionHandler.class );
    }
    
    public JavaTypeJumpActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected boolean computeEnablementState()
    {
        if( super.computeEnablementState() == true )
        {
            final Value<?> value = (Value<?>) property();
            final String typeName = value.text( true );
            
            if( typeName != null && getType( typeName, value.element().adapt( IProject.class ) ) != null )
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected Object run( final Presentation context )
    {
        final Value<?> value = (Value<?>) property();
        final String typeName = value.text( true );
        
        if( typeName != null )
        {
            final IType type = getType( typeName, value.element().adapt( IProject.class ) );
            
            try
            {
                if( type != null )
                {
                    JavaUI.openInEditor( type );
                }
                else 
                {
                    final String message = couldNotFindTypeDialogMessage.format( typeName );
                    MessageDialog.openInformation( ( (FormComponentPresentation) context ).shell(), couldNotFindTypeDialogTitle.text(), message );
                }
            } 
            catch( CoreException e ) 
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
    }
        
        return null;
    }
    
    private static IType getType( String fullyQualifiedType, IProject project )
    {
        assert fullyQualifiedType != null : "Fully qualified type should not be null."; //$NON-NLS-1$
        
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
        
        final String name = fullyQualifiedType.replace( '$', '.' );

        IType type = null;
        
        try
        {
            type = javaProject.findType( name );
            
            if( type != null && ( ! type.exists() || type.isAnonymous() ) )
            {
                type = null;
            }
        }
        catch( JavaModelException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        return type;
    }
    
}