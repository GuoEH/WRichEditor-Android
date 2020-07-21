// ========== TODO ===========
1. 使用HTML还原富文本
2. 如果editor是第一个，删除时，应该将 LineFormat 删除掉
3. ScrollView中的第0位置，添加自定义的HeaderView
4. 有序无序列表间距与普通文字的行间距不同
5. headline偶现失效
6. 换行后cursor问题
7. 相同格式的wrappermerge一下

// ========== EXPERIENCE ===========
1. 锤子便签：在quote时，会自动将所有的字符进行一次缩小，颜色变浅
2. 换肤、主题、文字颜色
3. MarkDown 格式
4. 锤子便签：待办事项
5. RichVideoView RichAudioView
6. 暂时不支持HTML标签
7. 本地存储+本地还原
8. HTML存储+HTML还原

// ========== FIXED ===========
1. 选中某段文字后，点击某个格式，只会响应此变化的格式，之前已经选中的格式不能带入
2. 检查delete键是否连续，image等类型
3. 首行改为Quote模式时，下一行不增加cell的问题
4. ordered list 的体验增强，修改 needAddWRichEditor 方法
5. 点击ScrollView的空白处，将焦点至于最后一个Editor中
6. 点击某个Editor时，被软键盘遮挡后自动调整位置
