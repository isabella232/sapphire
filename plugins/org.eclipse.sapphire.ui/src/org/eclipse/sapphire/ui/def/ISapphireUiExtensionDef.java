/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [8447730] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "extension", namespace = "http://www.eclipse.org/sapphire/xmlns/extension" )

public interface ISapphireUiExtensionDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireUiExtensionDef.class );
    
    // *** Actions ***
    
    @Type( base = ISapphireActionDef.class )
    @Label( standard = "action" )
    @Documentation( content = "Actions that have been contributed via the extension point." )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action", type = ISapphireActionDef.class ) )
    
    ListProperty PROP_ACTIONS = new ListProperty( TYPE, "Actions" );
    
    ModelElementList<ISapphireActionDef> getActions();
    
    // *** ActionHandlers ***
    
    @Type( base = ISapphireActionHandlerDef.class )
    @Label( standard = "action handlers" )
    @Documentation( content = "Action handlers that have been contributed via the extension point." )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action-handler", type = ISapphireActionHandlerDef.class ) )
    
    ListProperty PROP_ACTION_HANDLERS = new ListProperty( TYPE, "ActionHandlers" );
    
    ModelElementList<ISapphireActionHandlerDef> getActionHandlers();
    
    // *** ActionHandlerFactories ***
    
    @Type( base = ISapphireActionHandlerFactoryDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "action-handler-factory", type = ISapphireActionHandlerFactoryDef.class ) )
    @Label( standard = "action handler factories" )
    
    ListProperty PROP_ACTION_HANDLER_FACTORIES = new ListProperty( TYPE, "ActionHandlerFactories" );
    
    ModelElementList<ISapphireActionHandlerFactoryDef> getActionHandlerFactories();
    
}
