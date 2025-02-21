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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.FastMath;

public class PCMFrameTools {

    public final static int PCM_SAMPLE_LENGTH = 320;
    private final static int SILENCE_FRAME_CNT = 6;

    private static byte[] mSilenceFrame = new byte[PCM_SAMPLE_LENGTH];

    private PCMFrameTools() {}

    /**
     * Split PCM audio at frame boundaries
     * @param input byte array of audio
     * @return list of byte arrays containing audio frames
     */
    public static PCMAudioFrames split(byte[] input)
    {
        List<byte[]> frames = new ArrayList<>();
        int audioDuration = 0;

        // silence leader pad (injects ~125ms of silence at the beginning of the recording)
        for (int i = 0; i < SILENCE_FRAME_CNT; i++) {
            frames.add(mSilenceFrame);
            audioDuration += (int) (((float) PCM_SAMPLE_LENGTH / (float) 8000 / (float) 1) * 1000);
        }

        int offset = 0;
        while(offset < input.length)
        {
            frames.add(Arrays.copyOfRange(input, offset, offset + FastMath.min(PCM_SAMPLE_LENGTH, input.length - offset)));
            audioDuration += (int) (((float) PCM_SAMPLE_LENGTH / (float) 8000 / (float) 1) * 1000);
            offset += PCM_SAMPLE_LENGTH;
        }

        // silence follower pad (injects ~125ms of silence at the end of the recording)
        for (int i = 0; i < SILENCE_FRAME_CNT; i++) {
            frames.add(mSilenceFrame);
            audioDuration += (int) (((float) PCM_SAMPLE_LENGTH / (float) 8000 / (float) 1) * 1000);
        }

        return new PCMAudioFrames(audioDuration, frames);
    }

}
