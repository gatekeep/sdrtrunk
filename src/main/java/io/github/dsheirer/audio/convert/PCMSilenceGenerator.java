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
package io.github.dsheirer.audio.convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCMSilenceGenerator implements ISilenceGenerator
{
    private final static Logger mLog = LoggerFactory.getLogger(MP3SilenceGenerator.class);

    private InputAudioFormat mInputAudioFormat;
    private byte[] mSilenceFrame;
    private int mSilenceFrameDuration;

    /**
     * Generates PCM audio silence frames
     */
    public PCMSilenceGenerator(InputAudioFormat inputAudioFormat, MP3Setting setting)
    {
        mInputAudioFormat = inputAudioFormat;
        generate_frame();
        mLog.debug("constructed");
    }

    /**
     * Generates a single silence frame
     * @return
     */
    private void generate_frame()
    {
        mSilenceFrame = new byte[PCMFrameTools.PCM_SAMPLE_LENGTH];
        mSilenceFrameDuration = 20;
    }

    /**
     * Generates silence frames
     * @param duration_ms in milliseconds
     * @return
     */
    public PCMAudioFrames generate(long duration_ms)
    {
        List<byte[]> silenceFrames = new ArrayList<>();
        int silenceDuration = 0;
/*
        while(silenceDuration < duration_ms)
        {
            silenceFrames.add(mSilenceFrame);
            silenceDuration += mSilenceFrameDuration;
        }
*/
        return new PCMAudioFrames(silenceDuration, silenceFrames);
    }

    /**
     * Generates silence frames
     * @param numberOfFrames Number of silence frames
     * @return
     */
    public PCMAudioFrames generateEx(int numberOfFrames)
    {
        List<byte[]> silenceFrames = new ArrayList<>();
        int silenceDuration = 0, count = 0;

        while (count < numberOfFrames)
        {
            silenceFrames.add(mSilenceFrame);
            silenceDuration += mSilenceFrameDuration;
        }

        return new PCMAudioFrames(silenceDuration, silenceFrames);
    }
}
