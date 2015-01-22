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

package org.eclipse.sapphire.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.ui.internal.ExportModelDocumentationOpMethods;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface IExportModelDocumentationOp extends Element
{
    ElementType TYPE = new ElementType( IExportModelDocumentationOp.class );
    
    // *** CreateFinishedDocument ***
    
    @Type( base = Boolean.class )
    @Label( standard = "create finished document" )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_CREATE_FINISHED_DOCUMENT = new ValueProperty( TYPE, "CreateFinishedDocument" );
    
    Value<Boolean> getCreateFinishedDocument();
    void setCreateFinishedDocument( String value );
    void setCreateFinishedDocument( Boolean value );
    
    // *** DocumentTitle ***
    
    @Label( standard = "document title" )
    @DefaultValue( text = "Sapphire Extensions" )
    
    ValueProperty PROP_DOCUMENT_TITLE = new ValueProperty( TYPE, "DocumentTitle" );
    
    Value<String> getDocumentTitle();
    void setDocumentTitle( String value );
    
    // *** DocumentBodyTitle ***
    
    @Label( standard = "document body title" )
    
    ValueProperty PROP_DOCUMENT_BODY_TITLE = new ValueProperty( TYPE, "DocumentBodyTitle" );
    
    Value<String> getDocumentBodyTitle();
    void setDocumentBodyTitle( String value );
    
    // *** EmbedDefaultStyle ***
    
    @Type( base = Boolean.class )
    @Label( standard = "embed default style" )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_EMBED_DEFAULT_STYLE = new ValueProperty( TYPE, "EmbedDefaultStyle" );
    
    Value<Boolean> getEmbedDefaultStyle();
    void setEmbedDefaultStyle( String value );
    void setEmbedDefaultStyle( Boolean value );

    // *** Method: execute ***
    
    @DelegateImplementation( ExportModelDocumentationOpMethods.class )
    
    String execute( ElementType type,
                    IProgressMonitor monitor );
    
}
