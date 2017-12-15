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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.ListPropertyCustomBinding;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.SapphirePartDefHintsListController;
import org.eclipse.sapphire.ui.def.internal.SapphirePartDefMethods;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphirePartDef

    extends IModelElementForXml, IRemovable
    
{
    ModelElementType TYPE = new ModelElementType( ISapphirePartDef.class );
    
    String HINT_EXPAND_VERTICALLY = "expand.vertically";
    String HINT_HIDE_IF_DISABLED = "hide.if.disabled";
    String HINT_WIDTH = "width";
    String HINT_HEIGHT = "height";
    String HINT_PREFER_FORM_STYLE = "prefer.form.style";
    
    // *** HelpContextId ***
    
    @Label( standard = "help context id" )
    @XmlBinding( path = "help-context-id" )
    
    ValueProperty PROP_HELP_CONTEXT_ID = new ValueProperty( TYPE, "HelpContextId" );
    
    Value<String> getHelpContextId();
    void setHelpContextId( String helpContextId );
    
    // *** Hints ***
    
    @Label( standard = "hints" )
    @Type( base = ISapphireHint.class )
    @ListPropertyCustomBinding( impl = SapphirePartDefHintsListController.class )
    
    ListProperty PROP_HINTS = new ListProperty( TYPE, "Hints" );
    
    ModelElementList<ISapphireHint> getHints();
    
    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    String getHint( String name );
    
    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    boolean getHint( String name,
                     boolean defaultValue );

    // *** Method : getHint ***
    
    @DelegateImplementation( SapphirePartDefMethods.class )
    
    int getHint( String name,
                 int defaultValue );
    
    // *** Listeners ***
    
    @Label( standard = "listeners" )
    @Type( base = ISapphirePartListenerDef.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "listener", type = ISapphirePartListenerDef.class ) } )
    
    ListProperty PROP_LISTENERS = new ListProperty( TYPE, "Listeners" );
    
    ModelElementList<ISapphirePartListenerDef> getListeners();
    
}
