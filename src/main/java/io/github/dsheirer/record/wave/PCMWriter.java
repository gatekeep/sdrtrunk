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
package io.github.dsheirer.record.wave;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;

public class PCMWriter implements AutoCloseable
{
    private final static Logger mLog = LoggerFactory.getLogger(WaveWriter.class);

    private static final Pattern FILENAME_PATTERN = Pattern.compile("(.*_)(\\d+)(\\.tmp)");
    public static final long MAX_SIZE = 2l * (long)Integer.MAX_VALUE;

    private AudioFormat mAudioFormat;
    private int mFileRolloverCounter = 1;
    private long mMaxSize;
    private Path mFile;
    private FileChannel mFileChannel;
    private boolean mDataChunkOpen = false;

    /**
     * Constructs a new PCM writer that is for writing buffers of PCM sample data.
     *
     * Each time the maximum file size is reached, a new file is created with a
     * series suffix appended to the file name.
     *
     * @param format - audio format (channels, sample size, sample rate)
     * @param file - wave file to write
     * @param maxSize - maximum file size ( range: 1 - 4,294,967,294 bytes )
     * @throws IOException - if there are any IO issues
     */
    public PCMWriter(AudioFormat format, Path file, long maxSize) throws IOException
    {
        Validate.isTrue(format != null);
        Validate.isTrue(file != null);

        mAudioFormat = format;
        mFile = file;

        if(0 < maxSize && maxSize <= MAX_SIZE)
        {
            mMaxSize = maxSize;
        }
        else
        {
            mMaxSize = MAX_SIZE;
        }

        open();
    }

    /**
     * Constructs a new PCM writer that is for writing buffers of PCM sample data.  
     * The maximum file size is limited to the max size specified in the wave file format: max unsigned integer
     *
     * @param format - audio format (channels, sample size, sample rate)
     * @param file - wave file to write
     * @throws IOException - if there are any IO issues
     */
    public PCMWriter(AudioFormat format, Path file) throws IOException
    {
        this(format, file, 0);
    }

    /**
     * Opens the file.
     */
    private void open() throws IOException
    {
        int version = 2;

        while(Files.exists(mFile) && version < 20)
        {
            mFile = Paths.get(mFile.toFile().getAbsolutePath().replace(".tmp", "_" + version + ".tmp"));
            version++;
        }

        if(version >= 20)
        {
            throw new IOException("Unable to create a unique file name for recording - exceeded 20 versioning attempts");
        }

        mFileChannel = (FileChannel.open(mFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW));
    }

    /**
     * Implements the auto-closeable interface.  Closes the recording without any renaming operation.
     * @throws IOException if there is an error closing the file channel.
     */
    public void close() throws IOException
    {
        close(null);
    }

    /**
     * Closes the file and renames/moves the contents to the specified path
     */
    public void close(Path path) throws IOException
    {
        mFileChannel.force(true);
        mFileChannel.close();

        rename(path);
    }

    /**
     * Renames the file to the specified filename moving it to the from *.tmp to *.pcm after file has been closed.
     *
     * @throws IOException
     */
    private void rename(Path path) throws IOException
    {
        if(mFile != null && Files.exists(mFile) && path != null)
        {
            if(Files.exists(path))
            {
                mLog.warn("Duplicate recording file detected - ignoring [" + path + "]");
                Files.delete(mFile);
            }
            else
            {
                Files.move(mFile, path);
            }
        }
    }

    /**
     * Writes the buffer contents to the file.  Assumes that the buffer is full
     * and the first byte of data is at position 0.
     */
    public void writeData(ByteBuffer buffer) throws IOException
    {
        buffer.position(0);

        openDataChunk();

        /* Write the full buffer if there is room, respecting the max file size */
        if(mFileChannel.size() + buffer.capacity() < mMaxSize)
        {
            while(buffer.hasRemaining())
            {
                mFileChannel.write(buffer);
            }
        }
        else
        {
            /* Split the buffer to finish filling the current file and then put
             * the leftover into a new file */
            int remaining = (int)(mMaxSize - mFileChannel.size());

            /* Ensure we write full frames to fill up the remaining size */
            remaining -= (int)(remaining % mAudioFormat.getFrameSize());

            byte[] bytes = buffer.array();

            ByteBuffer current = ByteBuffer.wrap(Arrays.copyOf(bytes, remaining));

            ByteBuffer next = ByteBuffer.wrap(Arrays.copyOfRange(bytes,
                remaining, bytes.length));

            while(current.hasRemaining())
            {
                mFileChannel.write(current);
            }

            rollover();

            openDataChunk();

            while(next.hasRemaining())
            {
                mFileChannel.write(next);
            }
        }
    }

    /**
     * Closes the current data chunk
     */
    private void closeDataChunk()
    {
        mDataChunkOpen = false;
    }

    /**
     * Opens a new data chunk if a data chunk is not currently open.  This method can be invoked repeatedly as an
     * assurance that the data chunk header has been written.
     *
     * @throws IOException if there is an error writing the data chunk header.
     */
    private void openDataChunk() throws IOException
    {
        if(!mDataChunkOpen)
        {
            if(mFileChannel.size() + 32 >= mMaxSize)
            {
                rollover();
            }

            mDataChunkOpen = true;
        }
    }

    /**
     * Closes out the current file, appends an incremented sequence number to
     * the file name and opens up a new file.
     */
    private void rollover() throws IOException
    {
        closeDataChunk();
        close();

        mFileRolloverCounter++;

        updateFileName();

        open();
    }

    /**
     * Creates a little-endian 4-byte buffer containing an unsigned 32-bit
     * integer value derived from the 4 least significant bytes of the argument.
     *
     * The buffer's position is set to 0 to prepare it for writing to a channel.
     */
    protected static ByteBuffer getUnsignedIntegerBuffer(long size)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4);

        buffer.put((byte)(size & 0xFFl));
        buffer.put((byte)(Long.rotateRight(size & 0xFF00l, 8)));
        buffer.put((byte)(Long.rotateRight(size & 0xFF0000l, 16)));

        /* This side-steps an issue with right shifting a signed long by 32
         * where it produces an error value.  Instead, we right shift in two steps. */
        buffer.put((byte)Long.rotateRight(Long.rotateRight(size & 0xFF000000l, 16), 8));

        buffer.position(0);

        return buffer;
    }

    public static String toString(ByteBuffer buffer)
    {
        StringBuilder sb = new StringBuilder();

        byte[] bytes = buffer.array();

        for(byte b : bytes)
        {
            sb.append(String.format("%02X ", b));
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * Updates the current file name with the rollover counter series suffix
     */
    private void updateFileName()
    {
        String filename = mFile.toString();

        if(mFileRolloverCounter == 2)
        {
            filename = filename.replace(".tmp", "_2.tmp");
        }
        else
        {
            Matcher m = FILENAME_PATTERN.matcher(filename);

            if(m.find())
            {
                StringBuilder sb = new StringBuilder();
                sb.append(m.group(1));
                sb.append(mFileRolloverCounter);
                sb.append(m.group(3));

                filename = sb.toString();
            }
        }

        mFile = Paths.get(filename);
    }
}
