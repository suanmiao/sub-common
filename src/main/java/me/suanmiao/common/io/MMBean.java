package me.suanmiao.common.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import me.suanmiao.common.ui.widget.BigBitmap;

/**
 * Created by suanmiao on 15/4/15.
 */
public class MMBean {
  public static final int TYPE_NONE = -1;
  public static final int TYPE_PHOTO = 1;
  public static final int TYPE_BYTE = 2;

  private static final int LENGTH_TYPE_BYTE = 2;
  private static final int LENGTH_SIZE_BYTE = 4;
  private static final int BUFFER_SIZE = 512;

  private int type = TYPE_NONE;
  private long size;
  private byte[] data;

  private BigBitmap cachedBitmap;

  public MMBean(int type, byte[] data) {
    this.type = type;
    this.data = data;
    this.size = data.length;
  }

  public long getSize() {
    return size;
  }

  public int getType() {
    return type;
  }

  public byte[] getData() {
    return data;
  }

  public static MMBean fromBitmapStream(InputStream stream) {
    try {
      int type = TYPE_PHOTO;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[BUFFER_SIZE];
      int len;
      while ((len = stream.read(buffer)) > -1) {
        baos.write(buffer, 0, len);
      }
      baos.flush();
      return new MMBean(type, baos.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static MMBean fromStream(InputStream stream) {
    try {
      byte[] typeBytes = new byte[LENGTH_TYPE_BYTE];
      stream.read(typeBytes);
      ByteBuffer byteBuffer = ByteBuffer.wrap(typeBytes);
      int type = byteBuffer.getInt();
      byte[] sizeBytes = new byte[LENGTH_TYPE_BYTE];
      byteBuffer = ByteBuffer.wrap(sizeBytes);
      long size = byteBuffer.getLong();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[BUFFER_SIZE];
      int len;
      while ((len = stream.read(buffer)) > -1) {
        baos.write(buffer, 0, len);
      }
      baos.flush();
      return new MMBean(type, baos.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static MMBean fromBitmap(Bitmap content) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    content.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
    return new MMBean(TYPE_PHOTO, byteArrayOutputStream.toByteArray());
  }

  public Bitmap toBitmap() {
    return BitmapFactory.decodeByteArray(data, 0, (int) size);
  }

  public void toStream(OutputStream stream) {
    try {
      byte[] typeBytes = ByteBuffer.allocate(LENGTH_TYPE_BYTE).putInt(type).array();
      byte[] sizeBytes = ByteBuffer.allocate(LENGTH_SIZE_BYTE).putLong(size).array();
      stream.write(typeBytes);
      stream.write(sizeBytes);
      stream.write(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
