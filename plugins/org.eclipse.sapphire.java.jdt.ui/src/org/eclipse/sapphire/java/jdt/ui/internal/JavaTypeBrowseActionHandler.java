/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [363551] JavaTypeConstraintService
 ******************************************************************************/

package org.eclipse.sapphire.java.jdt.ui.internal;

import java.util.EnumSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeBrowseActionHandler extends BrowseActionHandler
{
    @Text( "Select" )
    private static LocalizableText select;
    
    @Text( "Java type kind \"{0}\" is not recognized." )
    private static LocalizableText typeKindNotRecognized;

    static 
    {
        LocalizableText.init( JavaTypeBrowseActionHandler.class );
    }

    public static final String ID = "Sapphire.Browse.Java.Type";
    public static final String PARAM_KINDS = "kinds";
    
    private String paramKinds;
    
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        
        this.paramKinds = def.getParam( PARAM_KINDS );
    }

    @Override
    public String browse( final Presentation context )
    {
        final Property property = property();
        
        final EnumSet<JavaTypeKind> kinds = EnumSet.noneOf( JavaTypeKind.class );
        
        if( this.paramKinds != null )
        {
            for( String kindString : this.paramKinds.split( "," ) )
            {
                kindString = kindString.trim();
                
                if( kindString.equalsIgnoreCase( JavaTypeKind.CLASS.name() ) )
                {
                    kinds.add( JavaTypeKind.CLASS );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.ABSTRACT_CLASS.name() ) )
                {
                    kinds.add( JavaTypeKind.ABSTRACT_CLASS );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.INTERFACE.name() ) )
                {
                    kinds.add( JavaTypeKind.INTERFACE );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.ANNOTATION.name() ) )
                {
                    kinds.add( JavaTypeKind.ANNOTATION );
                }
                else if( kindString.equalsIgnoreCase( JavaTypeKind.ENUM.name() ) )
                {
                    kinds.add( JavaTypeKind.ENUM );
                }
                else
                {
                    final String msg = typeKindNotRecognized.format( kindString );
                    Sapphire.service( LoggingService.class ).logError( msg );
                }
            }
        }
        else
        {
            final JavaTypeConstraintService javaTypeConstraintService = property.service( JavaTypeConstraintService.class );
            
            if( javaTypeConstraintService != null )
            {
                kinds.addAll( javaTypeConstraintService.kinds() );
            }
        }
        
        int browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ALL_TYPES;        
        int count = kinds.size();
        
        if( count == 1 )
        {
            final JavaTypeKind kind = kinds.iterator().next();
            
            switch( kind )
            {
                case CLASS:           browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES; break;
                case ABSTRACT_CLASS:  browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES; break;
                case INTERFACE:       browseDialogStyle = IJavaElementSearchConstants.CONSIDER_INTERFACES; break;
                case ANNOTATION:      browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ANNOTATION_TYPES; break;
                case ENUM:            browseDialogStyle = IJavaElementSearchConstants.CONSIDER_ENUMS; break;
                default:              throw new IllegalStateException();
            }
        }
        else if( count == 2 )
        {
            if( kinds.contains( JavaTypeKind.CLASS ) || kinds.contains( JavaTypeKind.ABSTRACT_CLASS ) )
            {
                if( kinds.contains( JavaTypeKind.INTERFACE ) )
                {
                    browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES;
                }
                else if( kinds.contains( JavaTypeKind.ENUM ) )
                {
                    browseDialogStyle = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS;
                }
            }
        }

        final IProject project = property.element().adapt( IProject.class );
        
        try 
        {
            final SelectionDialog dlg 
                = JavaUI.createTypeDialog( ( (FormComponentPresentation) context ).shell(), null, project, browseDialogStyle, false );
            
            final String title = property.definition().getLabel( true, CapitalizationType.TITLE_STYLE, false );
            dlg.setTitle(select + title);
            
            if (dlg.open() == SelectionDialog.OK) {
                Object results[] = dlg.getResult();
                assert results != null && results.length == 1;
                if (results[0] instanceof IType) {
                    return ((IType) results[0]).getFullyQualifiedName();
                }
            }
        } catch (JavaModelException e) {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        return null;
    }
    
    public static final class Condition extends PropertyEditorCondition
    {
        @Override
        protected boolean evaluate( final PropertyEditorPart part )
        {
            final Property property = part.property();
            
            if( property instanceof Value && property.definition().isOfType( JavaTypeName.class ) )
            {
                final Reference referenceAnnotation = property.definition().getAnnotation( Reference.class );
                
                return
                (
                    referenceAnnotation != null &&
                    referenceAnnotation.target() == JavaType.class &&
                    property.element().adapt( IJavaProject.class ) != null
                );
            }
            
            return false;
        }
    }

}