/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ConnectionEditorLocator implements CellEditorLocator {
	
	private Label label;
	private DiagramConnectionLabelEditPart source;

	/**
	 * Creates a new ActivityCellEditorLocator for the given Label
	 * 
	 * @param label
	 *            the Label
	 */
	public ConnectionEditorLocator(DiagramConnectionLabelEditPart source, Label label) {
		setLabel(label);
		this.source = source;
	}

	/**
	 * @see CellEditorLocator#relocate(org.eclipse.jface.viewers.CellEditor)
	 */
	public void relocate(CellEditor celleditor) {
		IFigure sourceFigure = source.getFigure();
		Rectangle exposeRegion = sourceFigure.getBounds().getCopy();
		if (exposeRegion.width > 0 && exposeRegion.height > 0) {
			source.getViewer().reveal(source);
		}
		
		Text text = (Text) celleditor.getControl();
		Point pref = text.computeSize(-1, -1);
		Rectangle rect = label.getTextBounds().getCopy();
		label.translateToAbsolute(rect);
		text.setBounds(rect.x - 1, rect.y - 1, pref.x + 1, pref.y + 1);
	}

	/**
	 * Returns the Label figure.
	 * 
	 * @return the Label
	 */
	protected Label getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * 
	 * 
	 * @param label
	 *            The label to set
	 */
	protected void setLabel(Label label) {
		this.label = label;
	}

}
