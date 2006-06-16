/*
 * $Id:$
 *
 * Copyright (c) 2005 Sascha Ledinsky
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
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.entity;

/**
 * @author sascha
 *
 */
public interface Animatable {
	/** gets the names of all available channels */
	public String[] getChannels();
	
	/** sets the channel with the specified channelNumber to the specified value */
	public void setChannel(int channelNumber, double value);
	
	/** gets the value of the channel with the specified channelNumber*/
	public double getChannel(int channelNumber);
	
	/** sets the motioncurve for the channel with the specified channelNumber*/
	public void setMotionCurveForChannel(int channelNumber, MotionCurveNew motionCurve);
	
	/** gets the motioncurve for the channel with the specified channelNumber*/
	public MotionCurveNew getMotionCurveForChannel(int channelNumber);
	
	/** updates all channels */
	public void update(double position);
	
	/**
	 * gets the number of the channel with the specified name
	 * @param the name of the channel to search for
	 * @return the channel number, or -1 if the channel does not exist
	 */
	public int getChannelNumberByName(String name);
	
	/** gets the id of the Animatable (needed to bind a curve to an animatable when loading from xml)*/
	public String getId();
}
