/*
 * *****************************************************************************
 * Copyright (C) 2014-2022 Dennis Sheirer
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
 * ****************************************************************************
 */

package io.github.dsheirer.module.decode.dmr.message;

import io.github.dsheirer.bits.CorrectedBinaryMessage;
import io.github.dsheirer.identifier.Identifier;
import io.github.dsheirer.module.decode.dmr.sync.DMRSyncPattern;
import java.util.Collections;
import java.util.List;

/**
 * Unknown DMR message used as a default case when the message type cannot be determined.
 */
public class UnknownDMRMessage extends DMRBurst
{
    /**
     * Constructs an instance
     *
     * @param syncPattern
     * @param message containing 288-bit DMR message with preliminary bit corrections indicated.
     * @param cach
     * @param timestamp
     * @param timeslot
     */
    public UnknownDMRMessage(DMRSyncPattern syncPattern, CorrectedBinaryMessage message, CACH cach, long timestamp, int timeslot)
    {
        super(syncPattern, message, cach, timestamp, timeslot);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if(hasCACH())
        {
            sb.append(getCACH().toString());
        }
        else
        {
            sb.append("TS").append(getTimeslot());
        }
        sb.append(" ").append(getSyncPattern());
        sb.append(" UNKNOWN DMR BURST");
        sb.append(" ").append(getMessage().toHexString());
        return sb.toString();
    }

    @Override
    public List<Identifier> getIdentifiers()
    {
        return Collections.emptyList();
    }
}
