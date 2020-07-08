基础功能

1. B    粗体      [知乎]    OK

2. I    斜体      [知乎]    OK

3. S    中横线             OK

4. U    下划线             OK

5. H    标题      [知乎]    OK

6. Q    引用      [知乎]

7. OL   有序列表   [知乎]

8. UL   无序列表   [知乎]

9. L    水平横线   [知乎]

10. 插入自定义视图（图片、视频、网盘）

=================

系统默认实现了：当光标至于某一段统一的格式文字中间时，自动按照当时的格式来进行输入，并且是按照紧接着后面的格式为标准。
此时的问题：如何根据光标位置，获取此位置后面的文字的样式呢？

=================

知乎特有

11. 链接      [Done]

12. @符号

=================

1. 换肤

2. 文字颜色

3. 文字背景颜色

4. MD

反编译多个市场上的app发现：

知乎采用H5的方案
com.zhihu.android.app.mercury.web.WebViewS
支持的格式：
粗体、斜体、标题、引用、有序列、无序列、图片（支持gif播放）、视频、网盘、链接、分割线、at符


小米便签采用 H5方案
com.miui.notes.editor.RichEditView$RichEditScrollView
支持的格式：
粗体、斜体、下划线、列、居中、右对齐、添加图片


锤子便签采用 Native(不确定) + 列表 方案
com.smartisanos.notes.rtf.RichEditTextView
支持的格式有：
标题、居中、列、粗体、引用、待办事项、添加图片、图片按住缩放并重置插入位置




// 这个功能有坑
originalEditable.append(extraEditable);

// TODO 

line 等view 在删除时，需要merge一下，同时在别的editor获取焦点时，取消自定义的焦点


// bug 点击 H 后，变成粗体，再点击H后变成非粗体

// 原生的 BulletSpan、QuoteSpan、DrawableMarginSpan 都有部分问题，多行时，光标错位

// TODO 如何像锤子一样，添加横线

// TODO 引用、有序列表、无序列表，都采用插入一个 WRichEditor 的方式实现


WRichEditor  需要设置背景为null


github上的开源项目，均兼具 bug多+功能少 特点，无法达到商业级应用的水准


// TODO
添加线、图片时，自动在下面加一个edittext





