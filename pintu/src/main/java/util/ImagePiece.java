package util;

import android.graphics.Bitmap;

/**
 * Created by W on 2016/9/5.
 * 1.定义一个类用于 存储每个小图片
 */
public class ImagePiece {
    private int index; //表示当前是第几块
    private Bitmap bitmap; //当前的图片

    public ImagePiece() {
    }

    public ImagePiece(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "ImagePiece{" +
                "index=" + index +
                ", bitmap=" + bitmap +
                '}';
    }
}
