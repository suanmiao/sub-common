package me.suanmiao.common.io.http.image.volley;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import me.suanmiao.common.io.MMBean;
import me.suanmiao.common.io.http.image.Photo;
import me.suanmiao.common.util.BitmapUtil;

/**
 * Created by suanmiao on 15/1/24.
 */
public class PhotoActionDelivery extends BaseCachePhotoActionDelivery {
  private Photo photo;
  protected static final String KEY_CONTENT_LENGTH = "Content-Length";

  public PhotoActionDelivery(Photo photo) {
    this.photo = photo;
  }

  @Override
  public Response<Photo> parseNetworkResponse(NetworkResponse response) {
    try {
      if (response instanceof BitmapNetworkResponse) {
        BitmapNetworkResponse bitmapNetworkResponse = (BitmapNetworkResponse) response;
        photo.setBitmap(bitmapNetworkResponse.getResult());
        return Response.success(photo, HttpHeaderParser.parseCacheHeaders(response));
      } else {
        /**
         * get content length
         */
        String contentLength = response.headers.get(KEY_CONTENT_LENGTH);
        if (!TextUtils.isEmpty(contentLength)) {
          photo.setContentLength(Integer.parseInt(contentLength));
        }
        response.headers.get(KEY_CONTENT_LENGTH);
        Bitmap result = BitmapUtil.decodePhoto(response.data, photo);
        if (result != null) {
          getCacheManager().put(photo.getUrl(), MMBean.fromBitmap(result), true);
        }
        photo.setBitmap(result);
        return Response.success(photo, HttpHeaderParser.parseCacheHeaders(response));
      }
    } catch (Exception e) {
      return Response.error(new ParseError());
    }

  }
}
