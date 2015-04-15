package me.suanmiao.common.io.http.volley;

import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;

import java.io.IOException;

import me.suanmiao.common.component.BaseApplication;
import me.suanmiao.common.io.MMBean;
import me.suanmiao.common.io.cache.CacheManager;
import me.suanmiao.common.io.http.image.Photo;
import me.suanmiao.common.io.http.image.volley.BitmapNetworkResponse;
import me.suanmiao.common.ui.blur.Blur;

/**
 * Created by suanmiao on 15/1/26.
 */
public class CommonNetwork extends BasicNetwork {

  static String userAgent = "volley/0";

  public CommonNetwork() {
    super(new HttpClientStack(AndroidHttpClient.newInstance(userAgent)));
  }

  @Override
  public NetworkResponse performRequest(Request<?> request) throws VolleyError {
    if (request instanceof FakeVolleyRequest && ((FakeVolleyRequest) request).isPhotoRequest()) {
      FakeVolleyRequest photoFakeVolleyRequest = (FakeVolleyRequest) request;
      if (photoFakeVolleyRequest.isBlurResult()) {
        if (photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.ONLY_FROM_CACHE
            || photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.BOTH) {
          Bitmap blurResult = getBlurImage(photoFakeVolleyRequest);
          if (photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.ONLY_FROM_CACHE
              || photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.BOTH) {
            if (blurResult != null) {
              return new BitmapNetworkResponse(blurResult);
            }
            Bitmap normalResult = getImage(photoFakeVolleyRequest);
            if (normalResult != null) {
              blurResult = Blur.apply(BaseApplication.getAppContext(), normalResult);
              return new BitmapNetworkResponse(blurResult);
            }
          }
          if (photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.ONLY_FROM_CACHE) {
            return new BitmapNetworkResponse(null);
          }
        }
      } else {
        if (photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.ONLY_FROM_CACHE
            || photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.BOTH) {
          Bitmap result = getImage(photoFakeVolleyRequest);
          if (result != null
              || photoFakeVolleyRequest.getLoadOption() == Photo.LoadOption.ONLY_FROM_CACHE) {
            return new BitmapNetworkResponse(result);
          }
        }
      }
    }
    return super.performRequest(request);
  }

  private Bitmap getImage(FakeVolleyRequest fakeVolleyRequest) {
    try {
      CacheManager mCacheManager = BaseApplication.getRequestManager().getCacheManager();
      MMBean bean = mCacheManager.get(fakeVolleyRequest.getUrl());
      return bean.getDataBitmap();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Bitmap getBlurImage(FakeVolleyRequest fakeVolleyRequest) {
    try {
      CacheManager mCacheManager = BaseApplication.getRequestManager().getCacheManager();
      MMBean bean = mCacheManager.get(fakeVolleyRequest.getUrl() + Photo.BLUR_SUFFIX);
      return bean.getDataBitmap();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
