package com.newtronlabs.smconsumerdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.newtronlabs.sharedmemory.IRemoteSharedMemory;
import com.newtronlabs.sharedmemory.RemoteMemoryAdapter;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Helper task for reading a bitmap from a Shared Memory region.
 * In order for the bitmap to be loaded the Remote application must have:
 *  1 - Allocated a Shared Memory region.
 *  2- Written a bitmap to that region.
 */
public class RemoteBitmapTask extends AsyncTask<Void, Void, Bitmap>
{

    /**
     * Listener that will be notified when the Bitmap retrieval is done.
     */
    public interface OnRemoteBitmapListener
    {
        /**
         * Called when the Bitmap retrieval is done.
         * @param remoteBitmap Bitmap retrieved from the remote app, null if the image could not be read.
         * @param producerAppId Application ID of the remote application whose memory we tried to access.
         * @param regionName Name of the region we tried to access on the remote application.
         */
        void onBitmapRetrieved(Bitmap remoteBitmap, String producerAppId, String regionName);
    }

    //Application ID of the Android application that shared the memory.
    private final String mProducerAppId;

    //Name of the Shared Region as created by the remote producer app.
    private final String mRegionName;

    private Context mContext;

    private WeakReference<OnRemoteBitmapListener> mListener;

    public RemoteBitmapTask(Context context, String producerAppId, String regionName, OnRemoteBitmapListener listener)
    {
        mContext = context.getApplicationContext();
        mProducerAppId = producerAppId;
        mRegionName = regionName;
        mListener = new WeakReference<OnRemoteBitmapListener>(listener);

    }
    @Override
    protected Bitmap doInBackground(Void... voids)
    {
        // Try to access the memory from the remote application.
        // Note: The remote application must have allocated a memory region with the same
        //       name or this call will fail and return null.
        IRemoteSharedMemory remoteMemory = RemoteMemoryAdapter.getDefaultAdapter()
                .getSharedMemory(mContext, mProducerAppId, mRegionName);

        if(remoteMemory == null)
        {
            // Failed to access shared memory.
            return null;
        }

        // Allocate memory to read the bitmap.
        byte[] bitmapBytes = new byte[remoteMemory.getSize()];

        Bitmap remoteBitmap = null;

        try
        {
            // Read the remote memory.
            remoteMemory.readBytes(bitmapBytes, 0, 0, bitmapBytes.length);
            remoteBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            // Close the reference we don't need it anymore.
            remoteMemory.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }



        return remoteBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        super.onPostExecute(bitmap);

        OnRemoteBitmapListener listener = mListener.get();

        if(listener == null)
        {
            return;
        }

        listener.onBitmapRetrieved(bitmap, mProducerAppId, mRegionName );
    }
}
