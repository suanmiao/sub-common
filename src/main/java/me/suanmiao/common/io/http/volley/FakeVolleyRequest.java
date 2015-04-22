package me.suanmiao.common.io.http.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Map;

import me.suanmiao.common.io.http.exception.CommonParamException;
import me.suanmiao.common.io.http.exception.CommonRequestException;
import me.suanmiao.common.io.http.image.Photo;
import me.suanmiao.common.io.http.robospiece.TaggedRequestListener;

/**
 * Created by suanmiao on 15/1/19.
 */
public class FakeVolleyRequest<T> extends Request<T> {

  private IVolleyActionDelivery<T> volleyActionDelivery;
  private TaggedRequestListener<T> taggedRequestListener;
  private Map<String, String> headers;
  private Map<String, String> postParams;
  private boolean photoRequest = false;
  private Photo.LoadOption loadOption = Photo.LoadOption.BOTH;

  public FakeVolleyRequest(int method, String url, Map<String, String> headers,
      Map<String, String> postParams,
      IVolleyActionDelivery<T> volleyActionDelivery,
      TaggedRequestListener<T> taggedRequestListener) throws CommonRequestException {
    super(method, url, taggedRequestListener);
    checkNull("url", url);
    checkNull("action delivery", volleyActionDelivery);
    this.headers = headers;
    this.postParams = postParams;
    this.volleyActionDelivery = volleyActionDelivery;
    this.taggedRequestListener = taggedRequestListener;
  }

  private void checkNull(String paramName, Object param) throws CommonRequestException {
    if (param == null) {
      throw new CommonParamException("param " + paramName + " is null");
    }
  }

  public void setIsPhotoRequest(boolean photoRequest) {
    this.photoRequest = photoRequest;
  }

  public boolean isPhotoRequest() {
    return photoRequest;
  }

  public void setLoadOption(Photo.LoadOption loadOption) {
    this.loadOption = loadOption;
  }

  public Photo.LoadOption getLoadOption() {
    return loadOption;
  }

  @Override
  protected Response<T> parseNetworkResponse(NetworkResponse response) {
    return volleyActionDelivery.parseNetworkResponse(response);
  }

  @Override
  protected void deliverResponse(T response) {
    taggedRequestListener.onResponse(response);
  }

  @Override
  public void deliverError(VolleyError error) {
    super.deliverError(error);
    taggedRequestListener.onErrorResponse(error);
  }

  @Override
  public Map<String, String> getHeaders() throws AuthFailureError {
    if (headers != null) {
      return headers;
    } else {
      return super.getHeaders();
    }
  }

  /**
   * 
   * @return params for POST request
   * @throws AuthFailureError
   */
  @Override
  protected Map<String, String> getParams() throws AuthFailureError {
    if (this.postParams != null) {
      return this.postParams;
    } else {
      return super.getParams();
    }
  }
}
