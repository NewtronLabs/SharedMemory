package com.newtronlabs.smconsumerdemo;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.newtronlabs.sharedmemory.IRemoteSharedMemory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RemoteBitmapTask.OnRemoteBitmapListener
{

    /**
     * Sample: Name of the Shared Memory region allocated by the producer application.
     */
    private static final String mRemoteRegionName = "Test-Region";

    /**
     * Sample: Application id of the producer application.
     */
    private static final String mRemoteAppId = "com.newtronlabs.smproducerdemo";

    /**
     * Shared Memory object to interact with the remote memory allocated
     * by the producer application.
     */
    private IRemoteSharedMemory mSharedMemory;

    private ImageView mRemoteImageView;
    private TextView mMessageView;
    private Bitmap mLastBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Object savedState = getLastCustomNonConfigurationInstance();
        if(savedState instanceof IRemoteSharedMemory)
        {
            // Reuse the shared memory reference.
            mSharedMemory = (IRemoteSharedMemory)savedState;
        }

        mRemoteImageView = (ImageView)findViewById(R.id.img_remote);
        mMessageView = (TextView)findViewById(R.id.tv_message);

        View content = findViewById(R.id.content_holder);
        content.setOnClickListener(this);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance()
    {
        // Preserve the shared memory reference across the activity config change.
        return mSharedMemory;
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.content_holder)
        {
            /**
             * Try to access the remote shared memory.
             * This will fail if:
             *  - The remote application is not installed.
             *  - The remote application has not shared a region with the specified name.
             */
            RemoteBitmapTask task = new RemoteBitmapTask(this, mRemoteAppId, mRemoteRegionName, this);
            task.execute();
        }
    }

    @Override
    public void onBitmapRetrieved(Bitmap remoteBitmap, String producerAppId, String regionName)
    {
        if(remoteBitmap == null)
        {
            mRemoteImageView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.usage_message_failed);
        }
        else
        {
            mMessageView.setVisibility(View.GONE);
            mRemoteImageView.setVisibility(View.VISIBLE);
            mRemoteImageView.setImageBitmap(remoteBitmap);

            if(mLastBitmap != null && !mLastBitmap.isRecycled())
            {
                // Free the memory
                mLastBitmap.recycle();
                mLastBitmap = null;
            }
            mLastBitmap = remoteBitmap;
        }
    }
}
