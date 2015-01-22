/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionLabelModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ImageModel;
import org.eclipse.sapphire.ui.swt.gef.model.LineShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.RectangleModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeFactoryModel;
import org.eclipse.sapphire.ui.swt.gef.model.SpacerModel;
import org.eclipse.sapphire.ui.swt.gef.model.TextModel;
import org.eclipse.sapphire.ui.swt.gef.model.ValidationMarkerModel;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireDiagramEditorEditPartFactory implements EditPartFactory {

	private DiagramConfigurationManager configManager;
	
	public SapphireDiagramEditorEditPartFactory(DiagramConfigurationManager configManager) {
		this.configManager = configManager;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart,
	 * java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object modelElement) {
		// get EditPart for model element
		EditPart part = getPartForElement(modelElement);
		// store model element in EditPart
		part.setModel(modelElement);
		return part;
	}

	/**
	 * Maps an object to an EditPart.
	 * 
	 * @throws RuntimeException
	 *             if no match was found (programming error)
	 */
	private EditPart getPartForElement(Object model) {
		if (model instanceof DiagramModel) {
			return new SapphireDiagramEditorPageEditPart(this.configManager);
		}
		if (model instanceof DiagramNodeModel) {
			return new DiagramNodeEditPart(this.configManager);
		}
		if (model instanceof TextModel) {
			return new TextEditPart(this.configManager);
		}
		if (model instanceof ImageModel) {
			return new ImageEditPart(this.configManager);
		}
		if (model instanceof LineShapeModel) {
			return new LineShapeEditPart(this.configManager);
		}
		if (model instanceof ValidationMarkerModel) {
			return new ValidationMarkerEditPart(this.configManager);
		}
		if (model instanceof RectangleModel) {
			return new ContainerShapeEditPart(this.configManager);
		}
		if (model instanceof ShapeFactoryModel) {
			return new ShapeFactoryEditPart(this.configManager);
		}
		if (model instanceof SpacerModel) {
			return new SpacerEditPart(this.configManager);
		}
		if (model instanceof DiagramConnectionModel) {
			return new DiagramConnectionEditPart(this.configManager);
		}
		if (model instanceof DiagramConnectionLabelModel) {
			return new DiagramConnectionLabelEditPart(this.configManager);
		}
		throw new RuntimeException("Can't create part for model: "
				+ ((model != null) ? model.getClass().getName()
						: "null"));
	}

}