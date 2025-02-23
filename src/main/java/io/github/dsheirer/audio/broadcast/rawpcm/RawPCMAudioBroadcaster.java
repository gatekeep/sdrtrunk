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
import io.github.dsheirer.audio.broadcast.AudioStreamingBroadcaster;
import io.github.dsheirer.audio.broadcast.BroadcastFormat;
import io.github.dsheirer.audio.broadcast.BroadcastState;
import io.github.dsheirer.audio.broadcast.IBroadcastMetadataUpdater;
import io.github.dsheirer.audio.convert.InputAudioFormat;
import io.github.dsheirer.audio.convert.MP3Setting;
import io.github.dsheirer.audio.convert.PCMFrameTools;
import io.github.dsheirer.identifier.Form;
import io.github.dsheirer.identifier.Identifier;
import io.github.dsheirer.identifier.IdentifierClass;
import io.github.dsheirer.identifier.IdentifierCollection;
import io.github.dsheirer.identifier.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.net.SMTPAppender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class RawPCMAudioBroadcaster extends AudioStreamingBroadcaster
{
    private final static Logger mLog = LoggerFactory.getLogger(RawPCMAudioBroadcaster.class);

    private RawPCMConfiguration mConfiguration;
    private IBroadcastMetadataUpdater mMetadataUpdater;
    protected AliasModel mAliasModel;

    private DatagramSocket mSocket;

    /**
     * Creates an Raw PCM compatible broadcaster using UDP.  This broadcaster is
     * compatible with dvmbridge.
     * @param configuration for the stream
     */
    public RawPCMAudioBroadcaster(RawPCMConfiguration configuration, InputAudioFormat inputAudioFormat,
                                      MP3Setting mp3Setting, AliasModel aliasModel)
    {
        configuration.setBroadcastFormat(BroadcastFormat.PCM); // force PCM always

        super(configuration, inputAudioFormat, mp3Setting);

        mConfiguration = configuration;
        try {
            mSocket = new DatagramSocket();
            setBroadcastState(BroadcastState.CONNECTED);
        } catch (IOException e) {
            mLog.error("Failed to setup raw PCM audio broadcaster", e);
        }
    }

    /**
     * Broadcasts the audio frame or sequence
     */
    @Override
    protected void broadcastAudio(byte[] audio, IdentifierCollection identifierCollection)
    {
        if (audio != null && audio.length > 0) {
            byte[] pkt = new byte[PCMFrameTools.PCM_SAMPLE_LENGTH + 12];
            int pktOffs = 0;
            int length = PCMFrameTools.PCM_SAMPLE_LENGTH;
            pkt[0] = (byte) ((length >> 24) & 255);
            pkt[1] = (byte) ((length >> 16) & 255);
            pkt[2] = (byte) ((length >> 8) & 255);
            pkt[3] = (byte) ((length >> 0) & 255);
            pktOffs += 4;

            System.arraycopy(audio, 0, pkt, pktOffs, PCMFrameTools.PCM_SAMPLE_LENGTH);
            pktOffs += PCMFrameTools.PCM_SAMPLE_LENGTH;

            Identifier to = null;
            if (identifierCollection != null)
                to = identifierCollection.getIdentifier(IdentifierClass.USER, Form.PATCH_GROUP, Role.TO);
            if (to == null && identifierCollection != null)
                to = identifierCollection.getIdentifier(IdentifierClass.USER, Form.TALKGROUP, Role.TO);

            Identifier from = null;
            if (identifierCollection != null)
                from = identifierCollection.getIdentifier(IdentifierClass.USER, Form.RADIO, Role.FROM);

            int dstId = 0;
            if (to != null)
                dstId = (int) to.getValue();

            pkt[0 + pktOffs] = (byte) ((dstId >> 24) & 255);
            pkt[1 + pktOffs] = (byte) ((dstId >> 16) & 255);
            pkt[2 + pktOffs] = (byte) ((dstId >> 8) & 255);
            pkt[3 + pktOffs] = (byte) ((dstId >> 0) & 255);
            pktOffs += 4;

            int srcId = 0;
            if (from != null)
                srcId = (int) from.getValue();

            pkt[0 + pktOffs] = (byte) ((srcId >> 24) & 255);
            pkt[1 + pktOffs] = (byte) ((srcId >> 16) & 255);
            pkt[2 + pktOffs] = (byte) ((srcId >> 8) & 255);
            pkt[3 + pktOffs] = (byte) ((srcId >> 0) & 255);

            try {
                DatagramPacket packet = new DatagramPacket(pkt, pkt.length,
                        InetAddress.getByName(mConfiguration.getHost()), mConfiguration.getPort());
                //mLog.debug("Sending audio packet to {}:{}, len {}, srcId {}, dstId {}", mConfiguration.getHost(),
                //        mConfiguration.getPort(), pkt.length, srcId, dstId);
                mSocket.send(packet);
            } catch (IOException e) {
                mLog.error("Failed to send audio packet", e);
            }
        }
    }

    /**
     * Disconnect from the remote broadcast server and cleanup input/output streams and socket connection
     */
    public void disconnect()
    {
        return; // stub
    }

    /**
     * Broadcast configuration
     */
    protected RawPCMConfiguration getConfiguration()
    {
        return (RawPCMConfiguration) getBroadcastConfiguration();
    }

    @Override
    protected IBroadcastMetadataUpdater getMetadataUpdater()
    {
        if(mMetadataUpdater == null)
        {
            mMetadataUpdater = new RawPCMBroadcastMetadataUpdater(getConfiguration(), mAliasModel);
        }

        return mMetadataUpdater;
    }
}
