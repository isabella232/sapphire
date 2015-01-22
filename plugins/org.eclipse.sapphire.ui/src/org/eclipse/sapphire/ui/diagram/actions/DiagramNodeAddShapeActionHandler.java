/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - fixes to case lookup logic
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapeFactoryPart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class DiagramNodeAddShapeActionHandler extends SapphireActionHandler 
{
	private static final String ID_BASE = "Sapphire.Add.";
	private DiagramNodePart nodePart;
	private ShapeFactoryPart factory;
	private ElementType type;
	
	public DiagramNodeAddShapeActionHandler(DiagramNodePart nodePart, ShapeFactoryPart factory, ElementType type)
	{
		this.nodePart = nodePart;
		this.factory = factory;
		this.type = type;
	}
	
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
    	super.init(action, def);
    	setId( ID_BASE + this.type.getSimpleName());
    	setLabel( this.type.getLabel( true, CapitalizationType.NO_CAPS, false ) );
    	
		final ImageData typeSpecificAddImage = this.type.image();
		if (typeSpecificAddImage != null)
		{
			addImage(typeSpecificAddImage);
		}
    }
	
	@Override
	protected Object run(Presentation context) 
	{
	    final Element element = this.factory.getModelElementList().insert( this.type );
		final ShapePart shapePart = this.factory.getShapePart( element );
		SapphireDiagramEditorPagePart diagramPart = this.nodePart.nearest(SapphireDiagramEditorPagePart.class);
		if (shapePart.isEditable())
		{
			diagramPart.selectAndDirectEdit(shapePart);
		}
		return null;
	}

}
