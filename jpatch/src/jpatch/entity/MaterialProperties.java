package jpatch.entity;

import java.awt.*;
import jpatch.auxilary.*;

public class MaterialProperties {
	/** default value for red */
	private static final float RED = 1;
	/** default value for green */
	private static final float GREEN = 0;
	/** default value for blue */
	private static final float BLUE = 0;
	/** default value for transmit */
	private static final float TRANSMIT = 0;
	/** default value for filter */
	private static final float FILTER = 0;
	/** default value for ambient */
	private static final float AMBIENT = 0.1f;
	/** default value for diffuse */
	private static final float DIFFUSE = 0.9f;
	/** default value for brilliance */
	private static final float BRILLIANCE = 1;
	/** default value for specular */
	private static final float SPECULAR = 1;
	/** default value for roughness */
	private static final float ROUGHNESS = 0.01f;
	/** default value for metallic */
	private static final float METALLIC = 0;
	/** default value for reflectionMin */
	private static final float REFLECTION_MIN = 0;
	/** default value for reflectionMAX */
	private static final float REFLECTION_MAX = 0;
	/** default value for reflectionFalloff */
	private static final float REFLECTION_FALLOFF = 1;
	/** default value for red */
	private static final float REFRACTION = 1;
	/** default value for red */
	private static final boolean CONSERVE_ENERGY = true;
	
	/** red component of RGB color */
	public float red;
	/** green component of RGB color */
	public float green;
	/** blue component of RGB color */
	public float blue;
	/** transmit value 0 = opaque, 1 = completely transparent */
	public float transmit;
	/** filter value 0 = opaque, 1 = filter through RGB color */
	public float filter;
	/** ambient value */
	public float ambient;
	/** diffuse value */
	public float diffuse;
	/** brilliance value */
	public float brilliance;
	/** specular value */
	public float specular;
	/** roughness value */
	public float roughness;
	/**
	 * metallic value<br>
	 * 0 = reflections and highlights unfiltered
	 * 1 = reflections and highlights filtered through RGB color
	 */
	public float metallic;
	/** reflection minumum */
	public float reflectionMin;
	/** reflection maximum */
	public float reflectionMax;
	/** reflection falloff exponent */
	public float reflectionFalloff;
	/**
	 * index of refraction
	 * air/vacuum = 0, water = 1.3, glass = 1.5, diamond = 2.6
	 */
	public float refraction;
	/**
	 * if true, the amount of reflected light is subtracted from light passing through (transmit and filter).<br>
	 * Only makes sense when using variable reflection
	 */
	public boolean conserveEnergy;
	
	/**
	 * Constructor
	 */
	public MaterialProperties() {
		reset();
	}
	
	public MaterialProperties(float r, float g, float b) {
		reset();
		red = r;
		green = g;
		blue = b;
	}
	
	/**
	 * Create new MaterialProperties with parameters from given MaterialProperties
	 *
	 * @param m MaterialProperties to copy
	 */
	public MaterialProperties(MaterialProperties m) {
		set(m);
	}
	
	public int getRGB() {
		return 0xFF000000 | (int) (red * 192) << 16 | (int) (green * 192) << 8 | (int) (blue * 192);
	}
	
	public Color getColor() {
		return new Color(red, green, blue);
	}
	
	/**
	 * reset all attributes to their default values
	 */
	public void reset() {
		red = RED;
		green = GREEN;
		blue = BLUE;
		transmit = TRANSMIT;
		filter = FILTER;
		ambient = AMBIENT;
		diffuse = DIFFUSE;
		brilliance = BRILLIANCE;
		specular = SPECULAR;
		roughness = ROUGHNESS;
		metallic = METALLIC;
		reflectionMin = REFLECTION_MIN;
		reflectionMax = REFLECTION_MAX;
		reflectionFalloff = REFLECTION_FALLOFF;
		refraction = REFRACTION;
		conserveEnergy = CONSERVE_ENERGY;
	}
	
	/**
	 * set all attributes as in MaterialProperties
	 *
	 * @param m MaterialProperties to copy
	 */
	public void set(MaterialProperties m) {
		red = m.red;
		green = m.green;
		blue = m.blue;
		transmit = m.transmit;
		filter = m.filter;
		ambient = m.ambient;
		diffuse = m.diffuse;
		brilliance = m.brilliance;
		specular = m.specular;
		roughness = m.roughness;
		metallic = m.metallic;
		reflectionMin = m.reflectionMin;
		reflectionMax = m.reflectionMax;
		reflectionFalloff = m.reflectionFalloff;
		refraction = m.refraction;
		conserveEnergy = m.conserveEnergy;
	}
	
