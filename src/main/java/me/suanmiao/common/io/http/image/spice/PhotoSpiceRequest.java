package me.suanmiao.common.io.http.image.spice;

import android.graphics.Bitmap;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

import me.suanmiao.common.component.BaseApplication;
import me.suanmiao.common.io.MMBean;
import me.suanmiao.common.io.http.image.Photo;
import me.suanmiao.common.ui.blur.Blur;

/**
 * Created by suanmiao on 14/12/7.
 */
public class PhotoSpiceRequest extends BaseCacheImageRequest<Photo> {

  protected static final String KEY_CONTENT_LENGTH = "Content-Length";
  protected Photo photo;
  protected boolean shouldCache = true;
  protected Photo.LoadOption loadOption = Photo.LoadOption.BOTH;
  private boolean blurResult = false;

  public PhotoSpiceRequest(Photo photo) {
    super(Photo.class);
    this.photo = photo;
  }

  public PhotoSpiceRequest(Photo photo, Photo.LoadOption option) {
    super(Photo.class);
    this.photo = photo;
    this.loadOption = option;
  }

  @Override
  public Photo loadDataFromNetwork() throws IOException {
    if (blurResult) {
      return loadBlurPhoto();
    } else {
      return loadNormalPhoto();
    }
  }

  private Photo loadNormalPhoto() throws IOException {
    switch (loadOption) {
      case ONLY_FROM_CACHE: {
        MMBean cacheContent = getCacheManager().get(photo.getUrl());
        photo.setContent(cacheContent);
      }
        break;
      case ONLY_FROM_NETWORK: {
        MMBean networkContent = getMMFromNetwork();
        if (shouldCache && networkContent != null) {
          getCacheManager().put(photo.getUrl(), networkContent, true);
        }
        photo.setContent(networkContent);
      }
        break;
      case BOTH: {
        MMBean content = getCacheManager().get(photo.getUrl());
        if (content == null) {
          content = getMMFromNetwork();
          if (shouldCache && content != null) {
            getCacheManager().put(photo.getUrl(), content, true);
          }
        }
        photo.setContent(content);
      }
        break;
    }
    return photo;
  }

  private Photo loadBlurPhoto() throws IOException {
    switch (loadOption) {
      case ONLY_FROM_CACHE: {
        MMBean cacheContent = getCacheManager().get(photo.getUrl() + Photo.BLUR_SUFFIX);
        if (cacheContent != null) {
          photo.setContent(cacheContent);
        } else {
          cacheContent = getCacheManager().get(photo.getUrl());
          if (cacheContent != null) {
            Bitmap blurBitmap =
                Blur.apply(BaseApplication.getAppContext(), cacheContent.getDataBitmap());
            photo.setContent(new MMBean(blurBitmap));
            getCacheManager().put(photo.getUrl() + Photo.BLUR_SUFFIX,
                new MMBean(blurBitmap), true);
          }
        }
      }
        break;
      case ONLY_FROM_NETWORK: {
        MMBean networkContent = getMMFromNetwork();
        if (shouldCache && networkContent != null) {
          Bitmap blurBitmap =
              Blur.apply(BaseApplication.getAppContext(), networkContent.getDataBitmap());
          getCacheManager().put(photo.getUrl(), networkContent, true);
          getCacheManager().put(photo.getUrl() + Photo.BLUR_SUFFIX, new MMBean(blurBitmap),
              true);
          photo.setContent(new MMBean(blurBitmap));
        }
      }
        break;
      case BOTH: {
        MMBean content = getCacheManager().get(photo.getUrl() + Photo.BLUR_SUFFIX);
        if (content == null) {
          content = getCacheManager().get(photo.getUrl());
          if (content != null) {
            Bitmap blurBitmap =
                Blur.apply(BaseApplication.getAppContext(), content.getDataBitmap());
            getCacheManager().put(photo.getUrl() + Photo.BLUR_SUFFIX,
                new MMBean(blurBitmap), true);
            photo.setContent(new MMBean(blurBitmap));
          } else {
            MMBean networkContent = getMMFromNetwork();
            if (shouldCache && networkContent != null) {
              Bitmap blurBitmap =
                  Blur.apply(BaseApplication.getAppContext(), networkContent.getDataBitmap());
              getCacheManager().put(photo.getUrl(), networkContent, true);
              getCacheManager().put(photo.getUrl() + Photo.BLUR_SUFFIX,
                  new MMBean(blurBitmap), true);
              photo.setContent(new MMBean(blurBitmap));
            }
          }
        }
      }
        break;
    }
    return photo;
  }

  protected MMBean getMMFromNetwork() throws IOException {
    Request request = new Request.Builder()
        .url(photo.getUrl())
        .build();
    Response response = getOkHttpClient().newCall(request).execute();
    /**
     * get content length
     */
    String contentLength = response.header(KEY_CONTENT_LENGTH, "0");
    photo.setContentLength(Integer.parseInt(contentLength));

    InputStream in = response.body().byteStream();
    return MMBean.fromBitmapStream(in);
  }

  public boolean isBlurResult() {
    return blurResult;
  }

  public void setBlurResult(boolean blurResult) {
    this.blurResult = blurResult;
  }

  public boolean isShouldCache() {
    return shouldCache;
  }

  public void setShouldCache(boolean shouldCache) {
    this.shouldCache = shouldCache;
  }

  public Photo.LoadOption getLoadOption() {
    return loadOption;
  }

  public void setLoadOption(Photo.LoadOption loadOption) {
    this.loadOption = loadOption;
  }
}
