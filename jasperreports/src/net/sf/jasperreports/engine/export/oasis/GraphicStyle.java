/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2009 Jaspersoft Corporation. All rights reserved.
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
package net.sf.jasperreports.engine.export.oasis;

import java.io.IOException;
import java.io.Writer;

import net.sf.jasperreports.engine.JRAlignment;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRPen;
import net.sf.jasperreports.engine.JRPrintGraphicElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id$
 */
public class GraphicStyle extends Style
{
	/**
	 *
	 */
	private String backcolor = null;
	private String forecolor = null;
	private String style = null;
	private String width = null;
	private String hAlign = null;
	private String vAlign = null;


	/**
	 *
	 */
	public GraphicStyle(Writer styleWriter, JRPrintGraphicElement element)
	{
		super(styleWriter);

		if (element.getMode() == ModeEnum.OPAQUE.getValue())
		{
			//fill = "solid";
			backcolor = JRColorUtil.getColorHexa(element.getBackcolor());
		}
		else
		{
			//fill = "none";
		}

		forecolor = JRColorUtil.getColorHexa(element.getLinePen().getLineColor());

		double doubleWidth = element.getLinePen().getLineWidth().doubleValue();
		if (doubleWidth < 0)
		{
			style = "none";
		}
		else
		{
			switch (element.getLinePen().getLineStyle().byteValue())
			{
				case JRPen.LINE_STYLE_DOTTED : //FIXMEBORDER
				case JRPen.LINE_STYLE_DASHED :
				{
					style = "dash";
					break;
				}
				case JRPen.LINE_STYLE_SOLID :
				default :
				{
					style = "solid";
					break;
				}
			}
		}

		width = String.valueOf(Utility.translatePixelsToInchesWithNoRoundOff(doubleWidth));
		byte horizontalAlignment = JRAlignment.HORIZONTAL_ALIGN_LEFT;
		byte verticalAlignment = JRAlignment.VERTICAL_ALIGN_TOP;

		if(element instanceof JRPrintImage)
		{
			JRPrintImage imageElement = (JRPrintImage)element;
			horizontalAlignment = imageElement.getHorizontalAlignment();
			verticalAlignment = imageElement.getVerticalAlignment();
		}

		switch(horizontalAlignment)
		{
			case JRAlignment.HORIZONTAL_ALIGN_RIGHT:
			{
				hAlign = "right";
				break;
			}
			case JRAlignment.HORIZONTAL_ALIGN_JUSTIFIED:
			{
				hAlign = "justified";
				break;
			}
			case JRAlignment.HORIZONTAL_ALIGN_CENTER:
			{
				hAlign = "center";
				break;
			}
			case JRAlignment.HORIZONTAL_ALIGN_LEFT:
			default:
			{
				hAlign = "left";
				break;
			}
		}

		switch(verticalAlignment)
		{
			case JRAlignment.VERTICAL_ALIGN_BOTTOM:
			{
				vAlign = "bottom";
				break;
			}
			case JRAlignment.VERTICAL_ALIGN_MIDDLE:
			{
				vAlign = "middle";
				break;
			}
			case JRAlignment.VERTICAL_ALIGN_TOP:
			default:
			{
				vAlign = "top";
				break;
			}

		}
	}

	/**
	 *
	 */
	public String getId()
	{
		//return fill + "|" + backcolor
		StringBuffer id = new StringBuffer();
		id.append(backcolor);
		id.append("|");
		id.append(forecolor);
		id.append("|");
		id.append(style);
		id.append("|");
		id.append(width);
		id.append("|");
		id.append(hAlign);
		id.append("|");
		id.append(vAlign);
		return id.toString();
	}

	/**
	 *
	 */
	public void write(String lineStyleName) throws IOException
	{
		styleWriter.write(" <style:style style:name=\"" + lineStyleName + "\"");
		styleWriter.write(" style:family=\"graphic\" style:parent-style-name=\"Graphics\">\n");
		styleWriter.write("   <style:graphic-properties");
		styleWriter.write(" draw:fill-color=\"#" + backcolor + "\"");
		styleWriter.write(" style:horizontal-pos=\""+hAlign+ "\" style:horizontal-rel=\"paragraph\"");
		styleWriter.write(" style:vertical-pos=\""+vAlign+ "\" style:vertical-rel=\"paragraph\"");
		styleWriter.write(" svg:stroke-color=\"#" + forecolor + "\"");
		styleWriter.write(" draw:stroke=\"" + style + "\"");//FIXMENOW dashed borders do not work; only dashed lines and ellipses seem to work
		styleWriter.write(" draw:stroke-dash=\"Dashed\"");
		styleWriter.write(" svg:stroke-width=\"" + width + "in\"");
		styleWriter.write("/>\n");
		styleWriter.write("</style:style>\n");
	}

}

