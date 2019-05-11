package com.example.hui.happy_of_disappear;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Random;

import tyrantgit.explosionfield.ExplosionField;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private int row = 8;
    private int col = 4;
    private int arr[][] = new int [8][4];
    private Location firstClick;
    private Location secondClick;
    GridLayout gridLayout;
    Random mRandom = new Random();
    private HashSet<String> eliminateSet = new HashSet<>();
    private int clickCount = 0;
    private ImageView firstView;
    private ImageView secondView;

    //每一列下落的个数
    private int[] eachColDownNum = new int[col];
    //需要下落的点
    private int[][] needDownPoint = new int [row][col];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridLayout =findViewById(R.id.grid_layout);
        gridLayout.setColumnCount(col);
        gridLayout.setRowCount(row);
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                initArr(arr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
                });
            }
        };
        thread.start();
    }

    @SuppressLint("ResourceAsColor")
    public void init(){
        for(int i = 0;i<row;i++){
            for(int j = 0;j<col;j++){
                ImageView image = new ImageView(this);
                image.setClickable(true);
                image.setId(i*10+j);
                image.setOnClickListener(this);
                GridLayout.LayoutParams para = new GridLayout.LayoutParams();
                para.width = 0;
                para.height = 0;
                para.columnSpec = GridLayout.spec(j,1f);
                para.rowSpec = GridLayout.spec(i,1f);
                //1的时候牛
                if(arr[i][j]==1)
                    image.setImageBitmap(getBitmap(R.drawable.cattle));
                //2的时候使小鸡
                if(arr[i][j]==2)
                    image.setImageBitmap(getBitmap(R.drawable.chick));
                //3的时候是狐狸
                if(arr[i][j]==3)
                    image.setImageBitmap(getBitmap(R.drawable.fox));
                //4的时候是青蛙
                if(arr[i][j]==4)
                    image.setImageBitmap(getBitmap(R.drawable.frog));
                if(arr[i][j]==0){
                    image.setImageBitmap(getBitmap(R.drawable.white));
                    image.setClickable(false);
                }
                gridLayout.addView(image,para);
            }
        }
    }

    private void initArr(int[][] arr){
       for(int i = 0;i<row;i++){
           for(int j = 0;j<col;j++){
               arr[i][j] = mRandom.nextInt(4)+1;;
               if(i-2>=0&&arr[i][j]==arr[i-1][j]&&arr[i-2][j]==arr[i][j]||j-2>=0&&arr[i][j]==arr[i][j-1]&&arr[i][j]==arr[i][j-2])
                   j--;
           }
       }
       for(int i = 0;i<col;i++){
           eachColDownNum[i] = 0;
       }
    }

    private Bitmap getBitmap(int id){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //设置inJustDecodeBounds为true，只返回bitmap的尺寸。
        options.inJustDecodeBounds = true;
        //计算bitmap的尺寸
        options.inSampleSize = calculateInSampleSize(100,100,options);
        //记得将inJustDecodeBounds设为false，否则位图加载不出来
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(getResources(),id,options);
    }
    private int calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options){
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if(width>reqWidth||height>reqHeight){
            final int halfHeight = height/2;
            final int halfWidth = width/2;
            while ((halfHeight/inSampleSize>=reqHeight)&&(halfWidth/inSampleSize)>=reqWidth){
                inSampleSize*=2;
            }
            return inSampleSize;
        }
        return inSampleSize;
    }

    @Override
    public void onClick(final View v) {
        if(clickCount==0){
            clickCount++;
            int firstRow =(int) v.getId()/10;
            int firstCol = (int)v.getId()%10;
            firstClick = new Location (firstRow,firstCol);

        }else if(clickCount==1){
            clickCount++;
            int firstRow =(int) v.getId()/10;
            int firstCol = (int)v.getId()%10;
            secondClick = new Location (firstRow,firstCol);
        }

        if(clickCount==2){
            for(int i = 0;i<col;i++){
                eachColDownNum[i] = 0;
            }
            //第二次点击，判断他们是不是相邻的，如果相邻交换他们的位置,并且将记录清空
            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        readyToEliminate(v);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
        Log.i("TAG","id:"+v.getId());
    }

    private double distance(Location firstClick,Location secondClick){
        int firstCol = firstClick.getCol();
        int firstRow = firstClick.getRow();
        int secondCol = secondClick.getCol();
        int secondRow = secondClick.getRow();
        return Math.sqrt((firstCol-secondCol)*(firstCol-secondCol)+(firstRow-secondRow)*(firstRow-secondRow));
    }

    private void readyToEliminate(View v) throws InterruptedException {
        double distance = 0;
        if(firstClick!=null&&secondClick!=null)
            distance= distance(firstClick,secondClick);
        else
            return;
        if(distance>0&&distance==1){
            clickCount=0;
            //第一个点和第二个点交换位置
            if(isEliminate(firstClick.getRow(),firstClick.getCol(),arr)&&isEliminate(secondClick.getRow(),secondClick.getCol(),arr)){
                swapViewOnMainThread();
            }
            else if(isEliminate(secondClick.getRow(),secondClick.getCol(),arr)){
                swapViewOnMainThread();
            }
            else if(isEliminate(firstClick.getRow(),firstClick.getCol(),arr)){
                swapViewOnMainThread();
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swapViewAndBack(firstClick,secondClick);
                    }
                });
            }

            Thread.sleep(600);

            //交换并消除
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(eliminateSet.size()!=0){
                        swapRect();
                        eliminate();
                    }
                }
            });

            //休眠200毫秒
            Thread.sleep(300);


            //消去之后，将消去点变为0，并将上面的点下落。
            changArr();

            Thread.sleep(600);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0;i<col;i++){
                        eachColDownNum[i] = 0;
                    }
                    gridLayout.removeAllViews();
                    init();
                }
            });

            Thread.sleep(200);

            firstClick = null;
            secondClick = null;
            needCheckPoint();

        }

        //如果不是相邻的位置，将第一个firstClick设为第二次的，第二次的为null
        else{
            clickCount = 1;
            firstClick = secondClick;
            secondClick = null;
        }
    }

    //交换View
    private void swapView(Location firstClick,Location secondClick){
        int firstCol = firstClick.getCol();
        int firstRow = firstClick.getRow();
        int secondCol = secondClick.getCol();
        int secondRow = secondClick.getRow();
        int firstId = firstRow*10+firstCol;
        int secondId = secondRow*10+secondCol;

        ImageView firstView = (ImageView) findViewById(firstId);
        ImageView secondView = (ImageView) findViewById(secondId);
        //左右交换
        if(firstView.getBottom()==secondView.getBottom()){
            ObjectAnimator.ofFloat(firstView,"translationX",0f,secondView.getLeft()-firstView.getLeft()).setDuration(500).start();
            ObjectAnimator.ofFloat(secondView,"translationX",0f,firstView.getLeft()-secondView.getLeft()).setDuration(500).start();
        }
        //上下交换
        else{
            ObjectAnimator.ofFloat(firstView,"translationY",0f,secondView.getBottom()-firstView.getBottom()).setDuration(500).start();
            ObjectAnimator.ofFloat(secondView,"translationY",0f,firstView.getBottom()-secondView.getBottom()).setDuration(500).start();
        }
    }

    private void swapViewOnMainThread(){
        swapArr();
        swapID();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swapView(firstClick,secondClick);
            }
        });

    }

    private void swapViewAndBack(Location firstClick, Location secondClick){

        int firstCol = firstClick.getCol();
        int firstRow = firstClick.getRow();
        int secondCol = secondClick.getCol();
        int secondRow = secondClick.getRow();
        int firstId = firstRow*10+firstCol;
        int secondId = secondRow*10+secondCol;

        ImageView firstView = (ImageView) findViewById(firstId);
        ImageView secondView = (ImageView) findViewById(secondId);
        //左右交换
        if(firstView.getBottom()==secondView.getBottom()){

            ObjectAnimator first_moveX1 =  ObjectAnimator.ofFloat(firstView,"translationX",0f,secondView.getLeft()-firstView.getLeft());
            ObjectAnimator second_moveX1 =  ObjectAnimator.ofFloat(secondView,"translationX",0f,firstView.getLeft()-secondView.getLeft());
            ObjectAnimator first_moveX2 =  ObjectAnimator.ofFloat(firstView,"translationX",secondView.getLeft()-firstView.getLeft(),0f);
            ObjectAnimator second_moveX2 =  ObjectAnimator.ofFloat(secondView,"translationX",firstView.getLeft()-secondView.getLeft(),0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(first_moveX1).with(second_moveX1).before(first_moveX2).before(second_moveX2);
            animatorSet.setDuration(500);
            animatorSet.start();
        }
        //上下交换
        else{
            ObjectAnimator first_moveY1 =  ObjectAnimator.ofFloat(firstView,"translationY",0f,secondView.getBottom()-firstView.getBottom());
            ObjectAnimator second_moveY1 =  ObjectAnimator.ofFloat(secondView,"translationY",0f,firstView.getBottom()-secondView.getBottom());
            ObjectAnimator first_moveY2 =  ObjectAnimator.ofFloat(firstView,"translationY",secondView.getBottom()-firstView.getBottom(),0f);
            ObjectAnimator second_moveY2 =  ObjectAnimator.ofFloat(secondView,"translationY",firstView.getBottom()-secondView.getBottom(),0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(first_moveY1).with(second_moveY1).before(first_moveY2).before(second_moveY2);
            animatorSet.setDuration(500);
            animatorSet.start();
        }

    }

    //判断交换之后能不能消除,如果能消除记录这个点
    private boolean isEliminate(int i,int j,int[][] arr){
        int[][] arr2 = new int [row][col];
        for(int m = 0;m<row;m++){
            for(int n = 0;n<col;n++){
                arr2[m][n] = arr[m][n];
            }
        }
        if(firstClick!=null&&secondClick!=null){
            int temp = arr2[firstClick.getRow()][firstClick.getCol()];
            arr2[firstClick.getRow()][firstClick.getCol()] = arr2[secondClick.getRow()][secondClick.getCol()];
            arr2[secondClick.getRow()][secondClick.getCol()] = temp;
        }


        int num1 = 0;
        int num2 = 0;
        int num3 = 0;
        int num4 = 0;
        //判断这个点的上下左右
        //上
        for(int k=i-1;k>=0;k--){
            if(arr2[i][j]==arr2[k][j]&&arr2[i][j]!=0)
                num1++;
            else
                break;
        }
        //下
        for(int k = i+1;k<(row>i+4?i+4:row);k++){
            if(arr2[i][j]==arr2[k][j]&&arr2[i][j]!=0)
                num2++;
            else
                break;
        }
        //左
        for(int k=j-1;k>=0;k--){
            if(arr2[i][j]==arr2[i][k]&&arr2[i][j]!=0)
                num3++;
            else
                break;
        }
        //右
        for(int k = j+1;k<(col>j+4?j+4:col);k++){
            if(arr2[i][j]==arr2[i][k]&&arr2[i][j]!=0)
                num4++;
            else
                break;
        }
        //纵向有多少个可以消去
        int col_num = num1+num2+1;
        //横向有多少个可以消去
        int row_num = num3+num4+1;

        if(col_num>=3||row_num>=3){

            eliminateSet.add(i+""+j);
            if(col_num>=3){

                for(;num1>0;num1--){
                    String location = (i-num1)+""+j;
                    eliminateSet.add(location);
                }
                for(;num2>0;num2--){
                    String location = (i+num2)+""+j;
                    eliminateSet.add(location);
                }
            }
            if(row_num>=3){

                for(;num3>0;num3--){
                    String location = i+""+(j-num3);
                    eliminateSet.add(location);
                }
                for(;num4>0;num4--){
                    String location = i+""+(j+num4);
                    eliminateSet.add(location);
                }
            }
            return true;
        }
        return false;
    }


    //交换选中两个的ID
    private void swapID(){

        int firstCol = firstClick.getCol();
        int firstRow = firstClick.getRow();
        int secondCol = secondClick.getCol();
        int secondRow = secondClick.getRow();
        int firstId = firstRow*10+firstCol;
        int secondId = secondRow*10+secondCol;


        ImageView firstView = (ImageView) findViewById(firstId);
        ImageView secondView = (ImageView) findViewById(secondId);


//        Log.i("TAG","first交换前的id："+firstId);
//        Log.i("TAG","second交换前的id："+secondId);


        firstView.setId(secondId);
        secondView.setId(firstId);

        this.firstView = firstView;
        this.secondView = secondView;
//        Log.i("TAG","first交换后的id："+firstView.getId());
//        Log.i("TAG","second交换后的id："+secondView.getId());
    }

    private void swapArr(){
        int temp = arr[firstClick.getRow()][firstClick.getCol()];
        arr[firstClick.getRow()][firstClick.getCol()] = arr[secondClick.getRow()][secondClick.getCol()];
        arr[secondClick.getRow()][secondClick.getCol()] = temp;
    }

    private void test(){
        int firstCol = firstClick.getCol();
        int firstRow = firstClick.getRow();
        int secondCol = secondClick.getCol();
        int secondRow = secondClick.getRow();
        int firstId = firstRow*10+firstCol;
        int secondId = secondRow*10+secondCol;
        ImageView firstView = (ImageView) findViewById(firstId);
        ImageView secondView = (ImageView) findViewById(secondId);
        Log.i("TAG","交换后的id："+firstView.getId()+"");
        Log.i("TAG","交换后的id："+secondView.getId()+"");

    }

    private void eliminate(){
        ExplosionField explosionField = ExplosionField.attach2Window(this);
        for(String id:eliminateSet){
            ImageView eliminateView = (ImageView) findViewById(Integer.parseInt(id));
            explosionField.explode(eliminateView);
        }
    }

    //改变View的Rect,避免消去的时候重绘View，然后造成View的突然改变位置
    private void swapRect(){
        //左右交换
        if(firstView.getTop()==secondView.getTop()){
            int left = firstView.getLeft();
            int right = firstView.getRight();
            firstView.setLeft(secondView.getLeft());
            firstView.setRight(secondView.getRight());
            firstView.setTranslationX(0);
            firstView.setX(firstView.getLeft());

            secondView.setLeft(left);
            secondView.setRight(right);
            secondView.setTranslationX(0);
            secondView.setX(secondView.getLeft());

        }

        //上下交换
        else{
            int top = firstView.getTop();
            int bottom = firstView.getBottom();
            firstView.setTop(secondView.getTop());
            firstView.setBottom(secondView.getBottom());
            firstView.setTranslationY(0);
            firstView.setY(firstView.getTop());

            secondView.setTop(top);
            secondView.setBottom(bottom);
            secondView.setTranslationY(0);
            secondView.setY(secondView.getTop());
        }
    }

    //将可消去点变为0
    private void changArr(){
        for(String id:eliminateSet){
            int imageId = Integer.parseInt(id);
            int i = imageId/10;
            int j = imageId%10;
            arr[i][j] = 0;
            eachColDownNum[j]++;
        }
        eliminateSet.clear();
        record();
        dowmAnimator();
    }

    //记录下落的点和每一列下落的个数和下落后需要检测的点
    private void record(){

        for(int p = 0;p<row;p++){
            for(int q = 0;q<col;q++){
                needDownPoint[p][q] = 0;
            }
        }

        for(int p = 0;p<row;p++){
            for(int q = 0;q<col;q++){
                if(arr[p][q] == 0){
                    for(int k = p-1;k>=0;k--){
                        if(arr[k][q]!=0){
                            needDownPoint[k][q] = 1;
                        }else{
                            break;
                        }
                    }
                }
            }
        }

        for(int p = row-1;p>=0;p--){
            for(int q = col-1;q>=0;q--){
//                if(arr[p][q] == 0){
//                    for(int k = p;k>0;k--){
//                        arr[k][q] = arr[k-1][q];
//                        arr[k-1][q] = 0;
//                    }
//                    arr[0][q] = 0;
//                }
                //需要下落的点下落
                if(needDownPoint[p][q]==1){
                    arr[p+eachColDownNum[q]][q] = arr[p][q];
                    arr[p][q] = 0;
                }
            }
        }
    }

    private void dowmAnimator(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i<row;i++){
                    for(int j = 0;j<col;j++){
                        if(needDownPoint[i][j]==1){
                            ImageView imageView =(ImageView) findViewById(i*10+j);
                            ObjectAnimator.ofFloat(imageView,"translationY",0f,imageView.getHeight()*eachColDownNum[j]).setDuration(500).start();
                        }
                    }
                }
            }
        });

    }

    private void needCheckPoint() throws InterruptedException {
        for(int i = 0;i<row;i++){
            for(int j = 0;j<col;j++){
                if(arr[i][j]!=0)
                    isEliminate(i,j,arr);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(eliminateSet.size()!=0){
                    eliminate();
                }
            }
        });


        if(eliminateSet.size()!=0){
            for(int i = 0;i<col;i++){
                eachColDownNum[i] = 0;
            }

            Thread.sleep(300);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changArr();
                    Log.i("TAG","啥玩意");
                }
            });

            Thread.sleep(600);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gridLayout.removeAllViews();
                    init();
                }
            });
            Thread.sleep(600);

            needCheckPoint();
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gridLayout.removeAllViews();
                    init();
                }
            });
        }
    }

}
