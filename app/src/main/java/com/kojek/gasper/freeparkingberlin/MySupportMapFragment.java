package com.kojek.gasper.freeparkingberlin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by gaspe on 12. 10. 2016.
 */
public class MySupportMapFragment extends SupportMapFragment {
    private View mOriginalContentView;
    private TouchableWrapper mTouchView;
    private MapsActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MapsActivity) getActivity();
    }

    public static SupportMapFragment newInstance(GoogleMapOptions googleMapOptions) {
        return SupportMapFragment.newInstance(googleMapOptions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper();
        mTouchView.addView(mOriginalContentView);
        return mTouchView;
    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }

    public View getTouchView() {
        return mTouchView;
    }

    class TouchableWrapper extends FrameLayout {

        public TouchableWrapper() {
            super(mActivity);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                    Log.d("ACTION_DOWN", "metastate: " + event.getMetaState());
                    break;
                case MotionEvent.ACTION_UP: {
//                    Log.d("ACTION_UP", "metastate: " + event.getMetaState());
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    mActivity.tapEvent(x,y);
                    break;
                }
            }
            return super.dispatchTouchEvent(event);
        }
    }

}
