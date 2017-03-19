package com.newtronlabs.smproducerdemo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.newtronlabs.sharedmemory.prod.memory.ISharedMemory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class ShareBitmapTask extends AsyncTask<Void, Void, Bitmap>
{
    /**
     * Listener that will be notified when the Bitmap is loaded and
     * written to the Shared Memory region.
     */
    public interface OnProducedBitmapListener
    {
        /**
         * Called prior to the start of the loading process.
         * @param sharedMemory
         */
        void onBitmapPreload(ISharedMemory sharedMemory);
        /**
         * Called when the bitmap alloaction process is done.
         * @param producedBitmap Bitmap that was loaded into the Shared Memory region, or null on error.
         * @param sharedMemory Shared Memory region where the bitmap was written to.
         */
        void onBitmapProduced(Bitmap producedBitmap, ISharedMemory sharedMemory);
    }

    private Context mContext;
    private @DrawableRes int mBitmapRes;
    private ISharedMemory mSharedRegion;
    private WeakReference<OnProducedBitmapListener> mListener;

    public ShareBitmapTask(Context context, @DrawableRes int bitmapRes, ISharedMemory memoryRegion, OnProducedBitmapListener listener)
    {
        mContext = context.getApplicationContext();
        mBitmapRes = bitmapRes;
        mSharedRegion = memoryRegion;
        mListener = new WeakReference<OnProducedBitmapListener>(listener);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        OnProducedBitmapListener listener = mListener.get();

        if(listener!= null)
        {
            listener.onBitmapPreload(mSharedRegion);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... voids)
    {

        if(mSharedRegion == null)
        {
            return null;
        }

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), mBitmapRes);

        if(bm == null)
        {
            Log.e("Sm-Producer", "Bitmap factory failed to load Bitmap. Image too large.");
            return null;
        }


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bmBytes = stream.toByteArray();

        Bitmap producedBitmap = null;

        try
        {
            // Copy the Bitmap to the Shared Memory region to make it accessible to
            // other processes or applications.
            mSharedRegion.writeBytes(bmBytes, 0, 0, bmBytes.length);
            producedBitmap = bm;

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return producedBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        super.onPostExecute(bitmap);

        OnProducedBitmapListener listener = mListener.get();

        if(listener == null)
        {
            return;
        }

        listener.onBitmapProduced(bitmap, mSharedRegion);
    }
}
