package me.suanmiao.common.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by suanmiao on 15/4/15.
 */
public class MMBean {
  public static final int TYPE_NONE = -1;
  public static final int TYPE_BITMAP = 1;
  public static final int TYPE_BYTE = 2;

  private static final int LENGTH_TYPE_BYTE = 4;
  private static final int LENGTH_SIZE_BYTE = 16;
  private static final int BUFFER_SIZE = 512;

  private int type = TYPE_NONE;
  private long size;

  // original data
  private byte[] data;
  // bitmap data
  private Bitmap dataBitmap;

  public MMBean(int type, byte[] data) {
    this.type = type;
    this.data = data;
    this.size = data.length;
  }

  public MMBean(Bitmap bitmap) {
    this.type = TYPE_BITMAP;
    this.dataBitmap = bitmap;
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

  public Bitmap getDataBitmap() {
    return dataBitmap;
  }

  public static MMBean fromBitmapStream(InputStream stream) {
    Bitmap result = BitmapFactory.decodeStream(stream);
    return new MMBean(result);
  }

  public static MMBean fromStream(InputStream stream) {
    try {
      byte[] typeBytes = new byte[LENGTH_TYPE_BYTE];
      stream.read(typeBytes);
      ByteBuffer byteBuffer = ByteBuffer.wrap(typeBytes);
      int type = byteBuffer.getInt();
      byte[] sizeBytes = new byte[LENGTH_TYPE_BYTE];
      stream.read(sizeBytes);
      byteBuffer = ByteBuffer.wrap(sizeBytes);
      long size = byteBuffer.getLong();
      switch (type) {
        case TYPE_BYTE:
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] buffer = new byte[BUFFER_SIZE];
          int len;
          while ((len = stream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
          }
          baos.flush();
          return new MMBean(type, baos.toByteArray());
        case TYPE_BITMAP:
          Bitmap bitmap = BitmapFactory.decodeStream(stream);
          return new MMBean(bitmap);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void toStream(OutputStream stream) {
    try {
      byte[] typeBytes = ByteBuffer.allocate(LENGTH_TYPE_BYTE).putInt(type).array();
      byte[] sizeBytes = ByteBuffer.allocate(LENGTH_SIZE_BYTE).putLong(size).array();
      switch (type) {
        case TYPE_BYTE:
          stream.write(typeBytes);
          stream.write(sizeBytes);
          stream.write(data);
          break;
        case TYPE_BITMAP:
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          dataBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
          stream.write(typeBytes);
          stream.write(sizeBytes);
          stream.write(byteArrayOutputStream.toByteArray());
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
