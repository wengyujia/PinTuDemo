# PinTuDemo
这是一个拼图小游戏

实现：
1.定义两个工具类：一个用于存储每个小图片（坐标位置和bitmap），一个用于作为切片类

2.自定义一个viewgroup,并声明变量和初始化代码

3.通过onMeasure（）确定布局大小，并在方法中进行切片，排序（用Collections.sort（）进行乱序）和获得
每个图片的宽高等属性，并设置图片的点击事件，在这里引用动画层效果（TranslateAnimation），通过每张图
片的index获得bitmap，复制图片到动画层，监听动画完成图片的交换。

4.判断游戏是否成功，由于游戏过关会更新UI，这里使用Handler方法，在判断每个小图片坐标位置正确后，发送
message给handler进行过关更新。

5.设置接口回调（下一关，时间的改变，游戏结束）,在MainActivity中回调：
a.通过AlertDialog进行手动更新；
b.设置时间和关卡，定义时间变量通过Math.pow（）得到时间，发送信息给handler，在此之前在布局中定义关卡
文本和时间文本，让时间每隔一秒逐一递减。
c.利用安卓的生命周期设置游戏的暂停与恢复