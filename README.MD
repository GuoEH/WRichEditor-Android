基础功能

1. B    粗体      [知乎]    OK

2. I    斜体      [知乎]    OK

3. S    中横线             OK

4. U    下划线             OK

5. H    标题      [知乎]

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

自定义绝对字体的大小
AbsoluteSizeSpan 


=================

应该继承自EditText类

// 两种混合方式
editText.getEditableText().setSpan(new MyBoldStyleSpan(),0 , length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
editText.getEditableText().setSpan(new MyItalicStyleSpan(),0 , length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

=================

1. 换肤

2. 文字颜色

3. 文字背景颜色

4. MD