	public float getAlpha() {
		return 1 - Math.max(transmit, filter);
	}
	
	public boolean isOpaque() {
		return (transmit == 0 && filter == 0);
	}
	
	/**
	 * compute opacity value
	 *
	 * @return opacity for red channel
	 */
	public float opacityRed() {
		return 1f - transmit - red * filter;
	}
	
	/**
	 * compute opacity value
	 *
	 * @return opacity for green channel
	 */
	public float opacityGreen() {
		return 1f - transmit - green * filter;
	}
	
	/**
	 * compute opacity value
	 *
	 * @return opacity for blue channel
	 */
	public float opacityBlue() {
		return 1f - transmit - blue * filter;
	}
	
	/**
	 * compute opacity value
	 * @return opacity string for RenderMan
	 */
	public String opacity() {
		return "" + opacityRed() + " " + opacityGreen() + " " + opacityBlue();
	}
	
	/**
	 * compute reflection (to use if variable reflection is not supported)
	 * @return average reflection
	 */
	public float reflection() {
		return 0.5f * reflectionMin + 0.5f * reflectionMax;
	}
	
	/**
	 * Output in XML format
	 * @return MaterialProperties in XML format
	 */
	public StringBuffer xml(String prefix) {
		StringBuffer sb = new StringBuffer();
		StringBuffer sbColor = colorString();
		StringBuffer sbFinish = finishString();
		StringBuffer sbReflection = reflectionString();
		StringBuffer sbRefraction = refractionString();
		
		sb.append(prefix).append(sbColor).append("\n");
		if (sbFinish != null) {
			sb.append(prefix).append(sbFinish).append("\n");
		}
		if (sbReflection != null) {
			sb.append(prefix).append(sbReflection).append("\n");
		}
		if (sbRefraction != null) {
			sb.append(prefix).append(sbRefraction).append("\n");
		}
		return sb;
	}
	
	/* private methods used for XML output */
	
	private StringBuffer colorString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<color r=").append(XMLutils.quote(red)).append(" g=").append(XMLutils.quote(green)).append(" b=").append(XMLutils.quote(blue));
		if (transmit != TRANSMIT) {
			sb.append(" transmit=").append(XMLutils.quote(transmit));
		}
		if (filter != FILTER) {
			sb.append(" filter=").append(XMLutils.quote(filter));
		}
		sb.append("/>");
		return sb;
	}
	private StringBuffer finishString() {
		StringBuffer sb = new StringBuffer();
		boolean def = true;
		sb.append("<finish");
		if (ambient != AMBIENT) {
			sb.append(" ambient=").append(XMLutils.quote(ambient));
			def = false;
		}
		if (diffuse != DIFFUSE) {
			sb.append(" diffuse=").append(XMLutils.quote(diffuse));
			def = false;
		}
		if (brilliance != BRILLIANCE) {
			sb.append(" brilliance=").append(XMLutils.quote(brilliance));
			def = false;
		}
		if (specular != SPECULAR) {
			sb.append(" specular=").append(XMLutils.quote(specular));
			def = false;
		}
		if (roughness != ROUGHNESS) {
			sb.append(" roughness=").append(XMLutils.quote(roughness));
			def = false;
		}
		if (metallic != METALLIC) {
			sb.append(" metallic=").append(XMLutils.quote(metallic));
			def = false;
		}
		sb.append("/>");
		return (def) ? null : sb;
	}
	private StringBuffer reflectionString() {
		StringBuffer sb = new StringBuffer();
		boolean def = true;
		sb.append("<reflection");
		if (reflectionMin == reflectionMax && reflectionMin != REFLECTION_MIN) {
			sb.append(" amount=").append(XMLutils.quote(reflectionMin));
			def = false;
		} else {
			if (reflectionMin != REFLECTION_MIN) {
				sb.append(" min=").append(XMLutils.quote(reflectionMin));
				def = false;
			}
			if (reflectionMax != REFLECTION_MAX) {
				sb.append(" max=").append(XMLutils.quote(reflectionMax));
				def = false;
			}
			if (reflectionFalloff != REFLECTION_FALLOFF) {
				sb.append(" falloff=").append(XMLutils.quote(reflectionFalloff));
				def = false;
			}
		}
		sb.append("/>");
		return (def) ? null : sb;
	}
	private StringBuffer refractionString() {
		StringBuffer sb = new StringBuffer();
		boolean def= true;
		sb.append("<refraction");
		if (refraction != REFRACTION) {
			sb.append(" index=").append(XMLutils.quote(refraction));
			def = false;
		}
		sb.append("/>");
		return (def) ? null : sb;
	}
}
