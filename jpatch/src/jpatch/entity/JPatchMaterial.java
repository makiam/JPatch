/*
 * $Id: JPatchMaterial.java,v 1.5 2006/05/22 10:46:20 sascha_l Exp $
 *
 * Copyright (c) 2004 Sascha Ledinsky
 *
 * This file is part of JPatch.
 * 
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jpatch.entity;

import java.util.*;
import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.auxilary.*;

/**
 * This class contains a material-name, materialProperties and renderStrings.
 * It provides methods which returns string representations of the material
 * which can be used for rendering (POV-Ray, RenderMAN) or XML export.
 * <p>
 * The following variables may be used in renderStrings and will be substituted
 * by the materialProperty values:
 * <table>
 * <tr><th align="left">$name</th><td>The name of the material</td></tr>
 * <tr><th align="left">$r</th><td>The red component of the materials color 0..1</td></tr>
 * <tr><th align="left">$g</th><td>The green component of the materials color 0..1</td></tr>
 * <tr><th align="left">$b</th><td>The blue component of the materials color 0..1</td></tr>
 * <tr><th align="left">$filter</th><td>The amount of light which is being filtered through the material 0..1</td></tr>
 * <tr><th align="left">$transmit</th><td>The amount of light which will pass unfiltered through the material 0..1</td></tr>
 * <tr><th align="left">$opacity</th><td>The opacity values (RGB) of the material, used by RenderMAN shaders</td></tr>
 * <tr><th align="left">$ambient</th><td>The amount of ambient light 0..1</td></tr>
 * <tr><th align="left">$diffuse</th><td>The amount of diffuse light 0..1</td></tr>
 * <tr><th align="left">$brilliance</th><td>The brilliance value 0..&infin;</td></tr>
 * <tr><th align="left">$specular</th><td>The amount of specular light 0..&infin;</td></tr>
 * <tr><th align="left">$roughness</th><td>The roughness value 0..1</td></tr>
 * <tr><th align="left">$metallic</th><td>The metallic value 0..1</td></tr>
 * <tr><th align="left">$reflection</th><td>The amount of reflection 0..1</td></tr>
 * <tr><th align="left">$reflection_min</th><td>The minimum reflection (used for variable reflection) 0..reflection_max</td></tr>
 * <tr><th align="left">$reflection_max</th><td>The maximum reflection (used for variable reflection) reflection_min..1</td></tr>
 * <tr><th align="left">$reflection_falloff</th><td>The exponent of variable reflection 0..&infin;</td></tr>
 * <tr><th align="left">$refraction</th><td>The index of refraction 1..&infin;</td></tr>
 * <tr><th align="left">$conserve_energy</th><td>The converse energy flag ture|false</td></tr>
 * </table>
 *
 * @author     Sascha Ledinsky
 * @version    $Revision: 1.5 $
 * @see		jpatch.entity.MaterialProperties
 */

public class JPatchMaterial extends JPatchTreeLeaf {
	private RenderExtension re = new RenderExtension(new String[] {
		"povray", "pigment {\n\tcolor rgbft <$r,$g,$b,$filter,$transmit>\n}\nfinish {\n\tambient $ambient\n\tdiffuse $diffuse brilliance $brilliance\n\tspecular $specular roughness $roughness metallic $metallic\n\t#if ($conserve_energy) conserve_energy #end\n\treflection {\n\t\t$reflection_min,$reflection_max\n\t\tfalloff $reflection_falloff\n\t\tmetallic $metallic\n\t}\n}\ninterior {\n\tior $refraction\n}\n",
		"renderman", "Color [$r $g $b]\nOpacity [$opacity]\nSurface \"plastic\" \"Ka\" [$ambient] \"Kd\" [$diffuse] \"Ks\" [$specular] \"roughness\" [$roughness]\n",
		"inyo",""
	});
	/** materialProperties */
	private MaterialProperties materialProperties = new MaterialProperties();
	
	private int iXmlNumber;
	
	/**
	 * Default Constructor
	 */
	public JPatchMaterial() {
		strName = "New Material";
	}

	/**
	 * Constructor
	 *
	 * @param color	The color of the material
	 */
	public JPatchMaterial(Color3f color) {
		this();
		materialProperties.red = color.x;
		materialProperties.green = color.y;
		materialProperties.blue = color.z;
	}

	
	/**
	 * sets the color of the material
	 *
	 * @param color The color to set
	 */
	public void setColor(Color3f color) {
		materialProperties.red = color.x;
		materialProperties.green = color.y;
		materialProperties.blue = color.z;
	}

	public void setXmlNumber(int n) {
		iXmlNumber = n;
	}
	
	public int getXmlNumber() {
		return iXmlNumber;
	}
	
	public void setRenderString(String format, String version, String renderString) {
		re.setRenderString(format, version, renderString);
	}
	
	public String getRenderString(String format, String version) {
		return re.getRenderString(format, version);
	}
	
	public StringBuffer renderStrings(String prefix) {
		return re.xml(prefix);
	}

	public Map getRenderStrings() {
		return re.getMap();
	}
	
	public void setRenderStrings(Map map) {
		re.setMap(map);
	}
	
	/**
	 * Get XML representation of material
	 *
	 * @param tabs The indent level
	 */
	public StringBuffer xml(String prefix) {
		String prefix2 = prefix + "\t";
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("<material>").append("\n");
		sb.append(prefix).append("\t<name>").append(strName).append("</name>").append("\n");
		sb.append(materialProperties.xml(prefix2));
		sb.append(renderStrings(prefix2));
		sb.append(prefix).append("</material>").append("\n");
		return sb;
	}

	/**
	 * Get MaterialProperties of this Material
	 *
	 * @return The MatreialProperties of this Material
	 */
	public MaterialProperties getMaterialProperties() {
		return materialProperties;
	}
}
