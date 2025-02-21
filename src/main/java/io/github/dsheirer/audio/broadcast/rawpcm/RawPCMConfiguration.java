/*******************************************************************************
 * sdrtrunk
 * Copyright (C) 2025 Bryan Biedenkapp, N2PLL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 ******************************************************************************/
package io.github.dsheirer.audio.broadcast.rawpcm;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.dsheirer.audio.broadcast.BroadcastConfiguration;
import io.github.dsheirer.audio.broadcast.BroadcastFormat;
import io.github.dsheirer.audio.broadcast.BroadcastServerType;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class RawPCMConfiguration extends BroadcastConfiguration
{
    private final static Logger mLog = LoggerFactory.getLogger( RawPCMConfiguration.class );

    public RawPCMConfiguration()
    {
        this(BroadcastFormat.PCM);
    }

    /**
     * Raw PCM compatible configuration
     * @param format of audio
     */
    public RawPCMConfiguration(BroadcastFormat format)
    {
        /* ignore input format always use PCM */
        super(BroadcastFormat.PCM);

        mValid.bind(Bindings.and(Bindings.isNotNull(mHost), Bindings.greaterThan(mPort, 0)));
    }

    @Override
    public BroadcastConfiguration copyOf()
    {
        RawPCMConfiguration copy = new RawPCMConfiguration(getBroadcastFormat());

        //Broadcast Configuration Parameters
        copy.setName(getName());
        copy.setHost(getHost());
        copy.setPort(getPort());
        copy.setPassword(getPassword());
        copy.setDelay(getDelay());
        copy.setEnabled(false);

        return copy;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance")
    @Override
    public BroadcastServerType getBroadcastServerType()
    {
        return BroadcastServerType.RAWPCM_UDP;
    }
}
