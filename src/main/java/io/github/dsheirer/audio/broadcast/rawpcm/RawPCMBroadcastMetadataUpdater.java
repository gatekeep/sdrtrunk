/*
 * *****************************************************************************
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
 * ****************************************************************************
 */
package io.github.dsheirer.audio.broadcast.rawpcm;

import io.github.dsheirer.alias.AliasModel;
import io.github.dsheirer.audio.broadcast.IBroadcastMetadataUpdater;
import io.github.dsheirer.identifier.IdentifierCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawPCMBroadcastMetadataUpdater implements IBroadcastMetadataUpdater
{
    private final static Logger mLog = LoggerFactory.getLogger(RawPCMBroadcastMetadataUpdater.class);

    private RawPCMConfiguration mConfiguration;
    private AliasModel mAliasModel;

    public RawPCMBroadcastMetadataUpdater(RawPCMConfiguration configuration, AliasModel aliasModel)
    {
        mConfiguration = configuration;
        mAliasModel = aliasModel;
    }

    public void update(IdentifierCollection identifierCollection)
    {
        return; // stub
    }

}
