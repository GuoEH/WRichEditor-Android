
// 待测试
系统默认实现了：当光标至于某一段统一的格式文字中间时，自动按照当时的格式来进行输入，并且是按照紧接着后面的格式为标准。
此时的问题：如何根据光标位置，获取此位置后面的文字的样式呢？

=================

// TODO 

line 等view 在删除时，需要merge一下，同时在别的editor获取焦点时，取消自定义的焦点


// bug 点击 H 后，变成粗体，再点击H后变成非粗体

// TODO WRichEditor  设置lineHeight

// TODO 
headline失效

// TODO
换行后cursor问题

// TODO
cursor delete 时，如果当前行有文字，不应该合并吗？

// TODO
统一 getEditableText getText

// TODO
删除不必要的类

// ========== EXPERIENCE ===========
1. 锤子便签：在quote时，会自动将所有的字符进行一次缩小，颜色变浅
2. 换肤、主题、文字颜色
3. MarkDown 格式

// ========== FIXED ===========
1. 选中某段文字后，点击某个格式，只会响应此变化的格式，之前已经选中的格式不能带入
2. 检查delete键是否连续，image等类型
3. 首行改为Quote模式时，下一行不增加cell的问题
4. ordered list 的体验增强，修改 needAddWRichEditor 方法
