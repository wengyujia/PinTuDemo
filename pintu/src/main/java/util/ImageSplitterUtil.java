package util;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.切片工具类
 * Created by W on 2016/9/5.
 */
public class ImageSplitterUtil {

    //传入bitmap，切成piece*piece块，返回list<ImagePiece>
    public  static List<ImagePiece> splitImage(Bitmap bitmap,int piece){
        List<ImagePiece> imagePieces=new ArrayList<ImagePiece>();//作为返回值

        //得到图片的宽高
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();

        int pieceWidth=Math.min(width,height)/piece;//每个小图片的宽度，取最小值

        for (int i=0;i<piece;i++) {
            for(int j=0;j<piece;j++){

                ImagePiece imagePiece=new ImagePiece();
                imagePiece.setIndex(j+i*piece);

                int x=j*pieceWidth;
                int y=i*pieceWidth;

                imagePiece.setBitmap(Bitmap.createBitmap(bitmap,x,y,pieceWidth,pieceWidth));
                imagePieces.add(imagePiece);

            }
        }


        return imagePieces;
    }

}
