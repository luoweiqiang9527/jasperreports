/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2022 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.engine.fill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintElementContainer;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.PrintElementId;
import net.sf.jasperreports.engine.base.JRVirtualPrintPage;
import net.sf.jasperreports.engine.base.VirtualizableElementList;
import net.sf.jasperreports.engine.util.VirtualizableElementCounter;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class VirtualizableFrame implements JRPrintElementContainer, OffsetElementsContainer
{

	private static final Log log = LogFactory.getLog(VirtualizableFrame.class);
	
	public static final String PROPERTY_FRAME_VIRTUALIZATION_ENABLED = 
			JRPropertiesUtil.PROPERTY_PREFIX + "frame.virtualization.enabled";

	private JRTemplatePrintFrame frame;
	private List<Object> elements;
	private int contentsWidth;
	private JRVirtualizationContext virtualizationContext;
	private JRVirtualPrintPage page;
	private int virtualizationPageElementSize;
	private int deepSize;

	public VirtualizableFrame(JRTemplatePrintFrame frame, 
			JRVirtualizationContext virtualizationContext, JRPrintPage page)
	{
		this.frame = frame;
		this.elements = new ArrayList<>();
		
		boolean virtualizationEnabled = virtualizationContext != null && page instanceof JRVirtualPrintPage
				&& JRPropertiesUtil.getInstance(virtualizationContext.getJasperReportsContext()).getBooleanProperty(
						PROPERTY_FRAME_VIRTUALIZATION_ENABLED, true);	
		if (virtualizationEnabled)
		{
			this.virtualizationContext = virtualizationContext;
			this.page = (JRVirtualPrintPage) page;
			this.virtualizationPageElementSize = virtualizationContext.getPageElementSize();
			this.deepSize = 0;
		}
		else
		{
			this.virtualizationPageElementSize = 0;
		}
	}
	
	@Override
	public void addElement(JRPrintElement element)
	{
		deepSize += VirtualizableElementCounter.count(element);
		
		this.elements.add(element);		
	}

	@Override
	public void addOffsetElements(Collection<? extends JRPrintElement> elements, int offsetX, int offsetY)
	{
		if (elements == null || elements.isEmpty())
		{
			// nothing to do
			return;
		}
		
		deepSize += VirtualizableElementCounter.count(elements);
		
		OffsetElements offsetElements = new OffsetElements(elements, offsetX, offsetY);
		this.elements.add(offsetElements);		
	}
	
	public void fill()
	{
		if (virtualizationPageElementSize > 0 && deepSize > virtualizationPageElementSize)
		{
			PrintElementId frameID = PrintElementId.forElement(frame);
			if (log.isDebugEnabled())
			{
				log.debug("creating virtualized list for frame " + frame.getUUID() + " (" + frameID + ")");
			}
			
			JRVirtualizationContext framesContext = virtualizationContext.getFramesContext();
			VirtualizableElementList virtualizableList = 
					new VirtualizableElementList(framesContext, page);
			frame.setElementsList(virtualizableList);
			framesContext.cacheVirtualizableList(frameID, virtualizableList);
		}
		
		//TODO lucian optimize case of a single subreport with zero offsets (e.g. a table)
		for (OffsetElementsIterator it = new OffsetElementsIterator(elements); it.hasNext();)
		{
			JRPrintElement element = it.next();
			frame.addElement(element);
		}
	}

	@Override
	public List<JRPrintElement> getElements()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getHeight()
	{
		return frame.getHeight();
	}

	@Override
	public void setHeight(int height)
	{
		frame.setHeight(height);
	}

	@Override
	public void setContentsWidth(int width)
	{
		this.contentsWidth = width;
	}
	
	public int getContentsWidth()
	{
		return contentsWidth;
	}
	
}
