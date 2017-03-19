package com.newtronlabs.smproducerdemo;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.newtronlabs.sharedmemory.SharedMemoryProducer;
import com.newtronlabs.sharedmemory.prod.memory.ISharedMemory;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ShareBitmapTask.OnProducedBitmapListener
{
    /**
     * Sample: Name of the Shared Memory to allocate so that other applications
     * can access it.
     */
    private static final String mRegionName = "Test-Region";

    private ISharedMemory mLocalMemory;
    private ImageView mImageView;
    private TextView mMessageView;
    private Bitmap mSharedBitmap;
    private View mContentHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.img_remote);
        mMessageView = (TextView)findViewById(R.id.tv_message);

        mContentHolder = findViewById(R.id.content_holder);
        mContentHolder.setOnClickListener(this);

        View closeBtn = findViewById(R.id.btn_destroy);
        closeBtn.setOnClickListener(this);

        Object savedState = getLastCustomNonConfigurationInstance();
        if(savedState instanceof ISharedMemory)
        {
            // Reuse the shared memory reference.
            mLocalMemory = (ISharedMemory)savedState;
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance()
    {
        // Preserve the shared memory reference across the activity config change.
        return mLocalMemory;
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.content_holder)
        {
            /**
             * Allocate a region of memory that can be accessed by other processes
             * and applications.
             */
            if(mLocalMemory != null)
            {
                mLocalMemory.close();
            }

            // Allocate 2MB.
            int sizeInBytes = 2*(1024*1024);
            try
            {
                mLocalMemory = SharedMemoryProducer.getInstance().allocate(mRegionName, sizeInBytes);
                ShareBitmapTask task = new ShareBitmapTask(this, R.drawable.newtron, mLocalMemory, this);
                task.execute();
            }
            catch (IOException e)
            {
                handleErrorCase();
            }
        }
        else if(view.getId() == R.id.btn_destroy)
        {
            if(mLocalMemory != null)
            {
                /**
                 * Release the shared memory.
                 * From this point on any attempt by a remote application to read or
                 * write will fail with a IOException.
                 */
                mLocalMemory.close();
                mLocalMemory = null;
            }

            mMessageView.setText(R.string.usage_message);
            mMessageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(null);
            mImageView.setVisibility(View.GONE);
            if(mSharedBitmap != null && !mSharedBitmap.isRecycled())
            {
                mSharedBitmap.recycle();
                mSharedBitmap = null;
            }
        }
    }

    /**
     * Helper method to handle the management of the UI
     * on error.
     */
    private void handleErrorCase()
    {
        mMessageView.setText(R.string.usage_message_failed);
        mImageView.setVisibility(View.GONE);
        mMessageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBitmapPreload(ISharedMemory sharedMemory)
    {
        mMessageView.setText(R.string.usage_message_loading);
        mContentHolder.invalidate();
    }

    @Override
    public void onBitmapProduced(Bitmap producedBitmap, ISharedMemory sharedMemory)
    {
        if(producedBitmap == null)
        {
            handleErrorCase();
        }
        else
        {
            // Display the bitmap.
            mImageView.setImageBitmap(producedBitmap);
            mMessageView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);

            mSharedBitmap = producedBitmap;
        }
    }
}
