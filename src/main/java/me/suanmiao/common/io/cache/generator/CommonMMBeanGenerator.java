package me.suanmiao.common.io.cache.generator;

import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import me.suanmiao.common.io.cache.mmbean.AbstractMMBean;
import me.suanmiao.common.io.cache.mmbean.BaseMMBean;

/**
 * Created by suanmiao on 15/4/23.
 */
public class CommonMMBeanGenerator implements IMMBeanGenerator {
  public static final int MAX_NORMAL_BITMAP_SIZE = 900;

  private static final int BUFFER_SIZE = 512;

  @Override
  public AbstractMMBean generateMMBeanFromTotalStream(InputStream stream) {
    try {
      byte[] typeBytes = new byte[AbstractMMBean.LENGTH_TYPE_BYTE];
      stream.read(typeBytes);
      ByteBuffer byteBuffer = ByteBuffer.wrap(typeBytes);
      int dataType = byteBuffer.getInt();
      byte[] sizeBytes = new byte[AbstractMMBean.LENGTH_SIZE_BYTE];
      stream.read(sizeBytes);
      byteBuffer = ByteBuffer.wrap(sizeBytes);
      long dataSize = byteBuffer.getLong();

      switch (dataType) {
        case AbstractMMBean.TYPE_BYTE:
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] buffer = new byte[BUFFER_SIZE];
          int len;
          while ((len = stream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
          }
          baos.flush();
          return new BaseMMBean(baos.toByteArray());
        case AbstractMMBean.TYPE_BITMAP:
          return new BaseMMBean(BitmapFactory.decodeStream(stream));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public AbstractMMBean constructMMBeanFromNetworkStream(InputStream stream) {
    return new BaseMMBean(BitmapFactory.decodeStream(stream));
  }

  @Override
  public AbstractMMBean constructMMBeanFromNetworkData(byte[] data) {
    return new BaseMMBean(BitmapFactory.decodeByteArray(data, 0, data.length));
  }
}
